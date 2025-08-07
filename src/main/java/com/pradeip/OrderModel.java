package com.pradeip;

import com.tts.in.model.PlaceOrderModel;
import com.tts.in.utilities.OrderType;
import com.tts.in.utilities.OrderValidity;
import com.tts.in.utilities.ProductType;
import com.tts.in.utilities.TransactionType;

public class OrderModel {


	/**
	 * This class is responsible for preparing order models for placing buy and sell
	 * orders.
	 */
	public static PlaceOrderModel prepareSellOrder(String symbol, int quantity) {
		PlaceOrderModel model = new PlaceOrderModel();
		model.Symbol = symbol;
		model.Qty = quantity;
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
		return model;

	}

	public PlaceOrderModel prepareBuyOrder(String symbol, int quantity) {
		PlaceOrderModel model = new PlaceOrderModel();
		model.Symbol = symbol;
		model.Qty = quantity;
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
		return model;

	}

}
