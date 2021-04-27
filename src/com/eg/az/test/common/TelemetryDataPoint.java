package com.eg.az.test.common;

import java.util.Random;

import com.google.gson.Gson;

public class TelemetryDataPoint {
	
	public double temperature;
	public double humidity;
	public long createdat;
	
	double minTemperature = 20;
	double minHumidity = 60;
	
	String data;
	
	public TelemetryDataPoint(){
		createdat = System.currentTimeMillis();
	}

	public TelemetryDataPoint(boolean initiate){
		this(initiate, -1);
	}

	public TelemetryDataPoint(boolean initiate, int len){
		this();
		if(initiate) {
			Random rand = new Random();
			temperature = minTemperature + rand.nextDouble() * 15;
			humidity = minHumidity + rand.nextDouble() * 20;
			
			if(len > 0) {
				RandomString session = new RandomString(len);
				data = session.nextString();
			}
		}
	}
	
	// Serialize object to JSON format.
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
