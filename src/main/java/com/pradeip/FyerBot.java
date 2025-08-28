package com.pradeip;

import com.pradeip.strategy.AdjustingStraddle;
import com.pradeip.strategy.StraddleWithTrailingSL;

public class FyerBot implements FyerBotInterface {

	private static InitializeApp pool;
	static FyerOperations fyerOperations;

	public static void main(String[] args) {

		pool = InitializeApp.pool;
		if (pool == null) {
			System.out.println("InitializeApp pool is null, exiting.");
			return;
		}

		switch (pool.STRATEGY) {
		case STRATEGY_STRADDLE_WITH_TRAILING_SL:
			StraddleWithTrailingSL straddleWithTrailingSL = new StraddleWithTrailingSL();
			straddleWithTrailingSL.WebSocket();
			System.out.println("Strategy: " + STRATEGY_STRADDLE_WITH_TRAILING_SL);
			break;
		case STRATEGY_ADJUSTING_STRADDLE:
			System.out.println("Strategy: " + STRATEGY_ADJUSTING_STRADDLE);
			new AdjustingStraddle().WebSocket();
			break;
		default:
			System.out.println("No valid strategy selected, exiting.");
			return;
		}
	}
}