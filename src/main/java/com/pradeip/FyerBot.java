package com.pradeip;

import com.pradeip.strategy.AdjustingStraddle;
import com.pradeip.strategy.StraddleWithTrailingSL;
import com.pradeip.strategy.TrailSingleLeg;
import com.tts.in.websocket.FyersSocket;

public class FyerBot implements FyerBotInterface {

	private static InitializeApp pool;

	public static void main(String[] args) {

		pool = InitializeApp.pool;
		if (pool == null) {
			System.out.println("InitializeApp pool is null, exiting.");
			return;
		}
		
		FyersSocket fyersSocket = new FyersSocket(3);

		switch (pool.STRATEGY) {
		case STRATEGY_STRADDLE_WITH_TRAILING_SL:
			new StraddleWithTrailingSL().WebSocket(fyersSocket);
			System.out.println("\nStrategy : " + STRATEGY_STRADDLE_WITH_TRAILING_SL);
			break;
		case STRATEGY_ADJUSTING_STRADDLE:
			System.out.println("\nStrategy : " + STRATEGY_ADJUSTING_STRADDLE);
			new AdjustingStraddle().WebSocket(fyersSocket);
			break;
		case TRAILING_LEG:
			System.out.println("\nStrategy : " + TRAILING_LEG);
			new TrailSingleLeg().WebSocket(fyersSocket);
			break;
		default:
			System.out.println("\nNo valid strategy selected, exiting.");
			return;
		}
	}
}