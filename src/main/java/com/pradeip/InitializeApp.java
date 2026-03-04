package com.pradeip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import com.pradeip.strategy.AdjustingStraddle;
import com.tts.in.model.FyersClass;

public class InitializeApp implements FyerBotInterface {
	public boolean exitOnce = false;
	public String exitPositionMessage = "";
	public String liveToken = null;
	public JSONObject script = null;
	public String mqttTopic = null;
	public MqttMessage message = null;
	public MqttPublisher mqttPublisher = null;
	public JSONObject orderScript = null;
	public JSONObject PE = null;
	public JSONObject CE = null;
	public JSONObject SCRIPE = null;
	public boolean peActive = false;
	public boolean ceActive = false;
	public String PE_ORDER_PLACED = null;
	public String PE_ORDER_SL_PRICE = null;
	public Double PE_ORDER_SL_INDEX_LEVEL = null;
	public String CE_ORDER_PLACED = null;
	public String CE_ORDER_SL_PRICE = null;
	public Double CE_ORDER_SL_INDEX_LEVEL = null;
	public String mqttBroker = null;
	public String mqttClientId;
	public long startTime = System.currentTimeMillis();
	public Boolean isBegining = true; // Set to false for testing
	public String mqttMessage = null;
	public Integer pointsEarned = null;
	public FyersClass fyersClass = null;
	private String initDirectoryPath = System.getenv("FYER_SCRIPT_PATH");
	public String appid = null;
	public String redirectURI = null;
	boolean initSucceeded = false;
	public JSONObject initData = null;
	public JSONObject orderData = null;
	private String pin = null;
	public String STRATEGY = null;
	private String appHashID;
	public int notifcationInterval;
	public List<String> subscriptionlist = new ArrayList<String>();
	public String ceSymbol = null;
	public String peSymbol = null;	
	public Boolean ENABLE_TRADE = false;
	public int waitTime = 0;
	
	File logFile = null;
	Date today = new Date();
	public Double trailMargin = 0.0; // Margin to trail the premium in points
	public Map<String,PositionDTO> symbolAndid = new HashMap<String,PositionDTO>();
	public java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	public String symbol;
	public static List<PositionDTO> positionDTOList = new ArrayList<PositionDTO>();
	public static List<String> postionIds = new ArrayList<String>();

	//There will always be only one instance of `pool` in a single JVM, no matter how many times it is accessed.
	public static InitializeApp pool = new InitializeApp();

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
	
	
	public static InitializeApp getInstance() {
		if(pool == null) {
			pool = new InitializeApp();
		}
		return pool;
	}
	

