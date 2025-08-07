package com.pradeip;

import org.json.JSONObject;
import com.tts.in.model.FyersClass;

public class RefreshToken {

	static String APPID = "T39EREBI76-100";
	static String appHashId = "22c8da69d9f404dfcf4e67148bf89ae147e1a0bd2fc71efdd71ac4258de19468";

	public static void main(String[] args) {

		String authCode = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBfaWQiOiJUMzlFUkVCSTc2IiwidXVpZCI6ImQ2YjUxZjJhMDdmNDQ4ODBhNTdhNjU0NTdlZGU4OTFmIiwiaXBBZGRyIjoiIiwibm9uY2UiOiIiLCJzY29wZSI6IiIsImRpc3BsYXlfbmFtZSI6IlhVMDQzODIiLCJvbXMiOiJLMSIsImhzbV9rZXkiOiJhY2EzY2U5Y2MxYTIwOWQ3ZWM1NzA4OTU5NzRkODRkNWEyYzZmZGJkMjg2MzhkN2U4NzJkNzQwMyIsImlzRGRwaUVuYWJsZWQiOiJZIiwiaXNNdGZFbmFibGVkIjoiTiIsImF1ZCI6IltcImQ6MVwiLFwiZDoyXCIsXCJ4OjBcIixcIng6MVwiLFwieDoyXCJdIiwiZXhwIjoxNzU0NTg4NDU4LCJpYXQiOjE3NTQ1NTg0NTgsImlzcyI6ImFwaS5sb2dpbi5meWVycy5pbiIsIm5iZiI6MTc1NDU1ODQ1OCwic3ViIjoiYXV0aF9jb2RlIn0.toB6tn6PvhSIGOpzFrcbZNxEUSzdUpd7iHc0xrRqSQk";

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
