package com.pradeip;

import com.pradeip.strategy.CombinedPremiuimAlarmsAndAction;
import com.pradeip.strategy.GaurdYourPosition;

public class FyerBot implements FyerBotInterface {

	private static InitializeApp pool;
	static FyerOperations fyerOperations;
	CombinedPremiuimAlarmsAndAction combinedPremiuimAlarmsAndAction = null;
	GaurdYourPosition gaurdYourPosition = null;

	public static void main(String[] args) {

		pool = InitializeApp.pool;
		if (pool == null) {
			System.out.println("InitializeApp pool is null, exiting.");
			return;
		}

		switch (pool.STRATEGY) {
		case STRATEGY_MONITOR_SL_AND_ACTION:
			GaurdYourPosition gaurdYourPosition = new GaurdYourPosition(pool);
			gaurdYourPosition.WebSocket();
			System.out.println("Strategy: " + STRATEGY.MONITOR_SL_AND_ACTION);
			break;
		case STRATEGY_COMBINED_PREMIUM_ALARMS_AND_ACTION:
			System.out.println("Strategy: " + STRATEGY.COMBINED_PREMIUM_ALARMS);
			new CombinedPremiuimAlarmsAndAction().WebSocket();
			break;
		default:
			System.out.println("No valid strategy selected, exiting.");
			return;
		}
	}
}