package com.pradeip;

import org.json.JSONObject;
import com.tts.in.model.FyersClass;

public class RefreshToken {

	static String APPID = "";
	static String appHashId = "";

	public static void main(String[] args) {

		String authCode = "";

		FyersClass fyersClass = FyersClass.getInstance();
		fyersClass.clientId = APPID;
		RefreshToken app = new RefreshToken();
		app.GetGenerateToken(authCode, fyersClass);
	}

	public void GetGenerateToken(String code, FyersClass fyersClass) {

		JSONObject jsonObject = fyersClass.GenerateToken(code, appHashId);
		//System.out.println(jsonObject);

		if (jsonObject != null && jsonObject.has("refresh_token")) {
			String refresh_token = jsonObject.getString("refresh_token");
			//System.out.println("Refresh Token: " + refresh_token);
			LiveToken liveToken = new LiveToken(refresh_token);
			liveToken.getLiveToken();

		} else {
			System.out.println("Failed to generate access token. Response: " + jsonObject);
		}

	}
}
// ----------------------------------------------------------------------------------
// Sample Success Response
// ----------------------------------------------------------------------------------

// {
// "access_token": "XXXXXXXXXXXXXXXXXXXXXXXXXXX",
// "refresh_token":
// "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhcGkuZnllcnMuaW4iLCJpYXQiOjE3Mjc0MTYyNDIsImV4cCI6MTcyODY5MzA0MiwibmJmIjoxNzI3NDE2MjQyLCJhdWQiOlsieDowIiwieDoxIiwieDoyIiwiZDoxIiwiZDoyIiwieDoxIiwieDowIl0sInN1YiI6InJlZnJlc2hfdG9rZW4iLCJhdF9oYXNoIjoiZ0FBQUFBQm05a2V5UmtGTllkY0FmMGFFa2lNZXJRWHpfLTFpMUU4V3RtcldnMExXeTZXREgzNW44RHZ2cmJuTExnWmtBcl9RcHhSbVhiaEgtWUJRbTF6Ym9ZeEhTVkJzWlQ0T1U4elkwazJTU1Y5ZWlBTkRTLWc9IiwiZGlzcGxheV9uYW1lIjoiS1VNQVIgS0lTSE9SRSBLVU1BUiIsIm9tcyI6IksxIiwiaHNtX2tleSI6IjI0OTJlMjBhNWI0ZDAwZGZiMTg0OGQ4MjcxMWRmZjJjODFmMzU3OTJlZjQzYzJiOGExNDVkMmZjIiwiZnlfaWQiOiJZSzA0MzkxIiwiYXBwVHlwZSI6MTAwLCJwb2FfZmxhZyI6Ik4ifQ.eZGgaSXZi01g4Gw4VxOue64uZXYOMJVokO3AASHxBxs",
// "RESPONSE_MESSAGE": "SUCCESS"
// }
