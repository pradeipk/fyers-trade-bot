package com.pradeip;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import com.tts.in.model.FyersClass;
import com.tts.in.utilities.Tuple;

public class ExitPositions {

	public static void main(String[] args) {
		String clientID = "AAAAAAAA-100";
		String LiveToken = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";

		FyersClass fyersClass = FyersClass.getInstance();
		fyersClass.clientId = clientID;
		fyersClass.accessToken = LiveToken;
		ExitPositions app = new ExitPositions();
		app.ExitPosition(fyersClass);
	}

	public void ExitPosition(FyersClass positions) {
		List<String> positionIDs = new ArrayList<>();
		// by sending empty list all posiion will be closed
		Tuple<JSONObject, JSONObject> jObject = positions.ExitPositions(positionIDs);
		if (jObject.Item2() == null) {
			System.out.println("Position Message: " + jObject.Item1());
		} else {
			System.out.println("Position Error: " + jObject.Item2());
		}
	}
}

/*------------------------------------------------------------------------------------------------------------------------------------------
Sample Success Response 
------------------------------------------------------------------------------------------------------------------------------------------
"Position Message":{
  "code":200,
  "s":"ok",
  "message":"Position NSE:UCOBANK-EQ-INTRADAY is closed."
}    */
