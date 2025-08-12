package com.pradeip.dto;

public class MarketData {

	private String vol_traded_today;
	private String symbol;
	private String ask_size;
	private String ch;
	private String high_price;
	private String exch_feed_time;
	private double ltp;
	private String chp;
	private String type;
	private String avg_trade_price;
	private String ask_price;
	private String tot_sell_qty;
	private String bid_size;
	private String last_traded_qty;
	private String low_price;
	private String open_price;
	private String tot_buy_qty;
	private String turnover;
	private String last_traded_time;
	private String bid_price;
	private String prev_close_price;

	// Getters and Setters

	public String getVol_traded_today() {
		return vol_traded_today;
	}

	public void setVol_traded_today(String vol_traded_today) {
		this.vol_traded_today = vol_traded_today;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getAsk_size() {
		return ask_size;
	}

	public void setAsk_size(String ask_size) {
		this.ask_size = ask_size;
	}

	public String getCh() {
		return ch;
	}

	public void setCh(String ch) {
		this.ch = ch;
	}

	public String getHigh_price() {
		return high_price;
	}

	public void setHigh_price(String high_price) {
		this.high_price = high_price;
	}

	public String getExch_feed_time() {
		return exch_feed_time;
	}

	public void setExch_feed_time(String exch_feed_time) {
		this.exch_feed_time = exch_feed_time;
	}

	public double getLtp() {
		return ltp;
	}

	public void setLtp(double ltp) {
		this.ltp = ltp;
	}

	public String getChp() {
		return chp;
	}

	public void setChp(String chp) {
		this.chp = chp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAvg_trade_price() {
		return avg_trade_price;
	}

	public void setAvg_trade_price(String avg_trade_price) {
		this.avg_trade_price = avg_trade_price;
	}

	public String getAsk_price() {
		return ask_price;
	}

	public void setAsk_price(String ask_price) {
		this.ask_price = ask_price;
	}

	public String getTot_sell_qty() {
		return tot_sell_qty;
	}

	public void setTot_sell_qty(String tot_sell_qty) {
		this.tot_sell_qty = tot_sell_qty;
	}

	public String getBid_size() {
		return bid_size;
	}

	public void setBid_size(String bid_size) {
		this.bid_size = bid_size;
	}

	public String getLast_traded_qty() {
		return last_traded_qty;
	}

	public void setLast_traded_qty(String last_traded_qty) {
		this.last_traded_qty = last_traded_qty;
	}

	public String getLow_price() {
		return low_price;
	}

	public void setLow_price(String low_price) {
		this.low_price = low_price;
	}

	public String getOpen_price() {
		return open_price;
	}

	public void setOpen_price(String open_price) {
		this.open_price = open_price;
	}

	public String getTot_buy_qty() {
		return tot_buy_qty;
	}

	public void setTot_buy_qty(String tot_buy_qty) {
		this.tot_buy_qty = tot_buy_qty;
	}

	public String getTurnover() {
		return turnover;
	}

	public void setTurnover(String turnover) {
		this.turnover = turnover;
	}

	public String getLast_traded_time() {
		return last_traded_time;
	}

	public void setLast_traded_time(String last_traded_time) {
		this.last_traded_time = last_traded_time;
	}

	public String getBid_price() {
		return bid_price;
	}

	public void setBid_price(String bid_price) {
		this.bid_price = bid_price;
	}

	public String getPrev_close_price() {
		return prev_close_price;
	}

	public void setPrev_close_price(String prev_close_price) {
		this.prev_close_price = prev_close_price;
	}

	public static MarketData fromJson(org.json.JSONObject json) {
		MarketData data = new MarketData();

		data.setVol_traded_today(json.optString("vol_traded_today"));
		data.setSymbol(json.optString("symbol"));
		data.setAsk_size(json.optString("ask_size"));
		data.setCh(json.optString("ch"));
		data.setHigh_price(json.optString("high_price"));
		data.setExch_feed_time(json.optString("exch_feed_time"));
		data.setLtp(Double.parseDouble(json.optString("ltp")));
		data.setChp(json.optString("chp"));
		data.setType(json.optString("type"));
		data.setAvg_trade_price(json.optString("avg_trade_price"));
		data.setAsk_price(json.optString("ask_price"));
		data.setTot_sell_qty(json.optString("tot_sell_qty"));
		data.setBid_size(json.optString("bid_size"));
		data.setLast_traded_qty(json.optString("last_traded_qty"));
		data.setLow_price(json.optString("low_price"));
		data.setOpen_price(json.optString("open_price"));
		data.setTot_buy_qty(json.optString("tot_buy_qty"));
		data.setTurnover(json.optString("turnover"));
		data.setLast_traded_time(json.optString("last_traded_time"));
		data.setBid_price(json.optString("bid_price"));
		data.setPrev_close_price(json.optString("prev_close_price"));

		return data;
	}
}
