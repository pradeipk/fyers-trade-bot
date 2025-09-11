package com.pradeip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.json.JSONObject;
import com.tts.in.model.FyersClass;

public class AuthCode {

	static FyersClass fyersClass = null;
	static String initDirectoryPath = System.getenv("FYER_SCRIPT_PATH");
	static String appid = null;
	static String redirectURI = null;
	static boolean initSucceeded = false;

	static {
		init(initDirectoryPath);
	}

	public static void main(String[] args) {
		if (!initSucceeded) {
			System.out.println("Initialization failed. Exiting application.");
			return;
		}
		FyersClass fyersClass = FyersClass.getInstance();
		fyersClass.clientId = appid;
		AuthCode app = new AuthCode();
		app.getGenerateCode(redirectURI, fyersClass);
	}

	public void getGenerateCode(String redirectURI, FyersClass fyersClass) {
		fyersClass.GenerateCode(redirectURI);
	}

	private static void init(String initDirectoryPath) {
		JSONObject initData = null;
		try {
			initData = new JSONObject(Files.readString(new File(initDirectoryPath, "init.json").toPath()));
			if (initData.has("redirectURI")) {
				appid = initData.getString("appid");
				redirectURI = initData.getString("redirectURI");
			} else {
				System.out.println("redirectURI not found in init.json");
			}

			initSucceeded = true;
		} catch (IOException e) {
			System.out.println("Error reading init.json: " + e.getMessage());
			initSucceeded = false;
			return;
		}
	}
}
