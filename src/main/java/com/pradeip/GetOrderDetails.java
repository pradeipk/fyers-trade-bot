package com.pradeip;

import java.net.URI;
import java.net.http.*;
import java.util.concurrent.*;

public class GetOrderDetails {

	private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiZDoxIiwiZDoyIiwieDowIiwieDoxIiwieDoyIl0sImF0X2hhc2giOiJnQUFBQUFCb2xZM2ZYSFRkWDJLQWNBUXNIUkdLZDhlbmx2YzctcDZDdHFpek5xUzN1NncyRHZpbEFCUzdvX2M4bURCUjhILUhzSDJtR3JmLXE1RndrdVFEQlZkNzhaczJ0ZzdTSWUtQm1xMjJmb2EzZXdOcm43dz0iLCJkaXNwbGF5X25hbWUiOiIiLCJvbXMiOiJLMSIsImhzbV9rZXkiOiJhY2EzY2U5Y2MxYTIwOWQ3ZWM1NzA4OTU5NzRkODRkNWEyYzZmZGJkMjg2MzhkN2U4NzJkNzQwMyIsImlzRGRwaUVuYWJsZWQiOiJZIiwiaXNNdGZFbmFibGVkIjoiTiIsImZ5X2lkIjoiWFUwNDM4MiIsImFwcFR5cGUiOjEwMCwiZXhwIjoxNzU0Njk5NDAwLCJpYXQiOjE3NTQ2MzE2NDcsImlzcyI6ImFwaS5meWVycy5pbiIsIm5iZiI6MTc1NDYzMTY0Nywic3ViIjoiYWNjZXNzX3Rva2VuIn0.kNlho9WhN7D3lSDjsQR9B-mnW8_2Q46q9_Fk0tqtUqA";

	private static final HttpClient client = HttpClient.newHttpClient();

	public static void main(String[] args) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		Runnable pollingTask = () -> {
			fetchAndPrint("orders", "https://api.fyers.in/api/v2/orders");
			fetchAndPrint("positions", "https://api.fyers.in/api/v2/positions");
			fetchAndPrint("tradebook", "https://api.fyers.in/api/v2/tradebook");
		};

		scheduler.scheduleAtFixedRate(pollingTask, 0, 5, TimeUnit.SECONDS);
	}

	private static void fetchAndPrint(String label, String url) {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
					.header("Authorization", "Bearer " + ACCESS_TOKEN).GET().build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			System.out.println("=== " + label.toUpperCase() + " ===");
			System.out.println(response.body());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}