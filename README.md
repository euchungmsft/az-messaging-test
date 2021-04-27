


##IOT Hub

Quickstart: Send telemetry to an Azure IoT hub and read it with a Java application
https://docs.microsoft.com/en-us/azure/iot-hub/quickstart-send-telemetry-java

###See Register Devcie 
https://docs.microsoft.com/en-us/azure/iot-hub/quickstart-send-telemetry-java#register-a-device

**For Sender**

	az iot hub device-identity create --hub-name kr-iot-01 --device-id MyJavaDevice
	
	{
	  "authentication": {
	    "symmetricKey": {
	      "primaryKey": "ruC7nEc/BccugCIkuZj+15qQa+NZ3ckVgMH+53HWu7Y=",
	      "secondaryKey": "SbKBwjRm8Suj5LJPWr1qi8tTCDKWvsBy9+0HTv90fNk="
	    },
	    "type": "sas",
	    "x509Thumbprint": {
	      "primaryThumbprint": null,
	      "secondaryThumbprint": null
	    }
	  },
	  "capabilities": {
	    "iotEdge": false
	  },
	  "cloudToDeviceMessageCount": 0,
	  "connectionState": "Disconnected",
	  "connectionStateUpdatedTime": "0001-01-01T00:00:00",
	  "deviceId": "MyJavaDevice",
	  "deviceScope": null,
	  "etag": "MzE4NzUxOTU1",
	  "generationId": "637546501104263193",
	  "lastActivityTime": "0001-01-01T00:00:00",
	  "parentScopes": [],
	  "status": "enabled",
	  "statusReason": null,
	  "statusUpdatedTime": "0001-01-01T00:00:00"
	}


	az iot hub device-identity connection-string show --hub-name kr-iot-01 --device-id MyJavaDevice --output table
	
	HostName=kr-iot-01.azure-devices.net;DeviceId=MyJavaDevice;SharedAccessKey=ruC7nEc/BccugCIkuZj+15qQa+NZ3ckVgMH+53HWu7Y=

**For Reciever**

	az iot hub show --query properties.eventHubEndpoints.events.endpoint --name kr-iot-01
	
	"sb://iothub-ns-kr-iot-01-9891450-1bb899bfad.servicebus.windows.net/"
	
	az iot hub show --query properties.eventHubEndpoints.events.path --name kr-iot-01
	
	"kr-iot-01"
	
	az iot hub policy show --name service --query primaryKey --hub-name kr-iot-01
	
	"3sBGROzWeTZQDA3MVHQzJmCacPTaZ8HlKeMURoFusR4="

###How to run

	mvn clean package


	mvn exec:java -Dexec.mainClass="com.eg.az.test.iothub.Client"


	mvn exec:java -Dexec.mainClass="com.eg.az.test.iothub.Service"



##Service Bus

mvn exec:java -Dexec.mainClass="com.eg.az.test.servicebus.Publisher"
 
mvn exec:java -Dexec.mainClass="com.eg.az.test.servicebus.Subscriber"

##Event Hub

mvn exec:java -Dexec.mainClass="com.eg.az.test.eventhub.Sender"

mvn exec:java -Dexec.mainClass="com.eg.az.test.eventhub.Receiver"


mvn exec:java -Dexec.mainClass="com.eg.az.test.common.DeviceTool"


