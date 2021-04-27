package com.eg.az.test.iothub;

import com.microsoft.azure.sdk.iot.device.*;
import com.eg.az.test.common.ClientConfig;
import com.eg.az.test.common.TelemetryDataPoint;
import com.eg.az.test.common.TestCase;
import com.eg.az.test.servicebus.Publisher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;

public class Client extends TestCase {

	// log config

	private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %1$tL [%4$-4s] %5$s %n");
		Properties config = loadConfig();
		
		deviceFile				= config.getProperty("iothub.client.devicefile");
		intervPerSend 			= new Long(config.getProperty("iothub.client.interv-per-send")).longValue();				
		messagesPerClient 		= new Integer(config.getProperty("iothub.client.messages-per-client")).intValue();
		intervPerClient 		= new Long(config.getProperty("iothub.client.interv-per-client")).longValue();
		clients 				= new Integer(config.getProperty("iothub.client.clients")).intValue();
		dataLen 				= new Integer(config.getProperty("iothub.client.data-length")).intValue();
	}

	// messaging config

	// The device connection string to authenticate the device with your IoT hub.
	// Using the Azure CLI:
	// az iot hub device-identity show-connection-string --hub-name {YourIoTHubName}
	// --device-id MyJavaDevice --output table
	String connString;

	// Using the MQTT protocol to connect to IoT Hub
	private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
	private static DeviceClient client;

	// clien config

	static String deviceFile; 
	static long intervPerSend;
	static int messagesPerClient;
	static long intervPerClient;
	static int clients;
	static int dataLen;
	
	static int sentCounter = 0;

	public Client() {
		this(0, null);
	}

	public Client(int counter, String connString) {
		sentCounter = counter;
		this.connString = connString;
	}

	void send() {

		try {

			client = new DeviceClient(connString, protocol);
			client.open();

			// System.out.println(client.getConfig().getDeviceId());

//			// Create new thread and start sending messages
//			MessageSender sender = new MessageSender();
//			ExecutorService executor = Executors.newFixedThreadPool(1);
//			executor.execute(sender);

			for (int i = 0; i < messagesPerClient; i++) {

				// Simulate telemetry.
				TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint(true, dataLen);
				String msgStr = telemetryDataPoint.serialize();
				Message msg = new Message(msgStr);
				msg.setProperty("temperatureAlert", (telemetryDataPoint.temperature > 30) ? "true" : "false");

				log.info(Thread.currentThread().getName() + " " + ++sentCounter);

				Object lockobj = new Object();
				
				// Send the message.
				client.sendEventAsync(msg, new IotHubEventCallback() {
					public void execute(IotHubStatusCode status, Object context) {
						// System.out.println("IoT Hub responded to message with status: " +
						// status.name());
						if (context != null) {
							synchronized (context) {
								context.notify();
							}
						}
					}
				}, lockobj);

				synchronized (lockobj) {
					lockobj.wait();
				}

				try {
					Thread.currentThread().sleep(intervPerSend);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

//			// Stop the application.
//			System.out.println("Press ENTER to exit.");
//			System.in.read();
//			executor.shutdownNow();
//			client.closeNow();

		} catch (Throwable th) {
			// TODO Auto-generated catch block
			th.printStackTrace();
		}

	}
	
	void perform() {
		
		List<String> connStrings = loadConnStrings();
		final Iterator<String> it = connStrings.iterator();
		
		for (int i = 0; i < clients; i++) {
			new Thread("Client-" + i) {
				public void run() {
					// Connect to the IoT hub.
					String conns = it.next();
					new Client(sentCounter, conns).send();
				}
			}.start();
			
			try {
				Thread.currentThread().sleep(intervPerClient);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new Client().perform();

	}

	private List<String> loadConnStrings() {
		// TODO Auto-generated method stub
		
		List<String> connStrings = new ArrayList<String>();
		
		try {
			Scanner scanner = new Scanner(new File(deviceFile));
			String line;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				if(line == null || (line = line.trim()).length() < 0)
					continue;
				
				if(line.startsWith("#"))
					continue;
				
				connStrings.add(line);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return connStrings;

	}

}
