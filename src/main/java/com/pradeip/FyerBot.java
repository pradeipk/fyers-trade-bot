package com.pradeip;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONObject;

import com.tts.in.model.FyersClass;
import com.tts.in.model.PlaceOrderModel;
import com.tts.in.utilities.Tuple;
import com.tts.in.websocket.FyersSocket;
import com.tts.in.websocket.FyersSocketDelegate;

import in.tts.hsjavalib.ChannelModes;

public class FyerBot implements FyersSocketDelegate, FyerBotInterface {

	private static InitializeApp pool;
	private static FyersClass fyersClass;

	public static void main(String[] args) {

		pool = InitializeApp.pool;
		if (pool == null) {
			System.out.println("InitializeApp pool is null, exiting.");
			return;
		}
		fyersClass = pool.getFyersClasss();
		FyerBot app = new FyerBot();
		app.WebSocket();
	}

	private MqttMessage message;
	private long startTime;
	private String mqttMessage;
	private Integer pointsEarned;
	private boolean isBegining;

	public void WebSocket() {
		List<String> scripList = new ArrayList<>();
		scripList.add("NSE:NIFTY50-INDEX");

		List<String> list = new ArrayList<>();
		list.add("orders");
		list.add("trades");
		list.add("positions");

		if (pool.peActive) {
			scripList.add(pool.PE_ORDER_PLACED);
			System.out.println("\u001B[31mPE Sell Order SL is now activated " + pool.PE_ORDER_PLACED + "\u001B[0m");
		}

		if (pool.ceActive) {
			scripList.add(pool.CE_ORDER_PLACED);
			System.out.println("\u001B[31mCE Sell Order SL is now activated " + pool.CE_ORDER_PLACED + "\u001B[0m");
		}

		FyersSocket fyersSocket = new FyersSocket(3);
		fyersSocket.webSocketDelegate = this;
		fyersSocket.ConnectHSM(ChannelModes.FULL);
		fyersSocket.SubscribeData(scripList);
		fyersSocket.Subscribe(list);
	}

