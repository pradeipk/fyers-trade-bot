package com.pradeip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class PositionDTO {
	JSONObject root = null;
	JSONArray netPositions = null;
	public String symbol;
	public double rbiRefRate;
	public double sellVal;
	public double sellAvg;
	public int cfBuyQty;
	public double buyAvg;
	public double netAvg;
	public int slNo;
	public double unrealized_profit;
	public int segment;
	public double buyVal;
	public String id;
	public String productType;
	public int side;
	public int qtyMulti_com;
	public int netQty;
	public String crossCurrency;
	public int dayBuyQty;
	public int daySellQty;
	public double ltp;
	public double realized_profit;
	public int sellQty;
	public String fyToken;
	public int cfSellQty;
	public int buyQty;
	public int qty;
	public int exchange;
	public double pl;
	
	public PositionDTO(JSONObject root) {
		this.root = root;
		netPositions = root.getJSONArray("netPositions");
		if(netPositions == null || netPositions.length() == 0) {
			System.out.println("No net positions found.");
			return;
		}
		ArrayList<String> positionIdList = new ArrayList<String>();

		// Loop through and print each symbol
		PositionDTO dto = null;
		for (int i = 0; i < netPositions.length(); i++) {
			JSONObject position = netPositions.getJSONObject(i);
			String symbol1 = position.getString("symbol");
			dto = new PositionDTO();
			dto.symbol = symbol;
			dto.rbiRefRate = position.getDouble("rbiRefRate");
			dto.sellVal = position.getDouble("sellVal");
			dto.sellAvg = position.getDouble("sellAvg");
			dto.cfBuyQty = position.getInt("cfBuyQty");
			dto.buyAvg = position.getDouble("buyAvg");
			dto.netAvg = position.getDouble("netAvg");
			dto.slNo = position.getInt("slNo");
			dto.unrealized_profit = position.getDouble("unrealized_profit");
			dto.segment = position.getInt("segment");
			dto.buyVal = position.getDouble("buyVal");
			dto.id = position.getString("id");
			positionIdList.add(dto.id);
			dto.productType = position.getString("productType");
			dto.side = position.getInt("side");
			dto.qtyMulti_com = position.getInt("qtyMulti_com");
			dto.netQty = position.getInt("netQty");
			dto.crossCurrency = position.getString("crossCurrency");
			dto.dayBuyQty = position.getInt("dayBuyQty");
			dto.daySellQty = position.getInt("daySellQty");
			dto.ltp = position.getDouble("ltp");
			dto.realized_profit = position.getDouble("realized_profit");
			dto.sellQty = position.getInt("sellQty");
			dto.fyToken = position.getString("fyToken");
			dto.cfSellQty = position.getInt("cfSellQty");
			dto.buyQty = position.getInt("buyQty");
			dto.qty = position.getInt("qty");
			dto.exchange = position.getInt("exchange");
			dto.pl = position.getDouble("pl");
			InitializeApp.pool.positionDTOList.add(dto);
			InitializeApp.pool.postionIds.add(dto.id);
			InitializeApp.pool.symbolAndid.put(symbol1, dto.id);
		}
	}
	
	public PositionDTO() {
	}

}
