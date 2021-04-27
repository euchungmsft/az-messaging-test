package com.eg.az.test.iothub;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PahoClient {

	public static void main_(String[] args) {
		try {
			System.out.println(generateSasToken("kr-iot-01.azure-devices.net/devices/MyJavaDevice", "ruC7nEc/BccugCIkuZj+15qQa+NZ3ckVgMH+53HWu7Y="));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//HostName=kr-iot-01.azure-devices.net;DeviceId=MyJavaDevice;SharedAccessKey=ruC7nEc/BccugCIkuZj+15qQa+NZ3ckVgMH+53HWu7Y=
		
        String broker       = "tcp://kr-iot-01.azure-devices.net:8883";
        String clientId     = "MyJavaDevice";
        String userName 	= "kr-iot-01.azure-devices.net/MyJavaDevice/?api-version=2018-06-30";
        String password 	= "SharedAccessSignature sr=kr-iot-01.azure-devices.net%2Fdevices%2FMyJavaDevice&sig=s%2BsE%2Be2etrNXRKme%2Bb%2BGIMsHD2H%2FfstIGGlq5%2F7j6xs%3D&se=1619439793";
        String topic        = "devices/MyJavaDevice/messages/events/";	//	{iot hub host name}/devices/{deviceId}/messages/devicebound
        
        String content      = "Message from MqttPublishSample";
        int qos             = 2;

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(userName);
            connOpts.setPassword(password.toCharArray());
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }		
		
	}
	
    public static String generateSasToken(String resourceUri, String key) throws Exception {
        // Token will expire in one hour
        long expiry = Instant.now().getEpochSecond() + 3600;

        String stringToSign = URLEncoder.encode(resourceUri) + "\n" + expiry;
        byte[] decodedKey = Base64.getDecoder().decode(key);

        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "HmacSHA256");
        sha256HMAC.init(secretKey);
        Base64.Encoder encoder = Base64.getEncoder();

        String signature = new String(encoder.encode(
            sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);

        String token = "SharedAccessSignature sr=" + URLEncoder.encode(resourceUri)
                + "&sig=" + URLEncoder.encode(signature, StandardCharsets.UTF_8.name()) + "&se=" + expiry;
            
        return token;
    }	

}
