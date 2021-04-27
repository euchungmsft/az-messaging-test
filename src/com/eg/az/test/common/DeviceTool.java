package com.eg.az.test.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceTool {

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %1$tL [%4$-4s] %5$s %n");
	}
	private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new DeviceTool().perform(args);

	}

	private void perform(String[] args) {
		// TODO Auto-generated method stub
		
		//	az iot hub device-identity list --output json --hub-name kr-iot-01
		
		try {
			Process p = Runtime.getRuntime().exec("az iot hub device-identity list --output json --hub-name kr-iot-01");
			new Thread(new Redirector(p.getInputStream())).start();
			new Thread(new Redirector(p.getErrorStream())).start();			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void perform1(String[] args) {
		// TODO Auto-generated method stub

		String hubName = "kr-iot-01";
		
		//	az iot hub device-identity list --output json --hub-name kr-iot-01
		ProcessBuilder pb = new ProcessBuilder("az", "iot", "hub", "device-identity", "list", "--output", "json", "--hub-name", hubName);
		Map<String, String> env = pb.environment();
		
			for( String key : env.keySet() ){
	            log.info(String.format("key : %s, value : %s", key, env.get(key)) );
	        }
			

		pb.directory(new File("."));
		Process p;
		try {
//			p = pb.start();
//			new Thread(new Redirector(p.getInputStream())).start();
//			new Thread(new Redirector(p.getErrorStream())).start();
			
			InputStream in = (p = pb.start()).getErrorStream();
			
			char c; 
			String s = ""; 
			do {
			   c = (char) in.read(); 
			   if (c == '\n') {
				   System.out.println(s);
				   s = "";
			      break;
			   }
			   s += c + "";
			} while (c != -1);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	class Redirector implements Runnable {

		private InputStream in;

		Redirector(InputStream in) {
			this.in = in;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

		     Scanner scan = new Scanner(in);

		     while(scan.hasNextLine()){
		         String line = scan.nextLine();
		         log.log(Level.FINE, line);
		     }			
		}

	}

}
