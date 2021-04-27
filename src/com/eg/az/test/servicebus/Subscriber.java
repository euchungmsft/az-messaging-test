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

public class Subscriber extends TestCase{

	//	log config

	private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	static String connectionString;
	static String queueName;	
	static int clients = -1;
	static long markOver;

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %1$tL [%4$-4s] %5$s %n");
		Properties config = loadConfig();
		
		connectionString		= config.getProperty("servicebus.subscriber.connection-string");
		queueName				= config.getProperty("servicebus.subscriber.queue-name");
		clients = new Integer(config.getProperty("servicebus.subscriber.clients")).intValue();
		markOver = new Long(config.getProperty("common.mark.over")).longValue();
	}
	
	static int recvCounter = 0;

	Subscriber(){
		this(0);
	}

	Subscriber(int recvCounter){
		this.recvCounter = recvCounter;
	}

	// handles received messages
	void receiveMessages() throws InterruptedException {

		// Create an instance of the processor through the ServiceBusClientBuilder
		ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder().connectionString(connectionString)
				.processor().queueName(queueName).processMessage(context->{
					
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
					
					
//					//	2. fetch from getEnqueuedTime()
//					
//					try {
//						/** interestingly it gets minus */
//						log.info(Thread.currentThread().getName()+" Laptime "+
//							(Instant.now().toEpochMilli() - context.getMessage().getEnqueuedTime().toInstant().toEpochMilli())
//							//Instant.now().toEpochMilli() + " " +
//							//context.getMessage().getEnqueuedTime().toInstant().toEpochMilli() +
//						);
//					}
//					catch(Throwable th) {
//						th.printStackTrace();
//					} // ignore
					
					
//					//	3. fetch from getApplicationProperties().get("iothub-enqueuedtime");
//
//					String v1 = (String) context.getMessage().getApplicationProperties().get("iothub-enqueuedtime");
//					if(v1 != null && v1.length() > 1) {
//						DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS");
//						try {
//							Date dt = df.parse(v1.substring(0, 23)+"+0000Z");
//							long timestamp = dt.getTime();
//							log.info(Thread.currentThread().getName()+" Laptime "+ (System.currentTimeMillis() - timestamp));
//						} catch (ParseException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						
//					}
					
//					//	0. simple
//
//					ServiceBusReceivedMessage message = context.getMessage();
//					//System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s \n",
//					//		message.getMessageId(), message.getSequenceNumber(), message.getBody());
//					

					
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
						new Subscriber(recvCounter).receiveMessages();
						//receiveMessages();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
		}
		
	}
	
	private void clearAll() {
		// TODO Auto-generated method stub

		// Create an instance of the processor through the ServiceBusClientBuilder
		ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder().connectionString(connectionString)
				.processor().queueName(queueName).processMessage(context->{
					log.info("Removing <"+context.getMessage().getSequenceNumber()+"> "+context.getMessage().getMessageId());
				})
				.processError(context -> {
					context.getException().printStackTrace();
				}).buildProcessorClient();

		log.info("Clearing,..");
		processorClient.start();

	}
		
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

		Subscriber subscr = new Subscriber();
		//Runtime.getRuntime().addShutdownHook(new TestHook(subscr));
		subscr.perform();
		//subscr.clearAll();
		
	}


}
