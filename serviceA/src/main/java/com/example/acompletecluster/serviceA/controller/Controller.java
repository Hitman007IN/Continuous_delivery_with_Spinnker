package com.example.acompletecluster.serviceA.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.acompletecluster.serviceA.util.MessageProperties;

@RestController
public class Controller {
	
	@Autowired
	MessageProperties messageProperties;

    @RequestMapping("/welcome")
    public String welcome() {
        return messageProperties.getWelcome();
    }

    @RequestMapping("/bye")
    public String bye() {
        return messageProperties.getGoodbye();
    }
    
    @RequestMapping("/call")
    public String call() {
        
    	//final String uri = "http://serviceb-service:8081/call";

        //RestTemplate restTemplate = new RestTemplate();
        //String result = restTemplate.getForObject(uri, String.class);

        return "calls to Service B is disabled";
    }

}
