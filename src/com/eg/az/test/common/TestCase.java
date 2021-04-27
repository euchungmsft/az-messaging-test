package com.eg.az.test.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.*;

public abstract class TestCase {
	List<Long> lts = new ArrayList<Long>();
	
	public void putLaptime(long laptime) {
		lts.add(new Long(laptime));
	}
	
	public List<Long> getData() {
		return lts;
	}
	
	public static Properties loadConfig() {
		FileReader fr;
		try {
			fr = new FileReader("config.properties");
			Properties p = new Properties();
			p.load(fr);
			fr.close();
			
			return p;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
