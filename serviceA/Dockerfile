#
# Run
#
FROM java:openjdk-8-jdk-alpine
ADD target/serviceA-0.0.1-SNAPSHOT.jar serviceA.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "serviceA.jar" ]