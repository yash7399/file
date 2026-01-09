package com.example.sft;

import java.io.InputStream;
import java.util.Properties;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Component;

@Component
public class Prop {
	public static Properties getProp() {
			Properties prop = new Properties();
		    	
		    	try {
		    		
		    		InputStream input= FileTransferTest.class.getClassLoader().getResourceAsStream("application.properties");
		    		
		    		if(input == null) {
		    			throw new RuntimeErrorException(null, "Unable to find config.properties");
		    		}
		    		prop.load(input);
		    	}
		    	catch(Exception e){
		    		
		    		System.out.println("Failed to load config"+ e.getMessage());
		    		
		    	}
				return prop;
	}
}
