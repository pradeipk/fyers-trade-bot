package com.pradeip;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LiveToken {
	static String refresh_token = null;

	public LiveToken(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	public static void main(String[] args) {
		LiveToken lt = new LiveToken(refresh_token);

		lt.getLiveToken();
	}

	String getLiveToken() {
		String liveToken = null;
		try {
			// Prepare URL and connection
			URL url = new URL("https://api-t1.fyers.in/api/v3/validate-refresh-token");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			// Prepare JSON body
			JSONObject jsonBody = new JSONObject();
			jsonBody.put("grant_type", "refresh_token");
			jsonBody.put("appIdHash", "22c8da69d9f404dfcf4e67148bf89ae147e1a0bd2fc71efdd71ac4258de19468");
			jsonBody.put("refresh_token", this.refresh_token);
			jsonBody.put("pin", "0304");

			// Send request
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = jsonBody.toString().getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			// Read response
			int code = conn.getResponseCode();
			BufferedReader br = new BufferedReader(
					new InputStreamReader((code == 200) ? conn.getInputStream() : conn.getErrorStream(), "utf-8"));
			StringBuilder response = new StringBuilder();
			String responseLine;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}

			String live_token = new JSONObject(response.toString()).getString("access_token");
			System.out.println("Live Token: " + live_token);
			//System.setProperty("Live_Token", live_token);
			//String value = System.getProperty("Live_Token");
			//System.out.println("Live_Token" + value);
			liveToken = live_token;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return liveToken;
	}

}