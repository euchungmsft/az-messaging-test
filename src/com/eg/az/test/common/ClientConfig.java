package com.eg.az.test.common;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class ClientConfig {

	private Map<String, String> iothub = new HashMap<>();
	private Map<String, String> eventhub = new HashMap<>();
	private Map<String, String> servicebus = new HashMap<>();
	private String name;
	
	public ClientConfig(){
		iothub.put("key1", "val1");
		eventhub.put("key1", "val1");
		eventhub.put("key2", "val2");
		eventhub.put("key3", "val3");
		servicebus.put("key1", "val1");
		servicebus.put("key2", "val2");
		
		name = "name1";
	}
	
	void save() {
	    Gson gson = new Gson();
	    System.out.println(gson.toJson(this));
	    
//	    FileWriter fw;
//		try {
//			fw = new FileWriter("testclient-config.json");
//			gson.toJson(this, fw);
//			fw.flush();
//			fw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}	
	
}
