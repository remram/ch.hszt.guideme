package ch.hszt.controller;

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

import ch.hszt.model.Place;

/**
 * @author egon
 *
 */
public class GooglePlaceRequest {
	private Place place;
	private String urlString;
	private int radius = 60;

	/**
	 * Overwrited constructor
	 * @param latitude
	 * @param longitude
	 */
	public GooglePlaceRequest(double latitude, double longitude) {
		urlString = buildUrl(latitude, longitude);
	}

	/**
	 * create the url which is requierd to get the search results
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	private String buildUrl(double latitude, double longitude) {
		String clientId = PlacesKeys.getClientID();
		String API_KEY = PlacesKeys.getApiKey();
		String sensor = "false";
		String type = "establishment";
		String url = "https://maps.googleapis.com/maps/api/place/search/json?" +
				"location=" + latitude + "," + longitude +
				"&radius=" + radius +
				"&client=" + clientId +
				"&type=" + type +
				"&sensor=" + sensor +
				"&key=" + API_KEY;
		return url;
	}

	/**
	 * search places and return an ArrayList with found places
	 * @return
	 */
	public ArrayList<Place> search() {
		ArrayList<Place> placeList = new  ArrayList<Place>();
		try {
			URL url = new URL(urlString);
			URLConnection urlConnection = url.openConnection();
			StringBuilder sb = new StringBuilder();
			Scanner sc = new Scanner(new InputStreamReader(urlConnection.getInputStream()));
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}
			sc.close();
			String json = sb.toString();

			JSONObject completeJSONObj = new JSONObject(json);
			JSONArray jsonArray = completeJSONObj.getJSONArray("results");
			JSONObject jsonObject = jsonArray.getJSONObject(0);

			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObject = jsonArray.getJSONObject(i);
				place = new Place();
				place.setName(jsonObject.getString("name"));
				place.setVicinity(jsonObject.getString("vicinity"));
				place.setLatitude(jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
				place.setLongitude(jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
				placeList.add(place);				
			}

			return placeList;	
			
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (JSONException e) {
			return null;
		}
	}
}