package com.eg.az.test.eventhub;

import java.io.IOException;
import java.util.Arrays;
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
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.eg.az.test.common.TelemetryDataPoint;
import com.eg.az.test.common.TestCase;
import com.eg.az.test.servicebus.Publisher;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class Sender extends TestCase {

	// log config

	private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %1$tL [%4$-4s] %5$s %n");
		Properties config = loadConfig();

		connectionString		= config.getProperty("eventhub.sender.connection-string");
		eventHubName			= config.getProperty("eventhub.sender.eventhub-name");
		intervPerSend 			= new Long(config.getProperty("eventhub.sender.interv-per-send")).longValue();				
		messagesPerClient 		= new Integer(config.getProperty("eventhub.sender.messages-per-client")).intValue();
		intervPerClient 		= new Long(config.getProperty("eventhub.sender.interv-per-client")).longValue();
		clients 				= new Integer(config.getProperty("eventhub.sender.clients")).intValue();
		dataLen 				= new Integer(config.getProperty("eventhub.sender.data-length")).intValue();		
	}

	// messaging config

	//static final String connectionString = "Endpoint=sb://ehkrcnt01.servicebus.windows.net/;SharedAccessKeyName=tester;SharedAccessKey=mkLLOPFEjx49IBbu/Jp95RqQzaCeogsT60O5aUmiI24=;EntityPath=eh1";
	//static final String eventHubName = "eh1";
	
	//static final String connectionString = "Endpoint=sb://ehkrcnt01.servicebus.windows.net/;SharedAccessKeyName=tester1;SharedAccessKey=PIrOckmc+95KpwlwjcZTl+/PncmCJQOQH8V54aLdyDs=;EntityPath=eh2";
	//static final String eventHubName = "eh2";	//	 partition 1

	//static final String connectionString = "Endpoint=sb://ehkrcnt01.servicebus.windows.net/;SharedAccessKeyName=tester1;SharedAccessKey=yaUJyCC9JgBKC0Eoy4VlutQ06Nh95R7NbPBNqKyz7rk=;EntityPath=eh3";
	//static final String eventHubName = "eh3";	//	 partition 2
	
	static final String connectionString;
	static final String eventHubName;	

	// clien config

	static long intervPerSend;
	static int messagesPerClient;
	static long intervPerClient;
	static int clients;
	static int dataLen;
	
	static int sentCounter = 0;

	public Sender() {
		this(0);
	}

	public Sender(int counter) {
		sentCounter = counter;
	}

	public void send() {

		EventHubProducerClient producer = new EventHubClientBuilder().connectionString(connectionString, eventHubName)
				.buildProducerClient();

		for (int i = 0; i < 100000; i++) {
			TelemetryDataPoint tdp = new TelemetryDataPoint(true, dataLen);
			String msgStr = tdp.serialize();

			producer.send(Arrays.asList(new EventData(msgStr)));
			log.info(Thread.currentThread().getName() + " " + ++sentCounter);

			try {
				Thread.currentThread().sleep(intervPerSend);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		producer.close();
	}

	public void send_() {
		// create a producer client
		EventHubProducerClient producer = new EventHubClientBuilder().connectionString(connectionString, eventHubName)
				.buildProducerClient();

		// sample events in an array
		List<EventData> allEvents = Arrays.asList(new EventData("Foo"), new EventData("Bar"));

		// create a batch
		EventDataBatch eventDataBatch = producer.createBatch();

		for (EventData eventData : allEvents) {
			// try to add the event from the array to the batch
			if (!eventDataBatch.tryAdd(eventData)) {
				// if the batch is full, send it and then create a new batch
				producer.send(eventDataBatch);
				eventDataBatch = producer.createBatch();

				// Try to add that event that couldn't fit before.
				if (!eventDataBatch.tryAdd(eventData)) {
					throw new IllegalArgumentException(
							"Event is too large for an empty batch. Max size: " + eventDataBatch.getMaxSizeInBytes());
				}
			}
		}
		// send the last batch of remaining events
		if (eventDataBatch.getCount() > 0) {
			producer.send(eventDataBatch);
		}
		producer.close();
	}

	void perform() {

		for (int i = 0; i < clients; i++) {
			new Thread("Client-" + i) {
				public void run() {
					new Sender(sentCounter).send();

					try {
						Thread.currentThread().sleep(intervPerClient);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new Sender().perform();

	}

}
