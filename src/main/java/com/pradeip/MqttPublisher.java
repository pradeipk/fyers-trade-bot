package com.pradeip;

import org.eclipse.paho.client.mqttv3.MqttClient;

public class MqttPublisher {

	/**
	 * This class is responsible for publishing messages to an MQTT broker. It
	 * initializes the MQTT client and connects to the specified broker.
	 */

	MqttClient mqttClient = null;
	String broker = "tcp://broker.mqtt.cool:1883";
	String clientId = "JavaClient151283";
	String topic = "nifty";

	public MqttPublisher() {
		try {
			mqttClient = new MqttClient(broker, clientId);
			mqttClient.connect();
		} catch (Exception e) {
			System.out.println("Error connecting to MQTT broker: " + e.getMessage());
		}
	}

	public MqttPublisher(String broker, String clientId) {
		try {
			mqttClient = new MqttClient(broker, clientId);
			mqttClient.connect();
		} catch (Exception e) {
			System.out.println("Error connecting to MQTT broker: " + e.getMessage());
		}
	}

	public MqttClient getMqttClient() {
		return mqttClient;
	}

}
