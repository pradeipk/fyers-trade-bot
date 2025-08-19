package com.pradeip;

public interface FyerBotInterface {

	public String EXCHANGE = "NSE";
	public String YEAR = "25";
	public String MONTH_AUG = "08";
	public String MONTH_SEP = "09";
	public String MONTH_OCT = "10";
	public String STRATEGY_MONITOR_SL_AND_ACTION = "sell_order_placed";
	public String STRATEGY_COMBINED_PREMIUM_ALARMS_AND_ACTION = "combined_premium";
	public String STRATEGY_ADJUSTING_STRADDLE = "adjusting_straddle";
	public String STRATEGY_SL = "sell_order_placed";
	public String pin = null;
	public String NSE_NIFTY = "NSE:NIFTY50-INDEX";
	
	
	public enum STRATEGY {
		GAURD_YOUR_POSITION, 
		COMBINED_PREMIUM_ALARMS, 
		MONITOR_SL_AND_ACTION, 
		STRATEGY_ADJUSTING_STRADDLE,
		SL
	}

}
