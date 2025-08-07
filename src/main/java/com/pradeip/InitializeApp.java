package com.pradeip;

import java.io.File;
import java.nio.file.Files;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import com.tts.in.model.FyersClass;

public class InitializeApp implements FyerBotInterface {

	public String liveToken = null;

	static JSONObject script = null;
	static String mqttTopic = null;
	MqttMessage message = null;
	static MqttPublisher mqttPublisher = null;
	static JSONObject orderScript = null;
	static JSONObject PE = null;
	static JSONObject CE = null;
	static boolean peActive = false;
	static boolean ceActive = false;
	static String PE_ORDER_PLACED = null;
	static String PE_ORDER_SL_PRICE = null;
	static Double PE_ORDER_SL_INDEX_LEVEL = null;

	String CE_ORDER_PLACED = null;
	String CE_ORDER_SL_PRICE = null;
	Double CE_ORDER_SL_INDEX_LEVEL = null;
	String mqttBroker = null;
	String clientId;
	long startTime = System.currentTimeMillis();
	Boolean isBegining = true; // Set to false for testing
	String mqttMessage = null;
	Integer pointsEarned = null;
	public FyersClass fyersClass = null;
	private String initDirectoryPath = System.getenv("FYER_SCRIPT_PATH");
	String appid = null;
	String redirectURI = null;
	boolean initSucceeded = false;
	JSONObject initData = null;
	JSONObject orderData = null;
	private String pin;


	private String appHashID;

	static InitializeApp pool = new InitializeApp();

	private InitializeApp() {

		getFyersClasss();
		init(initDirectoryPath);
	}

	public FyersClass getFyersClasss() {
		if (fyersClass == null) {
			fyersClass = FyersClass.getInstance();
		}

		return fyersClass;
	}

	private void init(String initDirectoryPath) {

		try {
			System.out.println("Reading Credentials from key JSON file from Directory " + initDirectoryPath);
			initData = new JSONObject(Files.readString(new File(initDirectoryPath, "init.json").toPath()));
			System.out.println("Loading Script to execute from Order JSON file from Directory" + initDirectoryPath);
			script = new JSONObject(Files.readString(new File(initDirectoryPath, "jsonformatter.json").toPath()));

			if (initData.has("appid")) {
				appid = initData.getString("appid").trim();
				redirectURI = initData.getString("redirectURI").trim();
				appHashID = initData.getString("appHashId").trim();
				pin = initData.getString("pin").trim();
				
				String authCode = null;				
				Scanner scanner = new Scanner(System.in);
				System.out.println("Do you have the auth token in the init file? (Y/N): ");
				String YN = scanner.nextLine();
				if (YN.equalsIgnoreCase("N")) {
					System.out.println("Redirecting to Fyers API, Do the required authentication Get the auth key.. ");
					fyersClass.clientId = appid;
					fyersClass.GenerateCode(redirectURI);
					Scanner scanner2 = new Scanner(System.in);
					System.out.println("Enter the Auth Token from Browser: ");
					authCode = scanner2.nextLine();
					if (authCode == null || authCode.isEmpty()) {
						System.out.println("Auth token is required to proceed. Please provide a valid auth token.");
						return;
					}
				} else {
					authCode = initData.getString("authToken").trim();
					System.out.println("Reading auth token from init.json file: " + authCode);
				}			

				JSONObject jsonObject = fyersClass.GenerateToken(authCode, appHashID);

				if (jsonObject != null && jsonObject.has("refresh_token")) {
					String refresh_token = jsonObject.getString("refresh_token");
					liveToken = new LiveToken(refresh_token).getLiveToken();
					System.out.println("Now We have the Live token --: " + liveToken);
					fyersClass.accessToken = liveToken;
				}
			}

			if (script != null) {
				mqttTopic = script.getString("mqtt_topic");
				mqttBroker = script.getString("mqtt_broker");
				clientId = script.getString("client_id");
				mqttPublisher = new MqttPublisher(mqttBroker, clientId);

				if (script.has("sell_order_placed")) {
					orderScript = script.getJSONObject("sell_order_placed");
					if (orderScript.has("pe")) {
						PE = orderScript.getJSONObject("pe");
						peActive = PE.getBoolean("peActive");
						if (peActive) {

							PE_ORDER_PLACED = EXCHANGE + ":" + PE.getString("strike");
							PE_ORDER_SL_INDEX_LEVEL = PE.getDouble("index_based_sl");
							PE_ORDER_SL_PRICE = PE.getString("price_sl");

							System.out.println("Script to execute " + PE_ORDER_PLACED);
						}
					} else {
						System.out.println("Symbol not found in script.json");
					}
					if (orderScript.has("ce")) {
						CE = orderScript.getJSONObject("ce");
						ceActive = CE.getBoolean("ceActive");
						if (ceActive) {
							CE_ORDER_PLACED = EXCHANGE + ":" + CE.getString("strike");
							CE_ORDER_SL_INDEX_LEVEL = CE.getDouble("index_based_sl");
							CE_ORDER_SL_PRICE = CE.getString("price_sl");

							System.out.println("Script to execute " + CE_ORDER_PLACED);
						}
					} else {
						System.out.println("Symbol not found in script.json");
					}
				} else {
					System.out.println("Script data not found in orderScript");
				}

			} else {
				System.out.println("Script is null, please check the JSON file.");
			}
		} catch (Exception e) {
			System.out.println("Error initializing application: " + e.getMessage());
		}

	}
}
