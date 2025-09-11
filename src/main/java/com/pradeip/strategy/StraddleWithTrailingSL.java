package com.pradeip.strategy;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;

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
public class StraddleWithTrailingSL implements FyersSocketDelegate, FyerBotInterface {

	public InitializeApp pool = null;
	private MqttMessage message;
	private long printTimer = System.currentTimeMillis();
	private String mqttMessage;
	private boolean initialized = false;
	private double current_combinedPremium = 0.0;
	private double ce = 0.0;
	private double pe = 0.0;
	private boolean exitDonePE = false;
	private boolean exitDoneCE = false;	
	private double start_ce = 0.0;
	private double start_pe = 0.0;
	private double delta_PE = 0.0;
	private double delta_CE = 0.0;
	private Double combinedPremiumLimit = null;
	private Double startPremium = null;
	private Double delta_Combined_premium =0.0;
	private Double delta_nifty = 0.0; // Current combined premium	
	FyersClass fyersClass = null;
	FyersSocket fyersSocket = null;
	FyerOperations fyerOperations = null;
	DecimalFormat df = null;
	double niftyIndex = 0.0;
	double start_niftyIndex = 0.0;
	private StringBuilder printBuilder = null;	
	long interval = 3 * 60000;
	PositionDTO positionDTO = null;	

	public StraddleWithTrailingSL() {
		pool = InitializeApp.pool;
		fyersClass = pool.getFyersClasss();
	}

	public void WebSocket(FyersSocket fyersSocket) {
		df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		pool.subscriptionlist.add(NSE_NIFTY);
		new ArrayList<String>(pool.subscriptionlist);		
		//fyersSocket = new FyersSocket(3);
		fyersSocket.webSocketDelegate = this;
		fyersSocket.ConnectHSM(ChannelModes.FULL);
		System.out.print("--- \nAbout to Subscribe to the required scrips --> \n");
		pool.subscriptionlist.forEach(x -> {
			System.out.println(x);
		});
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
		
		fyersSocket.SubscribeData(pool.subscriptionlist);

	}	

	@Override
	public void OnIndex(JSONObject index) {
			niftyIndex = index.getDouble("ltp");
			if(!initialized) {
				System.out.println("Subscription Started: Nifty Index: " + niftyIndex);
			}
	}
	
	@Override
	public void OnScrips(JSONObject strikePrice) {			
		OnScripeCallback(strikePrice);
		}	
	
	@Override
	public void OnClose(String arg0) {
		System.out.println("\nWebSocket connection closed: " + arg0);
	}

	@Override
	public void OnDepth(JSONObject arg0) {
		System.out.println("\nOn Depth " + arg0);

	}

	@Override
	public void OnError(JSONObject arg0) {
		System.out.println("\nOnError: " + arg0);

	}	

	@Override
	public void OnMessage(JSONObject arg0) {
		System.out.println("\nMessage Update: " + arg0.toString());

	}

	@Override
	public void OnOpen(String arg0) {
		System.out.println("\nOpen Position Update: " + arg0.toString());

	}

	@Override
	public void OnOrder(JSONObject arg0) {
		System.out.println("\nOrder Update: " + arg0.toString());

	}

	@Override
	public void OnPosition(JSONObject arg0) {
		System.out.println("\nPosition Update: " + arg0.toString());

	}
	
	@Override
	public void OnTrade(JSONObject arg0) {
		System.out.println("\n Trade Update: " + arg0.toString());
	}
	
	private void analyzePremiums() {		
		
		if (System.currentTimeMillis() - printTimer > 60000) {
			
			printBuilder = new StringBuilder();
			printBuilder.append("\n---Time : ").append(pool.sdf.format(new Date(System.currentTimeMillis()))).append("---\n")
			.append("ΔCE   ").append(" : ").append(df.format(delta_CE))
			.append("; ").append(df.format(start_ce)).append(" -> ").append(df.format(ce)).append("\n")
			.append("ΔPE   ").append(" : ").append(df.format(delta_PE))
			.append("; ").append(df.format(start_pe)).append(" -> ").append(df.format(pe)).append("\n")
			.append("---------------------------------").append("\n")
			.append("ΔGain ").append(" : ").append(df.format(delta_Combined_premium))			
			.append("; ").append(df.format(startPremium)).append(" -> ").append(df.format(current_combinedPremium)).append(" | SL ").append(df.format(combinedPremiumLimit)).append("\n")
			.append("---------------------------------").append("\n")
			.append("ΔNifty ").append(" : ").append(df.format(delta_nifty)).append("; ").append(df.format(start_niftyIndex)).append(" -> ").append(df.format(niftyIndex)).append("\n")
			.append("Active ").append(" : ").append(" PE : ").append(!exitDonePE).append(" ; CE : ").append(!exitDoneCE).append(" \n\n");
								
			mqttMessage = printBuilder.toString();
			System.out.println(printBuilder.toString());
			pool.logToFileSystem(printBuilder.toString());
			
			message = new MqttMessage(mqttMessage.getBytes());
			message.setQos(1);
			try {
				pool.mqttPublisher.getMqttClient().publish(pool.mqttTopic, message);
			} catch (MqttPersistenceException e) {
				System.out.println(e.getMessage());
			} catch (MqttException e) {
				System.out.println(e.getMessage());
			}			
			
			if (delta_nifty > 0) {
				System.out.println("Index is upwards since start of the Bot ..");
			}
			printTimer = System.currentTimeMillis();
		}
	}	
	