	@Override
	public void OnIndex(JSONObject index) {
		System.out.println("On Index: " + index);

		double niftyIndex = index.getDouble("ltp"); // adjust key as per actual data
		System.out.println("\u001B[31mNifty Price is below 24551: " + niftyIndex + "\u001B[0m");
		// If the index level goes to below SL then exit from PE
		if (pool.peActive && (niftyIndex < pool.PE_ORDER_SL_INDEX_LEVEL)) {
			mqttMessage = "PE Short Postion is at risk; Current level is (" + niftyIndex + ") < "
					+ pool.PE_ORDER_SL_INDEX_LEVEL;
			System.out.println(mqttMessage);

			PlaceOrderModel pom = OrderModel.prepareSellOrder(pool.PE_ORDER_PLACED, 1);

			Tuple<JSONObject, JSONObject> orderExecuted = fyersClass.PlaceOrder(pom);

			Tuple<JSONObject, JSONObject> h = fyersClass.GetAllOrders();

			try {
				message = new MqttMessage(mqttMessage.getBytes());
				message.setQos(1);
				pool.mqttPublisher.getMqttClient().publish(pool.mqttTopic, message);
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			exitOrder(niftyIndex);

		} else if (isBegining || (System.currentTimeMillis() - startTime) > 5000) {
			startTime = System.currentTimeMillis();
			isBegining = false;
			pointsEarned = (int) (niftyIndex - pool.PE_ORDER_SL_INDEX_LEVEL);
			mqttMessage = "PE Short Postion is safe; current level is \n(" + niftyIndex + ") > "
					+ pool.PE_ORDER_SL_INDEX_LEVEL + "\n Gain of " + pointsEarned + " points";
			System.out.println(mqttMessage);
			try {
				message = new MqttMessage(mqttMessage.getBytes());
				message.setQos(1);
				pool.mqttPublisher.getMqttClient().publish(pool.mqttTopic, message);
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (pool.ceActive && (niftyIndex > pool.CE_ORDER_SL_INDEX_LEVEL)) {

			mqttMessage = "CE Short Postion is at risk; Current level is (" + niftyIndex + ") > "
					+ pool.CE_ORDER_SL_INDEX_LEVEL;
		} else if (isBegining || (System.currentTimeMillis() - startTime) > 5000) {
			pointsEarned = (int) (pool.CE_ORDER_SL_INDEX_LEVEL - niftyIndex);
			try {
				mqttMessage = "CE Short Postion is safe; current level is \n(" + niftyIndex + ") < "
						+ pool.CE_ORDER_SL_INDEX_LEVEL + "\n Gain of " + pointsEarned + " points";
				message = new MqttMessage(mqttMessage.getBytes());
				message.setQos(1);
				pool.mqttPublisher.getMqttClient().publish(pool.mqttTopic, message);
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void OnScrips(JSONObject scrips) {
		double scrip = scrips.getDouble("ltp");

		exitOrder(scrip);
		System.out.println("On Scrips: " + scrips);
	}

	private void exitOrder(double scrip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnDepth(JSONObject depths) {
		System.out.println("On Depth: " + depths);
	}

	@Override
	public void OnOrder(JSONObject orders) {
		System.out.println("On Orders: " + orders);
	}

	@Override
	public void OnTrade(JSONObject trades) {
		System.out.println("On Trades: " + trades);
	}

	@Override
	public void OnPosition(JSONObject positions) {

		System.out.println("On Positions: " + positions);

		JSONObject positionsData = positions.getJSONObject("positions");
		JSONObject position = positionsData.getJSONObject("NSE:SBIN-EQ");
		String quantity = position.getString("quantity");
		System.out.println("Quantity: " + quantity);
		if (quantity.equals("0")) {
			System.out.println("Quantity is 0, placing order");
		} else {
			System.out.println("Quantity is not 0, not placing order");
		}

	}

	@Override
	public void OnOpen(String status) {
		System.out.println("On open: " + status);
	}

	@Override
	public void OnClose(String status) {
		System.out.println("On Close: " + status);
	}

	@Override
	public void OnError(JSONObject error) {
		System.out.println("On Error: " + error);
	}

	@Override
	public void OnMessage(JSONObject message) {
		System.out.println("OnMessage: " + message);
	}

	/*
	 * private static void init(String initDirectoryPath) { JSONObject initData =
	 * null; try { script = new JSONObject(Files.readString(new
	 * File(initDirectoryPath, "jsonformatter.json").toPath())); initData = new
	 * JSONObject(Files.readString(new File(initDirectoryPath,
	 * "init.json").toPath())); // File f = new File(initDirectoryPath,
	 * "execution.json");
	 * 
	 * if (initData.has("livetoken")) { LiveToken1 =
	 * initData.getString("livetoken"); appid = initData.getString("appid"); pin =
	 * initData.getString("pin"); }
	 * 
	 * if (script != null) { mqttTopic = script.getString("mqtt_topic"); mqttBroker
	 * = script.getString("mqtt_broker"); clientId = script.getString("client_id");
	 * mqttPublisher = new MqttPublisher(mqttBroker, clientId);
	 * System.out.println("App ID: " + appid); System.setProperty("Live_Token",
	 * LiveToken1); System.out.println("Live Token set: " + LiveToken1);
	 * 
	 * if (script.has("sell_order_placed")) { orderScript =
	 * script.getJSONObject("sell_order_placed"); if (orderScript.has("pe")) { PE =
	 * orderScript.getJSONObject("pe"); peActive = PE.getBoolean("peActive"); if
	 * (peActive) {
	 * 
	 * PE_ORDER_PLACED = EXCHANGE + ":" + PE.getString("strike");
	 * PE_ORDER_SL_INDEX_LEVEL = PE.getDouble("index_based_sl"); PE_ORDER_SL_PRICE =
	 * PE.getString("price_sl");
	 * 
	 * System.out.println("Script to execute " + PE_ORDER_PLACED); } } else {
	 * System.out.println("Symbol not found in script.json"); } if
	 * (orderScript.has("ce")) { CE = orderScript.getJSONObject("ce"); ceActive =
	 * CE.getBoolean("ceActive"); if (ceActive) { CE_ORDER_PLACED = EXCHANGE + ":" +
	 * CE.getString("strike"); CE_ORDER_SL_INDEX_LEVEL =
	 * CE.getDouble("index_based_sl"); CE_ORDER_SL_PRICE = CE.getString("price_sl");
	 * 
	 * System.out.println("Script to execute " + CE_ORDER_PLACED); } } else {
	 * System.out.println("Symbol not found in script.json"); } } else {
	 * System.out.println("Script data not found in orderScript"); }
	 * 
	 * } else { System.out.println("Live Token not found in init.json"); }
	 * initSucceeded = true; } catch (IOException e) {
	 * System.out.println("Error reading execution.json: " + e.getMessage());
	 * initSucceeded = false; return; }
	 */

}