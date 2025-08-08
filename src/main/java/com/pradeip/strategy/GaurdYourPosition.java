package com.pradeip.strategy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONObject;

import com.pradeip.FyerBotInterface;
import com.pradeip.FyerOperations;
import com.pradeip.InitializeApp;
import com.pradeip.PositionDTO;
import com.tts.in.model.FyersClass;
import com.tts.in.websocket.FyersSocket;
import com.tts.in.websocket.FyersSocketDelegate;

import in.tts.hsjavalib.ChannelModes;

//exit from your position if index breaches the SL level.
public class GaurdYourPosition implements FyersSocketDelegate, FyerBotInterface {

	public InitializeApp pool = null;;
	private MqttMessage message;
	private long startTime;
	private String mqttMessage;
	private Integer pointsEarned;
	private boolean isBegining;
	FyersClass fyersClass = null;
	FyersSocket fyersSocket = null;

	private enum CONDITION {
		GAURD_SHORT_POSITION_PUT, GAURD_SHORT_POSITION_CALL
	};

	public GaurdYourPosition(InitializeApp pool) {
		this.pool = pool;
		fyersClass = pool.fyersClass;
	}

	public void WebSocket() {
		List<String> scripList = new ArrayList<>();
		scripList.add("NSE:NIFTY21SEP");
		scripList.add("NSE:TCS-EQ");
		scripList.add("NSE:NIFTY50-INDEX");
		fyersSocket = new FyersSocket(3);
		fyersSocket.webSocketDelegate = this;
		fyersSocket.ConnectHSM(ChannelModes.FULL);
		fyersSocket.SubscribeData(scripList);
		
	}

	@Override
	public void OnClose(String arg0) {
		System.out.println("WebSocket connection closed: " + arg0);
	}

	@Override
	public void OnDepth(JSONObject arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnError(JSONObject arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnIndex(JSONObject index) {
		double niftyIndex = index.getDouble("ltp");
		if (pool.peActive) {
			triggerGaurdPE(niftyIndex);
		} else if (pool.peActive) {
			triggerGaurdCE(niftyIndex);
		}

		System.out.println("Index Data: -----> " + index.getDouble("ltp"));
		if (pool.exitOnce) {
			fyersSocket.Close();
			System.out.println("Exit already done once, closing the websocket connection.");
		}

	}

	private void triggerGaurdCE(double niftyIndex) {
		// TODO Auto-generated method stub

	}

	private void triggerGaurdPE(double niftyIndex) {
		try {
			if (niftyIndex < pool.PE_ORDER_SL_INDEX_LEVEL) {
				mqttMessage = "PE Short Postion is at risk; Current level is (" + niftyIndex + ") < "
						+ pool.PE_ORDER_SL_INDEX_LEVEL;
				System.out.println(mqttMessage);

				FyerOperations fyersOperation = new FyerOperations(fyersClass);
				PositionDTO positions = fyersOperation.getPositions(null);
				if (!pool.exitOnce && positions != null && positions.positionIdList != null
						&& !positions.positionIdList.isEmpty()) {
					System.out.println("Positions found to exit.");
					if (fyersOperation.exitPosition(positions.positionIdList)) {
						pool.exitOnce = true;
						mqttMessage = "exitOnce is set to true, script invalidated, restart the bot to re-enable.\n\n"
								+ InitializeApp.pool.exitPositionMessage;
						System.out
								.println("exitOnce is set to true, script invalidated, restart the bot to re-enable.");
						InitializeApp.pool.exitPositionMessage = "";
					}
					;
				} else if (pool.exitOnce) {
					System.out.println("Exit already done once please close the app.");
				}

				message = new MqttMessage(mqttMessage.getBytes());
				message.setQos(1);
				pool.mqttPublisher.getMqttClient().publish(pool.mqttTopic, message);

			} else if (pool.peActive && (isBegining
					|| (System.currentTimeMillis() - startTime) > pool.notifcationInterval * 60 * 1000)) {
				startTime = System.currentTimeMillis();
				isBegining = false;
				pointsEarned = (int) (niftyIndex - pool.PE_ORDER_SL_INDEX_LEVEL);
				mqttMessage = "PE Short Postion is safe; current level is \n(" + niftyIndex + ") > "
						+ pool.PE_ORDER_SL_INDEX_LEVEL + "\n Gain of " + pointsEarned + " points";
				System.out.println(mqttMessage);

				message = new MqttMessage(mqttMessage.getBytes());
				message.setQos(1);
				pool.mqttPublisher.getMqttClient().publish(pool.mqttTopic, message);

			}
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void OnMessage(JSONObject arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnOpen(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnOrder(JSONObject arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnPosition(JSONObject arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnScrips(JSONObject arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnTrade(JSONObject arg0) {
		// TODO Auto-generated method stub

	}

}
