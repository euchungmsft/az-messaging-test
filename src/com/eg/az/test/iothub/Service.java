package com.eg.az.test.iothub;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.eg.az.test.common.TestCase;
import com.eg.az.test.common.TestHook;
import com.eg.az.test.eventhub.Receiver;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Instant;
import java.util.Properties;
import java.util.logging.Logger;

public class Service extends TestCase {

	// log config

	private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	static String EVENT_HUBS_COMPATIBLE_ENDPOINT;
	static String EVENT_HUBS_COMPATIBLE_PATH;
	static String IOT_HUB_SAS_KEY;
	static String IOT_HUB_SAS_KEY_NAME;
	static int clients;
	static long markOver;
	
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %1$tL [%4$-4s] %5$s %n");
		Properties config = loadConfig();
		
		EVENT_HUBS_COMPATIBLE_ENDPOINT	= config.getProperty("iothub.service.endpoint");
		EVENT_HUBS_COMPATIBLE_PATH		= config.getProperty("iothub.service.hub-path");
		IOT_HUB_SAS_KEY					= config.getProperty("iothub.service.sas-key");
		IOT_HUB_SAS_KEY_NAME			= config.getProperty("iothub.service.sas-keyname");
		clients 						= new Integer(config.getProperty("iothub.service.clients")).intValue();	
		markOver 						= new Long(config.getProperty("common.mark.over")).longValue();
	}

	private static String EH_COMPATIBLE_CONNECTION_STRING_FORMAT = "Endpoint=%s/;EntityPath=%s;SharedAccessKeyName=%s;SharedAccessKey=%s";

	static int recvCounter = 0;

	Service() {
		this(0);
	}

	Service(int recvCounter) {
		this.recvCounter = recvCounter;
	}

	
	/**
	 * This method receives events from all partitions asynchronously starting from
	 * the newly available events in each partition.
	 *
	 * @param eventHubConsumerAsyncClient The {@link EventHubConsumerAsyncClient}.
	 * @param lockobj 
	 */
	private void receiveFromAllPartitions(EventHubConsumerAsyncClient eventHubConsumerAsyncClient, Object context) {

		eventHubConsumerAsyncClient.receive(false) // set this to false to read only the newly available events
				.subscribe(partitionEvent -> {
//					System.out.println();
//					System.out.printf("%nTelemetry received from partition %s:%n%s",
//							partitionEvent.getPartitionContext().getPartitionId(),
//							partitionEvent.getData().getBodyAsString());
//					System.out.printf("%nApplication properties (set by device):%n%s",
//							partitionEvent.getData().getProperties());
//					System.out.printf("%nSystem properties (set by IoT Hub):%n%s",
//							partitionEvent.getData().getSystemProperties());
					
					//	1. fetch from message contexts - TelemetryDataPoint
					
					try {
						JsonElement element = new JsonParser().parse(partitionEvent.getData().getBodyAsString());
						long laptime, createdAt = element.getAsJsonObject().get("createdat").getAsLong();
						
						log.info(Thread.currentThread().getName()+" Laptime "+(laptime = System.currentTimeMillis() - createdAt) +(markOver > 0 && laptime > markOver ? " *** " : ""));
						//putLaptime(laptime);
					}
					catch(Throwable th) {
						th.printStackTrace();
					} // ignore					

//					//	2. fetch from getEnqueuedTime()
//					
//					try {
//						/** interestingly it gets minus */
//						log.info(Thread.currentThread().getName()+" Laptime "+
//							(Instant.now().toEpochMilli() - partitionEvent.getData().getEnqueuedTime().toEpochMilli())
//							//Instant.now().toEpochMilli() + " " +
//							//context.getMessage().getEnqueuedTime().toInstant().toEpochMilli() +
//						);
//					}
//					catch(Throwable th) {
//						th.printStackTrace();
//					} // ignore					
					
					
				}, ex -> {
					ex.printStackTrace();
				}, () -> {
					//System.out.println("Completed receiving events");
					synchronized (context) {
						context.notify();
					}
				});

	}
	
	public void receiveEvents(){
		
		// Build the Event Hubs compatible connection string.
		String eventHubCompatibleConnectionString = String.format(EH_COMPATIBLE_CONNECTION_STRING_FORMAT,
				EVENT_HUBS_COMPATIBLE_ENDPOINT, EVENT_HUBS_COMPATIBLE_PATH, IOT_HUB_SAS_KEY_NAME, IOT_HUB_SAS_KEY);

		// Setup the EventHubBuilder by configuring various options as needed.
		EventHubClientBuilder eventHubClientBuilder = new EventHubClientBuilder()
				.consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
				.connectionString(eventHubCompatibleConnectionString);

		// uncomment to setup proxy
		// setupProxy(eventHubClientBuilder);

		// uncomment to use Web Sockets
		// eventHubClientBuilder.transportType(AmqpTransportType.AMQP_WEB_SOCKETS);

		// Create an async consumer client as configured in the builder.
		try (EventHubConsumerAsyncClient eventHubConsumerAsyncClient = eventHubClientBuilder
				.buildAsyncConsumerClient()) {

			Object lockobj = new Object();
			
			receiveFromAllPartitions(eventHubConsumerAsyncClient, lockobj);
			log.info("Starting,..");
			
			synchronized (lockobj) {
				lockobj.wait();
			}

			// uncomment to run these samples
			// receiveFromSinglePartition(eventHubConsumerAsyncClient);
			// receiveFromSinglePartitionInBatches(eventHubConsumerAsyncClient);

			// Shut down cleanly.
			//System.out.println("Press ENTER to exit.");
			//System.in.read();
			//System.out.println("Shutting down...");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void perform() {
		// TODO Auto-generated method stub
		
		for(int i=0; i<clients; i++) {	
			new Thread("Client-"+i) {
				public void run() {
					new Service(recvCounter).receiveEvents();
					//receiveEvents();
				}
			}.start();
		}
		
	}	

	/**
	 * The main method to start the sample application that receives events from
	 * Event Hubs sent from an IoT Hub device.
	 *
	 * @param args ignored args.
	 * @throws Exception if there's an error running the application.
	 */
	public static void main(String[] args){
		
		Service recv = new Service();
		// Runtime.getRuntime().addShutdownHook(new TestHook(recv));
		recv.perform();
		//recv.clearAll();		

	}

}
