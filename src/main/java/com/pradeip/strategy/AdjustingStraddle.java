package com.pradeip.strategy;
import java.sql.Date;
import java.text.DecimalFormat;
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

//exit from your position if index breaches the SL level. and then re-enter the position if the combined premium comes back within limits.
public class AdjustingStraddle implements FyersSocketDelegate, FyerBotInterface {

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
	private boolean exitDonePE = false;
	private boolean exitDoneCE = false;
	private boolean reEnterDoneCE = false;
	private double bookedPrice = 0.0;
	private double lossBooked = 0.0;
	private boolean exitDoneBOTH = false;
	
	public boolean stayAwayFlag = false;
	public static int stayAwayCount = 0;
	public static int reEnterCount = 0;
	public boolean renter = false;
	
	static public double start_ce = 0.0;
	static public double start_pe = 0.0;
	private double delta_PE = 0.0;
	private double delta_CE = 0.0;
	private Double combinedPremiumLimit = null;
	private Double startPremium = null;
	private Double delta_Combined_premium =0.0;
	private Double delta_nifty = 0.0; // Current combined premium
	//private Double trailingLimit =  // Initial trailing limit in points
	private List<Double> celist = new ArrayList<Double>();
	private List<Double> pelist = new ArrayList<Double>();
	private List<Double> combinedList = new ArrayList<Double>();
	private int pointer	= 0;
	private boolean isBegining;
	
	String CE_POSITION_ID;
	String PE_POSITION_ID;
	
	FyersClass fyersClass = null;
	FyersSocket fyersSocket = null;
	FyerOperations fyerOperations = null;
	DecimalFormat df = null;
	double niftyIndex = 0.0;
	double start_niftyIndex = 0.0;
	private StringBuilder printBuilder = null;
	
	long interval = 3 * 60000;
	PositionDTO positionDTO = null;
	private boolean keepContinue = true;
	private  List<Double> deltaGainList = new ArrayList<Double>(); 

	private enum CONDITION {
		GAURD_SHORT_POSITION_PUT, GAURD_SHORT_POSITION_CALL
	};

	public AdjustingStraddle() {
		pool = InitializeApp.pool;
		fyersClass = pool.getFyersClasss();
	}

