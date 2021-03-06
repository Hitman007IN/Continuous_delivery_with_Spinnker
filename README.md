# Continuous_delivery_with_Spinnker

![alt text](https://github.com/Hitman007IN/Continuous_delivery_with_Spinnker/blob/master/screenshots/ci_cd_pipeline.png)
 
# Create Cluster on GKE

Step 1 :- create cluster
- gcloud container clusters create spinnaker-cd \
--num-nodes 6 \
--machine-type n1-standard-4 \
--zone=us-east1-b \
--scopes "https://www.googleapis.com/auth/source.read_write,cloud-platform"

Step 2 :- Connect to cluster
- gcloud container clusters get-credentials spinnaker-cd --zone us-east1-b --project flawless-mason-258102

# Create Service Account

Name -> spinnaker-storage-account
Role -> storage.admin

# Install Helm

Step 1 :- Download and install the helm binary 
wget https://storage.googleapis.com/kubernetes-helm/helm-v2.9.0-linux-amd64.tar.gz

Step 2 :- Unzip the file to your local system 
$ tar zxfv helm-v2.9.0-linux-amd64.tar.gz 
$ sudo chmod +x linux-amd64/helm && sudo mv linux-amd64/helm /usr/bin/helm

Step 3 :- Grant Tiller, the server side of Helm, the cluster-admin role in your cluster
$ kubectl create clusterrolebinding user-admin-binding --clusterrole=cluster-admin --user=$(gcloud config get-value account) 
$ kubectl create serviceaccount tiller --namespace kube-system 
$ kubectl create clusterrolebinding tiller-admin-binding --clusterrole=cluster-admin --serviceaccount=kube-system:tiller

Step 4 :- Grant Spinnaker the cluster-admin role so it can deploy resources across all namespaces 
$ kubectl create clusterrolebinding --clusterrole=cluster-admin --serviceaccount=default:default spinnaker-admin

Step 5 :- Initialize Helm to install Tiller in your cluster 
$ helm init --service-account=tiller --upgrade 
$ helm repo update

Step 6 :- Ensure that Helm is properly installed 
$ helm version

# Install Jenkins on GKE

Step 1 :- Configure Jenkins
- git clone https://github.com/GoogleCloudPlatform/continuous-deployment-on-kubernetes.git
- cd continuous-deployment-on-kubernetes

Step 2 :- Install Jenkins
- helm install -n ci stable/jenkins -f jenkins/values.yaml --version 1.2.2 --wait
- kubectl get pods
- kubectl create clusterrolebinding jenkins-deploy --clusterrole=cluster-admin --serviceaccount=default:cd-jenkins

Step 3 :- Connect to Jenkins UI
- export POD_NAME=$(kubectl get pods --namespace default -l "app.kubernetes.io/component=jenkins-master" -l "app.kubernetes.io/instance=ci" -o jsonpath="{.items[0].metadata.name}")
- kubectl port-forward $POD_NAME 9000:8080 >> /dev/null &

Step 3 :- Connect to Jenkins
- printf $(kubectl get secret ci-jenkins -o jsonpath="{.data.jenkins-admin-password}" | base64 --decode);echo
- open browser with port 9000 

# Create Jenkins Pipeline

Step 1 :-  Add your service account credentials

- First we will need to configure our GCP credentials in order for Jenkins to be able to access our code repository

- In the Jenkins UI, Click “Credentials” on the left
- Click either of the “(global)” links (they both route to the same URL)
- Click “Add Credentials” on the left
- From the “Kind” dropdown, select “Google Service Account from metadata”. your project name will be displayed
- Click “OK”

Google Cloud Sourec Repo - https://source.developers.google.com/p/flawless-mason-258102/r/github_hitman007in_continuous-deployment-on-kubernetes



# Configure Spinnaker

Step 1 :- Create a bucket for Spinnaker to store its pipeline configuration 

Bucket name -> flawless-mason-258102-spinnaker-config
Region -> us-east1
Class -> Standard

Step 2 :- Create the configuration file 
rename the service account key file to spinnaker-sa.json
$ export SA_JSON=$(cat spinnaker-sa.json) 
$ export PROJECT=$(gcloud info --format='value(config.project)') 
$ export BUCKET=$PROJECT-spinnaker-config
$ cat > spinnaker-config.yaml <<EOF 
'''storageBucket: $BUCKET 
gcs:   
  enabled: true 
  project: $PROJECT   
  jsonKey: '$SA_JSON' 
 
# Disable minio as the default. Minio is an S3-compatible object store that you can host yourself. This is the persistent storage solution we recommend when you don’t want to depend on a cloud provider to host your Spinnaker data.
minio: 
  enabled: false

# Disable Jenkins and manually install it
jenkins:
  enabled: false
 
# Configure your Docker registries here 
accounts: 
- name: gcr  
  address: https://gcr.io 
  username: _json_key   
  password: '$SA_JSON' 
  email: 1234@5678.com 
EOF
'''

# Connect Jenkins
# Step 1 :- Connect to Jenkins UI
# - export JENKINS_POD=$(kubectl get pods --namespace default -l "component=cd-jenkins-master") 
# - kubectl port-forward $POD_NAME 9000:8080 >> /dev/null &

# Step 2 :- Get Credentials
# - kubectl exec -it $JENKINS_POD -- /bin/bash
# - cat /var/jenkins_home/secrets/initialAdminPassword
# - open browser with port 9000 
# - Enter pasword 

# Deploy the Spinnaker chart

Step 1 :- helm install -n cd stable/spinnaker -f spinnaker-config.yaml --timeout 600 --version 0.3.1

Step 2 :- Set up port forwarding to the Spinnaker UI 
$ export DECK_POD=$(kubectl get pods --namespace default -l "component=deck" -o jsonpath="{.items[0].metadata.name}") 

$ kubectl port-forward --namespace default $DECK_POD 8080:8080 >> /dev/null &

# Configure deployment pipeline

Step 1 :- open in port 8080
Step 2 :- In the Spinnaker UI, click Actions, then click Create Application
Step 3 :- n the New Application dialog, enter the following fields:
Name: servicea
Owner Email: proofofconceptoncloud@gmail.com

Step 4 :- Create prod Namespace and give permission for default SA
- kubectl create namespace prod
- kubectl create clusterrolebinding default-service --clusterrole=cluster-admin --serviceaccount=default:default

# Cost Savings at Night
gcloud container clusters resize  spinnaker-cd --num-nodes=0 --zone=us-east1-b

# Reference 
https://medium.com/velotio-perspectives/know-everything-about-spinnaker-how-to-deploy-using-kubernetes-engine-57090881c78f
https://blog.opsmx.com/deploying-helm-chart-to-kubernetes-using-spinnaker/
https://cloud.google.com/solutions/managing-and-deploying-apps-to-multiple-gke-clusters-using-spinnaker
