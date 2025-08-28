package com.pradeip;

import java.util.List;

import org.json.JSONObject;

import com.tts.in.model.FyersClass;
import com.tts.in.model.PlaceOrderModel;
import com.tts.in.utilities.Tuple;

public class FyerOperations {
	FyersClass fyersClass = null;

	public FyerOperations(FyersClass fyerClass) {
		this.fyersClass = fyerClass;
	}

	public boolean exitPosition(List<String> positionIDs) {
		// by sending empty list all posiion will be closed
		if(positionIDs == null || positionIDs.isEmpty()) {
			System.out.println("No positions to exit.");
			return false;
		}
		Tuple<JSONObject, JSONObject> jObject = fyersClass.ExitPositions(positionIDs);
		if (jObject.Item1() != null) {
			System.out.println("Position Message: " + jObject.Item1());
			if (jObject.Item1().getInt("code") == 201) {
				InitializeApp.pool.exitPositionMessage = jObject.Item1().getString("message");
				return true;
			}
		} else {
			System.out.println("Position Error: " + jObject.Item2());
		}
		return false;
	}

	public void getAllOrders(List<String> positionIDs) {
		fyersClass.GetAllOrders();
	}

	public PositionDTO getAllPositions() {
		Tuple<JSONObject, JSONObject> positionTuple = fyersClass.GetPositions();
		PositionDTO positionDTO = null;
		if (positionTuple.Item1() != null) {
			positionDTO = new PositionDTO(positionTuple.Item1());
			System.out.println("Position: " + positionTuple.Item1());
		} else {
			System.out.println("Position Error: " + positionTuple.Item2());
		}
		return positionDTO;
	}	
	
	public PositionDTO getPositionsBySymbols() {
		Tuple<JSONObject, JSONObject> positionTuple = fyersClass.GetPositions();
		PositionDTO positionDTO = null;
		if (positionTuple.Item1() != null) {
			positionDTO = new PositionDTO(positionTuple.Item1());
			System.out.println("Position: " + positionTuple.Item1());
		} else {
			System.out.println("Position Error: " + positionTuple.Item2());
		}
		return positionDTO;
	}

	public void GetProfile(FyersClass fyersClass) {
		Tuple<JSONObject, JSONObject> ProfileResponseTuple = fyersClass.GetProfile();

		if (ProfileResponseTuple.Item2() == null) {
			System.out.println("Profile: " + ProfileResponseTuple.Item1());
		} else {
			System.out.println("Profile Error: " + ProfileResponseTuple.Item2());
		}
	}

	public void GetFunds(FyersClass fyersClass) {
		Tuple<JSONObject, JSONObject> ResponseTuple = fyersClass.GetFunds();

		if (ResponseTuple.Item2() == null) {
			System.out.println("Fund: " + ResponseTuple.Item1());
		} else {
			System.out.println("Fund Error: " + ResponseTuple.Item2());
		}
	}

	public void GetHoldings(FyersClass fyersClass) {
		Tuple<JSONObject, JSONObject> holdingTuple = fyersClass.GetHoldings();
		if (holdingTuple.Item2() == null) {
			System.out.println("Holdings: " + holdingTuple.Item1());
		} else {
			System.out.println("Holdings Error: " + holdingTuple.Item2());
		}
	}

	public void GetLogoutValidation(FyersClass fyersClass) {
		//JSONObject jsonObject = fyersClass.LogoutValidation();
		//System.out.println(jsonObject);
	}

	public void GetMarketDepth(FyersClass fyersClass) {
		Tuple<JSONObject, JSONObject> ResponseTuple = fyersClass.GetMarketDepth("NSE:TCS-EQ", 0);

		if (ResponseTuple.Item2() == null) {
			System.out.println("Market Depth: " + ResponseTuple.Item1());
		} else {
			System.out.println("Market Depth Error:" + ResponseTuple.Item2());
		}
	}

	public void GetOptionChain(FyersClass fyersClass) {
		String symbol = "NSE:TCS-EQ";
		int strikeCount = 1;
		String timestamp = "";

		Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetOptionChain(symbol, strikeCount, timestamp);

		if (stockTuple.Item2() == null) {
			System.out.println("OptionChain:" + stockTuple.Item1());
		} else {
			System.out.println("OptionChain Error: " + stockTuple.Item2());
		}

	}
	
	
	public void Sell(String symbol, int quantity) {

		PlaceOrderModel pom = FyerOrderModel.prepareSellOrder(symbol, quantity);
		Tuple<JSONObject, JSONObject> sellTuple = fyersClass.PlaceOrder(pom);

		if (sellTuple.Item1() == null) {
			System.out.println("OptionChain:" + sellTuple.Item1());
		} else {
			System.out.println("OptionChain Error: " + sellTuple.Item2());
		}
	}
	
	
	public boolean exitFromAllPositions(List<String> positionIds) {
		if (positionIds != null) {
			Tuple<JSONObject, JSONObject> tuple = fyersClass.ExitPositions(positionIds);
			
			if (tuple.Item1() != null && tuple.Item1().getInt("code") == 201) {
				System.out.println(" Exit successful for all positions.");
				return true;
			} else {
				System.out.println("Error exiting all positions: " + tuple.Item2());
				return false;
			}
		} else {
			System.out.println("Error exiting position for PE: " + positionIds.get(0));
			return false;
		}
	}

}
