package com.example.acompletecluster.serviceA.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "message.default")
public class MessageProperties {

    private String welcome;
    private String goodbye;
    
	public String getWelcome() {
		return welcome;
	}
	public void setWelcome(String welcome) {
		this.welcome = welcome;
	}
	public String getGoodbye() {
		return goodbye;
	}
	public void setGoodbye(String goodbye) {
		this.goodbye = goodbye;
	}

}