	private void OnScripeCallback(JSONObject strikePrice) {

		MarketData data = MarketData.fromJson(strikePrice);
		InitializeAndUpdatePremium(data);
		if (!initialized)
			return;
		analyzePremiums();
		
		// exit from both Legs if the combined premium goes above the SL
		if (combinedPremiumLimit < current_combinedPremium) {
			if (pool.postionIds == null || pool.postionIds.isEmpty()) {
				System.out.println("No positions found for the given symbols, exiting.");
				pool.logToFileSystem("No positions found for the given symbols, exiting.");
				fyerOperations.populateLivePositions();
			}
			if (fyerOperations.exitFromAllPositions(pool.postionIds)) {
				System.out.println("Successfully exited from all positions. Closing the Applications");
				System.exit(0);
			}
		}
	}	

	private void InitializeAndUpdatePremium(MarketData data) {

		if (pool.ceSymbol.equalsIgnoreCase(data.getSymbol())) {
			ce = data.getLtp();
		} else if (pool.peSymbol.equalsIgnoreCase(data.getSymbol())) {
			pe = data.getLtp();
		}
		
		if(ce == 0.0 || pe == 0.0 || niftyIndex == 0.0) {
			System.out.println("Either of CE or PE premiums is not initialized, skipping further processing.");
			return;
		} else if (!initialized) {
			// check if you have the positions are not.
			start_ce = ce;
			start_pe = pe;
			fyerOperations = new FyerOperations(pool.getFyersClasss());
			System.out.println("\n Searching your positions --->  ");
			fyerOperations.populateLivePositions();		
			
			if(pool.postionIds == null|| pool.postionIds.isEmpty()) {
				System.out.println("No positions found for the given symbols, exiting.");
				pool.logToFileSystem("No positions found for the given symbols, exiting.");
				
			} else {
				System.out.println("\n found live Positions as below --->  ");
				pool.positionDTOList.forEach(dto -> {
					System.out.println("Symbol: " + dto.symbol + " NetQty: " + dto.netQty + " Sell Quantity : " + dto.sellQty + " Buy Quantity : " + dto.buyQty);
					if(dto.symbol.contains("PE"))
						start_pe = dto.netAvg;
					else if(dto.symbol.contains("CE"))
						start_ce = dto.netAvg;
				});
			}	
			
			// Initialize the combined premium limit and start premium
			startPremium = ce + pe;
			combinedPremiumLimit = startPremium + pool.trailMargin + 5; // Set initial limit to start premium + 20			
			System.out.println("\nInitialized combined premium limit to: " + df.format(combinedPremiumLimit));
			System.out.println(" Initial Premium (ce, pe)--> " + ce + ", " + pe + " (" + df.format(startPremium) + ")");
			System.out.println("\nNifty Index: " + niftyIndex);	
			start_niftyIndex = niftyIndex;
			initialized = true;
			pool.logToFileSystem("Start premium is set to " + startPremium + " with combined premium limit of " + combinedPremiumLimit + " at " + niftyIndex);
		}
		
		// keep track of the premium changes
		current_combinedPremium = ce + pe;
		delta_PE = start_pe - pe ; // positive delta means gain in your favour
		delta_CE = start_ce - ce ; // positive delta means gain in your favour
		delta_Combined_premium = startPremium - current_combinedPremium;
		delta_nifty = niftyIndex - start_niftyIndex;
		setTrailingLimit();	
		
	}
	
	// If the current stop loss margin is greater than 20 points as compared to current combined premium, then reset the limit to current combined premium + 25 points.
	private void setTrailingLimit() {
		
		if (combinedPremiumLimit - current_combinedPremium >  pool.trailMargin) {
			combinedPremiumLimit = current_combinedPremium +  pool.trailMargin;
			mqttMessage = "Updating trailing Stop loss to  " + df.format(combinedPremiumLimit);
			System.out.println(mqttMessage);			
			pool.logToFileSystem(mqttMessage);
			try {
				pool.mqttPublisher.getMqttClient().publish(pool.mqttTopic, new MqttMessage(mqttMessage.getBytes()));
			} catch (MqttException e) {
				System.out.println(e.getMessage());
			}
		}
	}	
}
