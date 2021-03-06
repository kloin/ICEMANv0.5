package com.breadcrumbs.gcm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.lucene.util.IOUtils;
import org.json.JSONObject;

/*
 * Written by Josiah Kendall. Copyright 2015 All Rights Reserved.
 * 
 * Class to Send messages to the Google Cloud Messenger service.
 * 
 * Used when an event happens such as comments, new photos etc and we need
 * to notify the user on their app.
 */
public class GcmSender {
	// The GCM Api Key for com.breadcrumbs app.
	private String API_KEY = "AIzaSyC3zYs82SyMaMlj2Xbss9b51FuoWJEVF-E";
	
	public void SendDownStreamCommentMessage(String message, String reciepent, String trailId, String crumbId) {
		// Checks to ensure nothing bad is going to happen.
				if (reciepent == null) {
					throw new NullPointerException("Method requires a reciepent. "
							+ "If you want to send a message to all clients, use /topics/global");
				}
				if (message == null) {
					throw new NullPointerException("Message was null. Please add a message to send to the client(s)");
				}
				try {
					// gcmData is the outer wrapper for the message, while data is the actual data we are sending.
					JSONObject gcmData = new JSONObject();
					JSONObject data = new JSONObject();
					data.put("message", message);
					data.put("trailId", trailId);
					data.put("type", "crumb");
					data.put("crumbId", crumbId);
					gcmData.put("to", reciepent);
					
					System.out.println("Message: "+ message);
					// Add our data to the outer wrapper
					gcmData.put("data", data);
					
					// Set up our http connection to google
					URL url = new URL("https://gcm-http.googleapis.com/gcm/send");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestProperty("Authorization", "key=" + API_KEY);
					conn.setRequestProperty("Content-Type", "application/json");
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					
					// Send the GCM message content
					OutputStream outputStream = conn.getOutputStream();
					outputStream.write(gcmData.toString().getBytes());
					
					// Read what gcm responded
					InputStream inputStream = conn.getInputStream();
					String respn = spark.utils.IOUtils.toString(inputStream); // Not sure about this - Spark?
					
					// Log the response - should be all go
					System.out.println(respn);			
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Probably failed to open Connection to URL");
				}		
	}
	// Send a downstream message to a client(s) via GCM. This is done via HTTP.
	public void SendDownStreamMessage(String message, String reciepent) {
		// Checks to ensure nothing bad is going to happen.
		if (reciepent == null) {
			throw new NullPointerException("Method requires a reciepent. "
					+ "If you want to send a message to all clients, use /topics/global");
		}
		if (message == null) {
			throw new NullPointerException("Message was null. Please add a message to send to the client(s)");
		}
		try {
			// gcmData is the outer wrapper for the message, while data is the actual data we are sending.
			JSONObject gcmData = new JSONObject();
			JSONObject data = new JSONObject();
			data.put("message", message);
			gcmData.put("to", reciepent);
			
			
			System.out.println("Message: "+ message);
			// Add our data to the outer wrapper
			gcmData.put("data", data);
			
			// Set up our http connection to google
			URL url = new URL("https://gcm-http.googleapis.com/gcm/send");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Authorization", "key=" + API_KEY);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			
			// Send the GCM message content
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(gcmData.toString().getBytes());
			
			// Read what gcm responded
			InputStream inputStream = conn.getInputStream();
			String respn = spark.utils.IOUtils.toString(inputStream); // Not sure about this - Spark?
			
			// Log the response - should be all go
			System.out.println(respn);			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Probably failed to open Connection to URL");
		}		
	}
}
