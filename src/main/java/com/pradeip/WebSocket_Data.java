package com.pradeip;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.tts.in.model.FyersClass;
import com.tts.in.model.PlaceOrderModel;
import com.tts.in.utilities.OrderType;
import com.tts.in.utilities.OrderValidity;
import com.tts.in.utilities.ProductType;
import com.tts.in.utilities.TransactionType;
import com.tts.in.websocket.FyersSocket;
import com.tts.in.websocket.FyersSocketDelegate;

import in.tts.hsjavalib.ChannelModes;

public class WebSocket_Data implements FyersSocketDelegate {

	static FyersClass fyersClass = null;

	public static void main(String[] args) {
		String APPID = "T39EREBI76-100";
		String LiveToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiZDoxIiwiZDoyIiwieDowIiwieDoxIiwieDoyIl0sImF0X2hhc2giOiJnQUFBQUFCb2t4ME4xenNIRGZqaDYwZV9SVlRxNGNEYWNLaWxQM1dua3FPeko3ZHlqSzZlY0ZBcFNNV1hhU1o2U3BHWGRoTm5Xb1FnRzVBRXU4bTlzTm5rRUlDWGZ4bzRyTmN5dWgtMHAweDRyYUE4c0VVUzRHbz0iLCJkaXNwbGF5X25hbWUiOiIiLCJvbXMiOiJLMSIsImhzbV9rZXkiOiJhY2EzY2U5Y2MxYTIwOWQ3ZWM1NzA4OTU5NzRkODRkNWEyYzZmZGJkMjg2MzhkN2U4NzJkNzQwMyIsImlzRGRwaUVuYWJsZWQiOiJZIiwiaXNNdGZFbmFibGVkIjoiTiIsImZ5X2lkIjoiWFUwNDM4MiIsImFwcFR5cGUiOjEwMCwiZXhwIjoxNzU0NTI2NjAwLCJpYXQiOjE3NTQ0NzE2OTMsImlzcyI6ImFwaS5meWVycy5pbiIsIm5iZiI6MTc1NDQ3MTY5Mywic3ViIjoiYWNjZXNzX3Rva2VuIn0.k-hD1yPxWERzSrZAs_u6tNi2cSA0vRFLGwKVSCkz-Qk";

		
		fyersClass = FyersClass.getInstance();
		fyersClass.clientId = APPID;
		fyersClass.accessToken = LiveToken;
		WebSocket_Data app = new WebSocket_Data();
		app.WebSocket();
	}

	public void WebSocket() {
		List<String> scripList = new ArrayList<>();
		scripList.add("NIFTY50-INDEX");
		FyersSocket fyersSocket = new FyersSocket(3);
		fyersSocket.webSocketDelegate = this;
		fyersSocket.ConnectHSM(ChannelModes.LITE);
		fyersSocket.SubscribeData(scripList);
	}

	@Override
	public void OnIndex(JSONObject index) {
		// Example: {"symbol": "NSE:NIFTY50-INDEX", "last_traded_price": 25000, ...}
		System.out.println("On Index: " + index);
		double niftyPrice = index.getDouble("last_traded_price"); // adjust key as per actual data
		if (niftyPrice < 25000) {
			// Construct order details for the desired strike price
			JSONObject orderParams = new JSONObject();
			orderParams.put("symbol", "NSE:NIFTY24JUN18000CE"); // example
			orderParams.put("qty", 75);
			orderParams.put("type", "BUY");
			// Create and configure the PlaceOrderModel

			PlaceOrderModel model = new PlaceOrderModel();
			model.Symbol = "NSE:NIFTY24JUN18000CE";
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
			fyersClass.PlaceOrder(model); // replace with actual method
		}
	}

	@Override
	public void OnScrips(JSONObject scrips) {
		System.out.println("On Scrips: " + scrips);
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

}