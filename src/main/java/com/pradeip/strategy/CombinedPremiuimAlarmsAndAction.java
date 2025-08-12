
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
import com.pradeip.dto.MarketData;
import com.tts.in.model.FyersClass;
import com.tts.in.websocket.FyersSocket;
import com.tts.in.websocket.FyersSocketDelegate;

import in.tts.hsjavalib.ChannelModes;

//exit from your position if index breaches the SL level.
public class CombinedPremiuimAlarmsAndAction implements FyersSocketDelegate, FyerBotInterface {

	public InitializeApp pool = null;
	private MqttMessage message;
	private long startTime = System.currentTimeMillis();
	private long printTimer = System.currentTimeMillis();
	private String mqttMessage;
	private Integer pointsEarned;	
	private boolean initialized = false;
	private double current_combinedPremium = 0.0;
	private double ce = 0.0;
	private double pe = 0.0;
	
	private double pre_current_combinedPremium = 0.0;
	private double pre_ce = 0.0;
	private double pre_pe = 0.0;
	
	
	
	private Double combinedPremiumLimit = null;
	private Double startPremium = null;
	private Double trailingLimit = 25.0; // Initial trailing limit in points

	
	private List<Double> celist = new ArrayList<Double>();
	private List<Double> pelist = new ArrayList<Double>();
	private List<Double> combinedList = new ArrayList<Double>();
	private int pointer	= 0;
	private boolean isBegining;
	FyersClass fyersClass = null;
	FyersSocket fyersSocket = null;
	FyerOperations fyerOperations = null;
	double niftyIndex = 0.0;
	
	long interval = 3 * 60000;

	private enum CONDITION {
		GAURD_SHORT_POSITION_PUT, GAURD_SHORT_POSITION_CALL
	};

	public CombinedPremiuimAlarmsAndAction() {
		pool = InitializeApp.pool;
		fyersClass = pool.getFyersClasss();
	}

	public void WebSocket() {
		pool.subscriptionlist.add(NSE_NIFTY);
		//scripList.add("NSE:NIFTY2581424600CE");
		//scripList.add("NSE:NIFTY2581424600PE");
		//scripList.add("NSE:NIFTY50-INDEX");
		fyersSocket = new FyersSocket(3);
		fyersSocket.webSocketDelegate = this;
		fyersSocket.ConnectHSM(ChannelModes.FULL);
		System.out.println("\nWebSocket connection established; starting the subscription for the items...");
		pool.subscriptionlist.forEach(x -> {
			System.out.println(x);
		});
		fyersSocket.SubscribeData(pool.subscriptionlist);

	}	

	@Override
	public void OnIndex(JSONObject index) {
			niftyIndex = index.getDouble("ltp");
			//System.out.println("Nifty Index: " + niftyIndex);

	}
	
	
	@Override
	public void OnScrips(JSONObject strikePrice) {
		MarketData data = MarketData.fromJson(strikePrice);
		if (pool.ceSymbol.equalsIgnoreCase(data.getSymbol())) {
			ce = data.getLtp();
		} else if (pool.peSymbol.equalsIgnoreCase(data.getSymbol())) {
			pe = data.getLtp();
		}
		
		if(ce == 0.0 || pe == 0.0) {
			System.out.println("Either of CE or PE premiums is not initialized, skipping further processing.");
			return;
		} else if (!initialized) {
			startTime = System.currentTimeMillis();
			// Initialize the combined premium limit and start premium
			startPremium = ce + pe;
			combinedPremiumLimit = startPremium + trailingLimit; // Set initial limit to start premium + 20			
			System.out.println("\nInitialized combined premium limit to: " + combinedPremiumLimit);
			System.out.println(" Premium (ce, pe)--> " + ce + ", " + pe + "(" + startPremium + ")");
			System.out.println("\nNifty Index: " + niftyIndex);
			
			initialized = true;
			pool.logToFileSystem("Start premium is set to " + startPremium + " with combined premium limit of " + combinedPremiumLimit + " at " + niftyIndex);
		}
		
		current_combinedPremium = ce + pe;		
		//if the current premium is below the limit and also the margin is greater than 20 then trail the stop loss limit.
		setTrailingLimit();
		//System.out.println("CE Premium is -----> " + data.getLast_traded_time());
		
		if (System.currentTimeMillis() - startTime > 1 * 60000) {

			if (pre_ce != 0.0 && pre_pe != 0.0 && pre_current_combinedPremium != 0.0) {
				celist.add(pre_ce - ce);
				pelist.add(pre_pe - pe);
				combinedList.add(pre_current_combinedPremium - current_combinedPremium);
				analyzePremiums();
			}

			pre_current_combinedPremium = current_combinedPremium;
			pre_ce = ce;
			pre_pe = pe;

			if (celist.size() > 200) {
				celist.clear();
				pelist.clear();
				combinedList.clear();
			}
			startTime = System.currentTimeMillis();
		}
		
		// Check if the combined premium (current_combinedPremium) exceeds the limit
		if (ce!=0.0 && pe!=0.0 && (combinedPremiumLimit < current_combinedPremium)) {
			System.out.println("Current combined Premium  is (" + current_combinedPremium + ") > " + combinedPremiumLimit);
			fyerOperations = new FyerOperations(fyersClass);
			PositionDTO positionDTO = fyerOperations.getAllPositions();
			fyerOperations.exitPosition(positionDTO.positionIdList);
			pool.logToFileSystem("SL hit for combined premium: " + current_combinedPremium + " at " + niftyIndex + " with limit " + combinedPremiumLimit);

		} else if (ce!=0.0 && pe!=0.0 && (combinedPremiumLimit >= current_combinedPremium) && (System.currentTimeMillis() - printTimer > 60000)) {
			System.out.println("\n\n Current combined Premium  is within limits (" + current_combinedPremium + ") < " + combinedPremiumLimit);
			System.out.println("Nifty Index: " + niftyIndex);
			printTimer = System.currentTimeMillis();
			
		}

	}

	private void analyzePremiums() {
		// difference of previous and current premiums and average of all differences
		Double ceDiff = 0.0;
		if (celist.size() > 1) {
			for (Double price : celist) {
				ceDiff += price;
			}

			ceDiff = ceDiff / celist.size();

			Double peDiff = 0.0;
			for (Double price : pelist) {
				peDiff += price;
			}
			peDiff = peDiff / pelist.size();

			System.out.println(
					" Average CE Premium at interval of every (minutes) " + interval / 60000 + " is " + ceDiff);
			System.out.println(
					" Average PE Premium at interval of every (minutes) " + interval / 60000 + " is " + peDiff);
			pool.logToFileSystem("Average premeium change for ce is " + ceDiff + " and for pe is " + peDiff);

		}
	}
	
	// If the stop loss margin is greater than 20 points, then reset the limit to current combined premium + 25 points.
	private void setTrailingLimit() {
		
		if (combinedPremiumLimit - current_combinedPremium > trailingLimit) {
			trailingLimit = 20.0; // Reset trailing limit to 20 points
			combinedPremiumLimit = current_combinedPremium + trailingLimit;
			System.out.println("Updating Stop loss to  " + combinedPremiumLimit);
			System.out.println("Net Gain in premium is " + (startPremium - current_combinedPremium));
			pool.logToFileSystem("New premium limit is set to " + combinedPremiumLimit);
		}
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
	public void OnTrade(JSONObject arg0) {
		// TODO Auto-generated method stub

	}

}