	public void WebSocket(FyersSocket fyersSocket) {
		df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		pool.subscriptionlist.add(NSE_NIFTY);
		this.fyersSocket = fyersSocket;
		fyersSocket.webSocketDelegate = this;
		fyersSocket.ConnectHSM(ChannelModes.FULL);
		System.out.print("--- \nAbout to Subscribe to the required scrips --> \n");
		
		pool.subscriptionlist.forEach(x -> {
			System.out.println(x);
		});
		
		try {
			System.out.print("\nWaiting for" + pool.waitTime + " ms before subscribing to the data..");
			Thread.sleep(pool.waitTime);
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
		System.out.println("WebSocket connection closed: " + arg0);
	}

	@Override
	public void OnDepth(JSONObject arg0) {
		System.out.println("Market Depth received: " + arg0.toString());

	}

	@Override
	public void OnError(JSONObject arg0) {
		System.out.println("WebSocket error: " + arg0.toString());

	}	

	@Override
	public void OnMessage(JSONObject arg0) {
		System.out.println("Message received: " + arg0.toString());

	}

	@Override
	public void OnOpen(String arg0) {
		System.out.println("WebSocket connection opened: " + arg0);

	}

	@Override
	public void OnOrder(JSONObject arg0) {
		System.out.println("Order update received: " + arg0.toString());

	}

	@Override
	public void OnPosition(JSONObject arg0) {
		System.out.println("Position update received: " + arg0.toString());

	}
	
	@Override
	public void OnTrade(JSONObject arg0) {
		System.out.println("Trade update received: " + arg0.toString());

	}
	
	private void analyzePremiums() {		
		
		if (delta_Combined_premium < -12) {
			stayAwayFlag = true;
			System.out.println("\nStay Away Triggered .." + ++stayAwayCount);
			pool.logToFileSystem("\nStay Away Triggered .." + stayAwayCount);
			renter = false;
			// renter is possible only whenstayAway has happened.
		} else if (stayAwayFlag && delta_Combined_premium > -10) {
			stayAwayFlag = false;
			renter = true;
			System.out.println("\nre-enter Triggered .." + ++reEnterCount);
			pool.logToFileSystem("\nre-enter Triggered .." + reEnterCount);
		}
		
		if (System.currentTimeMillis() - printTimer > 60000) {
			
			printBuilder = new StringBuilder();
			printBuilder.append("\n---Time : ").append(pool.sdf.format(new Date(System.currentTimeMillis()))).append("---\n")
			//.append("Gain/Loss in premium for (+ive delta is gain in your favour.)").append("\n")
			.append("ΔCE   ").append(" : ").append(df.format(delta_CE))
			.append("; ").append(df.format(start_ce)).append(" -> ").append(df.format(ce)).append("\n")
			.append("ΔPE   ").append(" : ").append(df.format(delta_PE))
			.append("; ").append(df.format(start_pe)).append(" -> ").append(df.format(pe)).append("\n")
			.append("---------------------------------").append("\n")
			.append("ΔGain ").append(" : ").append(df.format(delta_Combined_premium))			
			.append("; ").append(df.format(startPremium)).append(" -> ").append(df.format(current_combinedPremium)).append(" | SL ").append(df.format(combinedPremiumLimit)).append("\n")
			.append("---------------------------------").append("\n")
			//.append("Trail SL").append(" : ").append(df.format(combinedPremiumLimit)).append("\n")
			//.append("Start Premium").append(" : ").append(df.format(startPremium)).append(" \n ")
			//.append("Combined Premium").append(df.format(startPremium)).append(" ---> ").append(df.format(current_combinedPremium)).append(df.format(delta_Combined_premium)).append(" \n ")
			.append("ΔNifty ").append(" : ").append(df.format(delta_nifty)).append("; ").append(df.format(start_niftyIndex)).append(" -> ").append(df.format(niftyIndex)).append("\n")
			.append("Active ").append(" : ").append(" PE : ").append(!exitDonePE).append(" ; CE : ").append(!exitDoneCE).append(" \n\n");
				
			deltaGainList.add(delta_Combined_premium);
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
				System.out.println("\nIndex is upwards since start of the Bot ..");
			}  else {
				System.out.println("\nIndex has declined since start of the Bot ..");
			}
			printTimer = System.currentTimeMillis();
		}

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
	
	private void OnScripeCallback(JSONObject strikePrice) {

		MarketData data = MarketData.fromJson(strikePrice);
		initializeAndUpdatePremium(data);
		
		if (!initialized)
			return;
		
		analyzePremiums();		
		
		if(keepContinue) {
			
		}
		
		if(stayAwayFlag) {
			if(!exitDonePE && delta_PE < 0) {
				System.out.println("\n Staying away from PE side of the straddle as index has moved down by more than 12 points.");
				stayAwayFromPE();
			}
			if(!exitDoneCE & delta_CE < 0) {
				System.out.println("\n Staying away from CE side of the straddle as index has moved down by more than 12 points.");
				stayAwayFromCE();
			}
		}
		
		if(renter && !reEnterDoneCE) {
			if(exitDonePE && (combinedPremiumLimit >= current_combinedPremium)) {
				// re-neter the PE leg if the combined premium has come done.
				fyerOperations.Sell(pool.peSymbol, 1);
				renter = false;
				exitDonePE = false;
			}
			if(exitDoneCE && (combinedPremiumLimit >= current_combinedPremium)) {
				// re-neter the CE leg if the combined premium has come done.
				fyerOperations.Sell(pool.ceSymbol, 1);
				renter = false;
				exitDoneCE = false;
			}
		}		
		// SL hit, exit the position.
		if (combinedPremiumLimit < current_combinedPremium) {
			List<String> exitPositionList = new ArrayList<String>();
			// Need Fix here
			// -ve delta means the premium has gone up, so you need to exit the position.
			// (start -current)
			if (!exitDonePE && delta_PE < 0 && pool.ENABLE_TRADE) {
				
				System.out.println("\n Market data symbol is : " + data.getSymbol() + " and " + pool.peSymbol);
				System.out.println("\n Premium for PE has increased by :" + df.format(delta_PE) + " Exitting the position."
						+ pool.peSymbol);
				// check that you are exitiong the right position.
				//String positionID = pool.symbolAndid.get(pool.peSymbol).id;

				if (PE_POSITION_ID == null) {
					//System.out.println("\n Position ID is null for " + pool.peSymbol + ", cannot exit position.");
					return;
				} 
				
				if (fyerOperations.exitPositionById(PE_POSITION_ID)) {
					exitDonePE = true;
					System.out.println(" Exit successful for PE: " + pool.peSymbol);
					exitPositionList.clear();
				} else {
					System.out.println("Error exiting position for PE: " + pool.peSymbol);
				}
				System.out.println(pool.exitPositionMessage);
			}
			// -ve delta means the premium has gone up, so you need to exit the position.
			// (start -current)
			if (!exitDoneCE && delta_CE < 0 && pool.ENABLE_TRADE) {
				System.out.println("\nMarket data symbol is :" + data.getSymbol() + " and " + pool.peSymbol);
				//String positionID = pool.symbolAndid.get(pool.ceSymbol).id;
				if (CE_POSITION_ID == null) {
					//System.out.println("\n Position ID is null for " + pool.peSymbol + ", cannot exit position.");
					return;
				} 
				
				if (fyerOperations.exitPositionById(CE_POSITION_ID)) {
					exitDoneCE = true;
					System.out.println(" Exit successful for CE: " + pool.ceSymbol);
				} else {
					System.out.println(" Error exiting position for CE: " + pool.peSymbol);
				}
				System.out.println(pool.exitPositionMessage);
			}

			pool.logToFileSystem("SL hit for combined premium: " + current_combinedPremium + " at " + niftyIndex
					+ " with limit " + combinedPremiumLimit);

		} else if (exitDoneCE && (combinedPremiumLimit >= current_combinedPremium)) {
			// re-neter the CE leg if the combined premium has come done.
			fyerOperations.Sell(pool.ceSymbol, 1);
			
		} else if (exitDonePE && (combinedPremiumLimit >= current_combinedPremium)) {
			// re-neter the PE leg if the combined premium has come done.
			fyerOperations.Sell(pool.peSymbol, 1);
		
		} else {
			if (System.currentTimeMillis() - printTimer > 60000) {
				System.out.println("\n\n Current combined Premium  is within limits (" + current_combinedPremium
						+ ") < " + combinedPremiumLimit);
				System.out.println("Nifty Index: " + niftyIndex);
				printTimer = System.currentTimeMillis();
			}
		}
	}
	// keep a check of the latest data and update the premiums.
	private void initializeAndUpdatePremium(MarketData data) {
		if (pool.ceSymbol.equalsIgnoreCase(data.getSymbol())) {
			ce = data.getLtp();
		} else if (pool.peSymbol.equalsIgnoreCase(data.getSymbol())) {
			pe = data.getLtp();
		}
		
		if(ce == 0.0 || pe == 0.0 || niftyIndex == 0.0) {
			System.out.println("\nEither of CE or PE premiums is not initialized, skipping further processing.");
			return;
		} else if (!initialized) {
			// check if you have the positions are not.
			fyerOperations = new FyerOperations(pool.getFyersClasss());
			fyerOperations.populateLivePositions();			
			startTime = System.currentTimeMillis();
			// if the sell price is not give in strategy.json, then take the current ltp as the start price.
			if(pool.symbolAndid.isEmpty() || pool.symbolAndid.get(pool.ceSymbol) == null || pool.symbolAndid.get(pool.peSymbol) == null) {
				System.out.println("Either of CE or PE position is not found in live positions, exiting.");
				pool.logToFileSystem("Either of CE or PE position is not found in live positions, exiting.");
				//System.exit(0);
			}
			if(start_ce == 0.0 || start_pe == 0.0) {
				System.out.println("\n initial sell price not found in strategy.json, taking current ltp as start price.");			
			start_ce = ce;
			start_pe = pe;
			}
			if(!pool.symbolAndid.isEmpty() && pool.symbolAndid.get(pool.ceSymbol) != null && pool.symbolAndid.get(pool.peSymbol) != null) {
			CE_POSITION_ID = pool.symbolAndid.get(pool.ceSymbol).id;
			PE_POSITION_ID = pool.symbolAndid.get(pool.peSymbol).id;
			} else {
				System.out.println("Either of CE or PE position is not found in live positions, exiting.");
				pool.logToFileSystem("Either of CE or PE position is not found in live positions, exiting.");
				//System.exit(0);
			}
			// Initialize the combined premium limit and start premium
			startPremium = start_ce + start_pe;
			combinedPremiumLimit = startPremium + pool.trailMargin + 5; // Set initial limit to start premium + 20			
			System.out.println("\nInitialized combined premium limit to: " + df.format(combinedPremiumLimit));
			System.out.println(" Premium (ce, pe)--> " + ce + ", " + pe + " (" + df.format(startPremium) + ")");
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
	
	
	private void stayAwayFromPE() {
		
		if (!exitDonePE && delta_PE < 0 && pool.ENABLE_TRADE) {			
			System.out.println("\n Premium for PE has increased by :" + df.format(delta_PE) + " Exitting the position."
					+ pool.peSymbol);
			// check that you are exitiong the right position.
			//String positionID = pool.symbolAndid.get(pool.peSymbol).id;

			if (PE_POSITION_ID == null) {
				//System.out.println("\n Position ID is null for " + pool.peSymbol + ", cannot exit position.");
				return;
			} 
			
			if (fyerOperations.exitPositionById(PE_POSITION_ID)) {
				exitDonePE = true;
				System.out.println(" Exit successful for PE: " + pool.peSymbol);
				bookedPrice = 0.0;
				lossBooked = 0.0;
			} else {
				System.out.println("Error exiting position for PE: " + pool.peSymbol);
			}
			System.out.println(pool.exitPositionMessage);
		}
		
	}
	
	
	private void stayAwayFromCE() {

		//System.out.println("\nMarket data symbol is :" + data.getSymbol() + " and " + pool.peSymbol);
		//String positionID = pool.symbolAndid.get(pool.ceSymbol).id;
		if (CE_POSITION_ID == null) {
			//System.out.println("\n Position ID is null for " + pool.peSymbol + ", cannot exit position.");
			return;
		} 
		
		if (fyerOperations.exitPositionById(CE_POSITION_ID)) {
			exitDoneCE = true;
			System.out.println(" Exit successful for CE: " + pool.ceSymbol);
		} else {
			System.out.println(" Error exiting position for CE: " + pool.peSymbol);
		}
		System.out.println(pool.exitPositionMessage);
	
	}

}
