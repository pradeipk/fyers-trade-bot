package com.pradeip;

import org.json.JSONObject;
import com.tts.in.model.FyersClass;
import com.tts.in.model.PlaceOrderModel;
import com.tts.in.utilities.OrderType;
import com.tts.in.utilities.OrderValidity;
import com.tts.in.utilities.ProductType;
import com.tts.in.utilities.TransactionType;
import com.tts.in.utilities.Tuple;

public class PlaceFyerOrder {

	public static void main(String[] args) {
		String clientID = "AAAAAAAA-100";
		String LiveToken = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";

		FyersClass fyersClass = FyersClass.getInstance();
		fyersClass.clientId = clientID;
		fyersClass.accessToken = LiveToken;
		// App app = new App();
		// app.PlaceOrder(fyersClass);
	}

	public void PlaceOrder(FyersClass fyersClass) {
		PlaceOrderModel model = new PlaceOrderModel();
		model.Symbol = "NSE:IDEA-EQ";
		model.Qty = 1;
		model.OrderType = OrderType.MarketOrder.getDescription();
		model.Side = TransactionType.Buy.getValue();
		model.ProductType = ProductType.CNC;
		model.LimitPrice = 0;
		model.StopPrice = 0;
		model.OrderValidity = OrderValidity.DAY;
		model.DisclosedQty = 0;
		model.OffLineOrder = false;
		model.StopLoss = 0;
		model.TakeProfit = 0;
		model.OrderTag = "PlacingOrderWithTag2";

		Tuple<JSONObject, JSONObject> ResponseTuple = fyersClass.PlaceOrder(model);
		if (ResponseTuple.Item2() == null) {
			System.out.println("Order ID: " + ResponseTuple.Item1());
		} else {
			System.out.println("Place order Message : " + ResponseTuple.Item2());
		}

	}
	/*------------------------------------------------------------------------------------------------------------------------------------------
	Sample Success Response 
	------------------------------------------------------------------------------------------------------------------------------------------
	    "Order ID":{
	       "code":1101,
	       "s":"ok",
	       "id":"24092700227212",
	       "message":"Successfully placed order"
	     }*/
}