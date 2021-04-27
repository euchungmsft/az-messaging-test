package com.eg.az.test.servicebus;

import java.text.*;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String v0 = "2021-04-21T13:28:42.1030000Z";
		String v1 = "yyyy-MM-dd'T'HH:mm:ss.SSS0000Z";
		DateFormat sdf = new SimpleDateFormat(v1);
		
		//long date = sdf.parse(v0);
		System.out.println(sdf.format(new java.util.Date()));
		
	}

}