	private void init(String initDirectoryPath) {

		try {
			String date = today.getDate() + "-" + (today.getMonth() + 1) + "-" + (1900 + today.getYear());
			logFile = new File(initDirectoryPath, date + "_log.txt");
			logToFileSystem("Initializing application with directory: " + initDirectoryPath);

			System.out.println("Reading Credentials from key JSON file from Directory " + initDirectoryPath);
			initData = new JSONObject(Files.readString(new File(initDirectoryPath, "init.json").toPath()));
			System.out.println("Loading Script to execute from Order JSON file from Directory" + initDirectoryPath);
			script = new JSONObject(Files.readString(new File(initDirectoryPath, "strategy.json").toPath()));
			validateScript(script);
		} catch (IOException | JSONException e) {
			System.out.println("Error reading JSON files: " + e.getMessage());
		}

		try {

			if (initData.has("appid")) {
				appid = initData.getString("appid").trim();
				redirectURI = initData.getString("redirectURI").trim();
				appHashID = initData.getString("appHashId").trim();
				pin = initData.getString("pin").trim();
				fyersClass.clientId = appid;
				String authCode = null;
				Scanner scanner = new Scanner(System.in);
				System.out.println("Do you have the auth token in the init file? (Y/N): ");
				String YN = scanner.nextLine();
				if (YN.equalsIgnoreCase("N")) {
					System.out.println("Redirecting to Fyers API, Do the required authentication Get the auth key.. ");
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

				if (jsonObject != null && jsonObject.has("refresh_token") ) {
					String refresh_token = jsonObject.getString("refresh_token");
					if(refresh_token == null || refresh_token.isEmpty()) {
						System.out.println("Refresh token is null or empty, cannot proceed. Please restart with valid or new  Auth token.");
						System.exit(0);
					}
					liveToken = new LiveToken(refresh_token).getLiveToken();
					System.out.println("Now We have the Live token to proceed with the application.");
					fyersClass.accessToken = liveToken;
				}
			}

			if (script != null) {
				mqttTopic = script.getString("mqtt_topic");
				mqttBroker = script.getString("mqtt_broker");
				mqttClientId = script.getString("client_id");
				mqttPublisher = new MqttPublisher(mqttBroker, mqttClientId);
				notifcationInterval = script.getInt("notificationInterval_min");
				STRATEGY = script.getString("strategy").trim();
				ENABLE_TRADE = script.getBoolean("enable_trade");
				waitTime = script.getInt("waitTime");

				switch (STRATEGY) {

				case STRATEGY_COMBINED_PREMIUM_ALARMS_AND_ACTION:
					System.out.println("\nActivating Strategy: " + STRATEGY_COMBINED_PREMIUM_ALARMS_AND_ACTION);
					combinedPremiuimAlarmsAndAction();
					break;
				case STRATEGY_MONITOR_SL_AND_ACTION:
					System.out.println("\nActivating Strategy: " + STRATEGY_MONITOR_SL_AND_ACTION);
					monitorSLStrategy();
					break;
				case STRATEGY_ADJUSTING_STRADDLE:
					System.out.println("\nActivating Strategy: " + STRATEGY_ADJUSTING_STRADDLE);
					adjustingStraddle();
					break;
				case STRATEGY_STRADDLE_WITH_TRAILING_SL:
					System.out.println("\nActivating Strategy: " + STRATEGY_STRADDLE_WITH_TRAILING_SL);
					straddleWitlSL();
					break;
				case TRAILING_LEG:
					System.out.println("\nActivating Strategy: " + TRAILING_LEG);
					trailingLeg();
					break;
				default:
					System.out.println("\nNo Valid strategy selected.");
					System.exit(0);
					return;
				}

			} else {
				System.out.println("\nScript is null, please check the JSON file.");
			}
		} catch (Exception e) {
			System.out.println("Error initializing application: " + e.getMessage());
		}

	}

	private void trailingLeg() {

		if (script.has(TRAILING_LEG)) {
			orderScript = script.getJSONObject(TRAILING_LEG);
			trailMargin = orderScript.getDouble("trailMargin");
			if (orderScript.has("scripe")) {
				SCRIPE = orderScript.getJSONObject("scripe");
				symbol = EXCHANGE + ":" + SCRIPE.getString("symbol");
				subscriptionlist.add(symbol);
			}
		} else {
			System.out.println("Script data not found in orderScript");
		}

	}

	private void monitorSLStrategy() {
		if (script.has(STRATEGY_MONITOR_SL_AND_ACTION)) {
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
	}
	
	private void combinedPremiuimAlarmsAndAction() {
		if (script.has(STRATEGY_COMBINED_PREMIUM_ALARMS_AND_ACTION)) {
			orderScript = script.getJSONObject(STRATEGY_COMBINED_PREMIUM_ALARMS_AND_ACTION);
			trailMargin = orderScript.getDouble("trailMargin");
			if (orderScript.has("pe")) {				
				PE = orderScript.getJSONObject("pe");
				peSymbol = EXCHANGE+":"+PE.getString("strike");
				subscriptionlist.add(peSymbol);
			}
			if (orderScript.has("ce")) {
				CE = orderScript.getJSONObject("ce");
				ceSymbol = EXCHANGE+":"+CE.getString("strike");
				subscriptionlist.add(ceSymbol);				
			} 
		} else {
			System.out.println("Script data not found in orderScript");
		}
		
	}
	
	// read the actual sell price from strategy json.
	private void adjustingStraddle() {
		if (script.has(STRATEGY_ADJUSTING_STRADDLE)) {
			orderScript = script.getJSONObject(STRATEGY_ADJUSTING_STRADDLE);
			trailMargin = orderScript.getDouble("trailMargin");
			if (orderScript.has("pe")) {				
				PE = orderScript.getJSONObject("pe");
				peSymbol = EXCHANGE+":"+PE.getString("strike");
				AdjustingStraddle.start_pe = PE.getDouble("sell_price");
				subscriptionlist.add(peSymbol);
			}
			if (orderScript.has("ce")) {
				CE = orderScript.getJSONObject("ce");
				ceSymbol = EXCHANGE+":"+CE.getString("strike");
				AdjustingStraddle.start_ce = CE.getDouble("sell_price");
				subscriptionlist.add(ceSymbol);				
			} 
		} else {
			System.out.println("Script data not found in orderScript");
		}
		
	}
	
	private void straddleWitlSL() {
		if (script.has(STRATEGY_STRADDLE_WITH_TRAILING_SL)) {
			orderScript = script.getJSONObject(STRATEGY_STRADDLE_WITH_TRAILING_SL);
			trailMargin = orderScript.getDouble("trailMargin");
			if (orderScript.has("pe")) {				
				PE = orderScript.getJSONObject("pe");
				peSymbol = EXCHANGE+":"+PE.getString("strike");
				subscriptionlist.add(peSymbol);
			}
			if (orderScript.has("ce")) {
				CE = orderScript.getJSONObject("ce");
				ceSymbol = EXCHANGE+":"+CE.getString("strike");
				subscriptionlist.add(ceSymbol);				
			} 
		} else {
			System.out.println("Script data not found in orderScript");
		}
		
	}
	
	public void logToFileSystem(String message) {
		try {

			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (Exception e) {
					System.out.println("Error creating log file: " + e.getMessage());
				}
			}

			today.setTime(System.currentTimeMillis());
			String time = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
			java.nio.file.Files.write(logFile.toPath(), (time + " : " + message + System.lineSeparator()).getBytes(),
					java.nio.file.StandardOpenOption.APPEND);
		} catch (Exception e) {
			System.out.println("Error writing to log file: " + e.getMessage());
		}
	}

	private void validateScript(JSONObject script2) {
		// TODO Auto-generated method stub
		
	}
}
