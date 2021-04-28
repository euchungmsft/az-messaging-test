package com.eg.az.test.servicebus;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.eg.az.test.common.TelemetryDataPoint;
import com.eg.az.test.common.TestCase;

public class PublisherT extends TestCase {

	//	log config

	private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %1$tL [%4$-4s] %5$s %n");
		Properties config = loadConfig();

		connectionString		= config.getProperty("servicebus.publishert.connection-string");
		topicName				= config.getProperty("servicebus.publishert.topic-name");
		intervPerSend 			= new Long(config.getProperty("servicebus.publishert.interv-per-send")).longValue();				
		messagesPerClient 		= new Integer(config.getProperty("servicebus.publishert.messages-per-client")).intValue();
		intervPerClient 		= new Long(config.getProperty("servicebus.publishert.interv-per-client")).longValue();
		clients 				= new Integer(config.getProperty("servicebus.publishert.clients")).intValue();
		dataLen 				= new Integer(config.getProperty("servicebus.publishert.data-length")).intValue();		
	}

	//	messaging config

	static String connectionString;
	static String topicName;    
	
	//	clien config

	static long intervPerSend;
	static int messagesPerClient;
	static long intervPerClient;
	static long clients;
	static int dataLen;
	
	static int sentCounter = 0;

	public PublisherT() {
		this(0);
	}

	public PublisherT(int counter) {
		sentCounter = counter;
	}

	void send() {

		// create a Service Bus Sender client for the queue
		ServiceBusSenderClient senderClient = new ServiceBusClientBuilder().connectionString(connectionString).sender()
				.topicName(topicName).buildClient();
		
		// send one message to the queue
		for (int i = 0; i < messagesPerClient; i++) {
			TelemetryDataPoint tdp = new TelemetryDataPoint(true, dataLen);
			String msgStr = tdp.serialize();

			senderClient.sendMessage(new ServiceBusMessage(msgStr));
			log.info(Thread.currentThread().getName() + " " + ++sentCounter);

			try {
				Thread.currentThread().sleep(intervPerSend);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	void perform() {

		for (int i = 0; i < clients; i++) {
			new Thread("Client-" + i) {
				public void run() {
					new PublisherT(sentCounter).send();
				}
			}.start();
			
			try {
				Thread.currentThread().sleep(intervPerClient);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new PublisherT().perform();
	}

}
