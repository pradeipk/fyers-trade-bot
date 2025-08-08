package com.pradeip;

import org.json.JSONObject;
import com.tts.in.model.FyersClass;
import com.tts.in.utilities.Tuple;

public class FyerOrder {

	public static void main(String[] args) {
		//String clientID = "AAAAAAAA-100";
		//String LiveToken = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";

		//FyersClass fyersClass = FyersClass.getInstance();
		//fyersClass.clientId = clientID;
		//fyersClass.accessToken = LiveToken;
		//App app = new App();
		//app.GetOrders(fyersClass);
	}

	public static void getOrders(FyersClass order) {
		Tuple<JSONObject, JSONObject> orderList = order.GetAllOrders();
		if (orderList.Item2() == null) {
			System.out.println("Orders :" + orderList.Item1());
		} else {
			System.out.println("Orders Error:" + orderList.Item2());
		}
	}
}
/*------------------------------------------------------------------------------------------------------------------------------------------
Sample Success Response
------------------------------------------------------------------------------------------------------------------------------------------Response structure:
"Orders":{
  "code":200,
  "s":"ok",
  "orderBook":[
      {
        "remainingQuantity":0,
        "symbol":"NSE:UCOBANK-EQ",
        "lp":49.31,
        "description":"UCO BANK",
        "instrument":0,
        "source":"W",
        "type":2,
        "slNo":2,
        "offlineOrder":false,
        "segment":10,
        "ex_sym":"UCOBANK",
        "id":"24092700122266",
        "pan":"LBGPK9804E",
        "productType":"INTRADAY",
        "orderDateTime":"27-Sep-2024 10:33:09",
        "side":1,
        "clientId":"YK04391",
        "limitPrice":49.3,
        "ch":0.76,
        "tradedPrice":49.3,
        "disclosedQty":0,
        "chp":1.565396498455201,
        "message":"",
        "fyToken":"101000000011223",
        "stopPrice":0,
        "qty":1,
        "orderValidity":"DAY",
        "exchOrdId":"1300000017598717",
        "exchange":10,
        "filledQty":1,
        "orderTag":"2:Untagged",
        "orderNumStatus":"24092700122266:2",
        "status":2
      }
  ],
  "message":""
}*/
