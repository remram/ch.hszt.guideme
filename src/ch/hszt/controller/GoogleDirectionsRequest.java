package ch.hszt.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author egon
 *
 */
public class GoogleDirectionsRequest {
	private int distance;
	private double fromLat;
	private double fromLng;
	private double toLat;
	private double toLng;
	
	public GoogleDirectionsRequest(double fromLat, double fromLng, double toLat, double toLng) {
		this.fromLat = fromLat;
		this.fromLng = fromLng;
		this.toLat = toLat;
		this.toLng = toLng;
	}
	
	/**
	 * GoogleDirectionsSearch format the return json-string into json objects
	 * @return
	 */
	public ArrayList<String> searchGoogleDirections() {
		ArrayList<String> polPoints = new  ArrayList<String>();		
		try {
			URL url = new URL(buildGoogleDirectionsUrl());
			URLConnection urlConnection = url.openConnection();
			StringBuilder sb = new StringBuilder();
			Scanner sc = new Scanner(new InputStreamReader(urlConnection.getInputStream()));
			while (sc.hasNext()) {
				sb.append(sc.nextLine()+"\n");
			}
			sc.close();
			String json = sb.toString();			
			
			JSONObject firstJsonObject = new JSONObject(json);
			JSONArray jsonArray = firstJsonObject.getJSONArray("routes");
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			int n = jsonArray.length();
			int m = jsonObject.getJSONArray("legs").getJSONObject(0).getJSONArray("steps").length();
			
			for (int i = 0; i < n; i++) {
				jsonObject = jsonArray.getJSONObject(i);
				distance = jsonObject.getJSONArray("legs").getJSONObject(i).getJSONObject("distance").getInt("value");
				for (int j = 0; j < m; j++) {
					String polygonePoints = jsonObject.getJSONArray("legs").
							getJSONObject(i).
							getJSONArray("steps").
							getJSONObject(j).
							getJSONObject("polyline").getString("points");
					polPoints.add(polygonePoints);
				}
			}	
			return polPoints;
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * url for GoogleDirections search
	 * @return
	 */
	private String buildGoogleDirectionsUrl() {
		String mode = "walking";
		String sensor = "false";
		String urlString = "http://maps.google.ch/maps/api/directions/json?" +
				"origin=" + this.fromLat + "," + this.fromLng +
				"&destination=" + this.toLat + "," + this.toLng +
				"&mode=" + mode +
				"&sensor=" + sensor;
		return urlString;
	}
	
	public int getDistance() {
		return distance;
	}
}