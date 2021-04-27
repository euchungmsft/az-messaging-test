package com.eg.az.test.eventhub;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.eg.az.test.common.TestCase;
import com.eg.az.test.common.TestHook;
import com.eg.az.test.servicebus.Subscriber;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class Receiver extends TestCase {

	// log config

	private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	static String connectionString;
	static String eventHubName;	
	static String storageConnectionString;
	static String storageContainerName;
	static int clients = -1;
	static long markOver;

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %1$tL [%4$-4s] %5$s %n");
		Properties config = loadConfig();
		
		connectionString		= config.getProperty("eventhub.receiver.connection-string");
		eventHubName			= config.getProperty("eventhub.receiver.eventhub-name");
		storageConnectionString	= config.getProperty("eventhub.receiver.storage-connection-string");
		storageContainerName	= config.getProperty("eventhub.receiver.storage-container-name");
		clients = new Integer(config.getProperty("eventhub.receiver.clients")).intValue();
		markOver = new Long(config.getProperty("common.mark.over")).longValue();
		
	}

	static int recvCounter = 0;

	Receiver() {
		this(0);
	}

	Receiver(int recvCounter) {
		this.recvCounter = recvCounter;
	}

	public void receiveEvents() {

		// Create a blob container client that you use later to build an event processor
		// client to receive and process events
		BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
				.connectionString(storageConnectionString).containerName(storageContainerName).buildAsyncClient();

		// Create a builder object that you will use later to build an event processor
		// client to receive and process events and errors.
		EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
				.connectionString(connectionString, eventHubName)
				.consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME).processEvent(eventContext -> {

					//	1. fetch from message contexts - TelemetryDataPoint
					
					try {
						JsonElement element = new JsonParser().parse(eventContext.getEventData().getBodyAsString());
						long laptime, createdAt = element.getAsJsonObject().get("createdat").getAsLong();
						log.info(Thread.currentThread().getName()+" Laptime "+(laptime = System.currentTimeMillis() - createdAt) +(markOver > 0 && laptime > markOver ? " *** " : ""));
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
//							(Instant.now().toEpochMilli() - eventContext.getEventData().getEnqueuedTime().toEpochMilli())
//							//Instant.now().toEpochMilli() + " " +
//							//eventContext.getEventData().getEnqueuedTime().toEpochMilli() +
//						);
//					}
//					catch(Throwable th) {
//						th.printStackTrace();
//					} // ignore					
					
//					//	3. fetch from getApplicationProperties().get("iothub-enqueuedtime");
//
//					String v1 = (String) eventContext.getEventData().getProperties().get("iothub-enqueuedtime");
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
//					// System.out.printf("Processing event from partition %s with sequence number %d
//					// with body: %s %n",
//					// eventContext.getPartitionContext().getPartitionId(),
//					// eventContext.getEventData().getSequenceNumber(),
//					// eventContext.getEventData().getBodyAsString());
//

					//if (eventContext.getEventData().getSequenceNumber() % 10 == 0) {
						eventContext.updateCheckpoint();
					//}
				}).processError(errorContext -> {
					errorContext.getThrowable().printStackTrace();
				}).checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient));

		// Use the builder object to create an event processor client
		EventProcessorClient eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
		eventProcessorClient.start();
		log.info("Starting,..");

		// eventProcessorClient.stop();
		// System.out.println("Event processor stopped.");
		// System.out.println("Exiting process");

	}

	private void perform() {
		// TODO Auto-generated method stub
		
		for(int i=0; i<clients; i++) {	
			new Thread("Client-"+i) {
				public void run() {
					new Receiver(recvCounter).receiveEvents();
					//receiveEvents();
				}
			}.start();
		}
		
	}
	
	private void clearAll() {
		// TODO Auto-generated method stub

		// Create a blob container client that you use later to build an event processor
		// client to receive and process events
		BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
				.connectionString(storageConnectionString).containerName(storageContainerName).buildAsyncClient();

		// Create a builder object that you will use later to build an event processor
		// client to receive and process events and errors.
		EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
				.connectionString(connectionString, eventHubName)
				.consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME).processEvent(context -> {
					/**
					 * System properties {x-opt-offset=11338984, x-opt-sequence-number=12831,
					 * x-opt-enqueued-time=2021-04-23T04:48:10.970Z}
					 */
					log.info("Removing <" + context.getEventData().getSequenceNumber() + "> "
							+ context.getEventData().getEnqueuedTime());
					context.updateCheckpoint();
				}).processError(errorContext -> {
					errorContext.getThrowable().printStackTrace();
				}).checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient));

		// Use the builder object to create an event processor client
		EventProcessorClient eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
		eventProcessorClient.start();
		log.info("Removing,..");

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Receiver recv = new Receiver();
		// Runtime.getRuntime().addShutdownHook(new TestHook(recv));
		recv.perform();
		//recv.clearAll();

//		FileReader fr;
//		try {
//			fr = new FileReader("config.properties");
//			Properties p = new Properties();
//			p.load(fr);
//			fr.close();
//			
//			//p.save(System.out, "for debug");
//			System.out.println(p.getProperty("eventhub.receiver.connection-string"));
//			System.out.println(p.getProperty("eventhub.receiver.storage-connection-string"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
	}

}
