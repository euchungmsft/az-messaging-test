package com.eg.az.test.servicebus;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusProcessorClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.eg.az.test.common.TestCase;
import com.eg.az.test.common.TestHook;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class SubscriberT extends TestCase{

	//	log config

	private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	static String connectionString;
	static String topicName;	
	static String subName;	
	static int clients = -1;
	static long markOver;

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %1$tL [%4$-4s] %5$s %n");
		Properties config = loadConfig();
		
		connectionString	= config.getProperty("servicebus.subscribert.connection-string");
		topicName			= config.getProperty("servicebus.subscribert.topic-name");
		subName				= config.getProperty("servicebus.subscribert.sub-name");
		clients 			= new Integer(config.getProperty("servicebus.subscribert.clients")).intValue();
		markOver 			= new Long(config.getProperty("common.mark.over")).longValue();
	}
	
	static int recvCounter = 0;

	SubscriberT(){
		this(0);
	}

	SubscriberT(int recvCounter){
		this.recvCounter = recvCounter;
	}

	// handles received messages
	void receiveMessages() throws InterruptedException {

		// Create an instance of the processor through the ServiceBusClientBuilder
		ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder().connectionString(connectionString)
				.processor().topicName(topicName).subscriptionName(subName).processMessage(context->{
					
					//	1. fetch from message contexts - TelemetryDataPoint
					
					try {
						JsonElement element = new JsonParser().parse(context.getMessage().getBody().toString());
						long laptime, createdAt = element.getAsJsonObject().get("createdat").getAsLong();
						log.info(Thread.currentThread().getName()+" Laptime "+(laptime = System.currentTimeMillis() - createdAt)+(markOver > 0 && laptime > markOver ? " *** " : ""));
						putLaptime(laptime);
					}
					catch(Throwable th) {
						th.printStackTrace();
					} // ignore
					
			
				})
				.processError(context -> {
					context.getException().printStackTrace();
				}).buildProcessorClient();

		log.info("Starting,..");
		processorClient.start();

		//TimeUnit.SECONDS.sleep(10);
		//System.out.println("Stopping and closing the processor");
		//processorClient.close();
		
	}


	private void perform() {
		// TODO Auto-generated method stub
		
		for(int i=0; i<clients; i++) {	
			new Thread("Client "+i) {
				public void run() {
					try {
						new SubscriberT(recvCounter).receiveMessages();
						//receiveMessages();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
		}
		
	}
		
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

		SubscriberT subscr = new SubscriberT();
		//Runtime.getRuntime().addShutdownHook(new TestHook(subscr));
		subscr.perform();
		//subscr.clearAll();
		
	}


}
