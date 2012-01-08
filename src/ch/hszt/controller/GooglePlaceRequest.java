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
import ch.hszt.model.VirtualPoint;

/**
 * @author egon
 *
 */
public class GooglePlaceRequest {
	private Place place;
	private String urlString;
	private int radius = 60;
	private double latitude;
	private double longitude;
	private boolean overQueryLimit = true;
	private int totalOverQueryCounter = 0;
	private int totalOkCounter = 0;
	private ArrayList<Place> placeList = new ArrayList<Place>();
	
	/**
	 * Overwrited constructor
	 * @param latitude
	 * @param longitude
	 */
	public GooglePlaceRequest(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * create the url which is requierd to get the search results
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	private String buildGooglePlaceSearchUrl(double lat, double lng) {
		String clientId = PlacesKeys.getClientID();
		String API_KEY = PlacesKeys.getApiKey();
		String sensor = "false";
		String type = "establishment";
		String url = "https://maps.googleapis.com/maps/api/place/search/json?" +
				"location=" + lat + "," + lng +
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
	public ArrayList<Place> search(double lat, double lng) {
		ArrayList<Place> pl = new  ArrayList<Place>();
		
		do {
			try {
				URL url = new URL(buildGooglePlaceSearchUrl(lat, lng));
				URLConnection urlConnection = url.openConnection();
				StringBuilder sb = new StringBuilder();
				Scanner sc = new Scanner(new InputStreamReader(urlConnection.getInputStream()));
				while (sc.hasNext()) {
					sb.append(sc.nextLine());
				}
				sc.close();
				String json = sb.toString();

				JSONObject completeJSONObj = new JSONObject(json);
				String status = completeJSONObj.getString("status");
				
				
				if (status.equals("OVER_QUERY_LIMIT")) {
					overQueryLimit = true;
					totalOverQueryCounter++;
				}
				
				if (status.equals("OK")) {
					overQueryLimit = false;
					totalOkCounter++;
					JSONArray jsonArray = completeJSONObj.getJSONArray("results");
					JSONObject jsonObject = jsonArray.getJSONObject(0);

					for (int i = 0; i < jsonArray.length(); i++) {
						jsonObject = jsonArray.getJSONObject(i);
						if (jsonObject.getString("vicinity").equals("Switzerland")) {
							place = new Place();
							place.setName(jsonObject.getString("name"));
							place.setVicinity(jsonObject.getString("vicinity"));	
							place.setLatitude(jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
							place.setLongitude(jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
							pl.add(place);
						}
					}
				}


			} catch (MalformedURLException e) {
			} catch (IOException e) {
			} catch (JSONException e) {
			}
			return pl;
		} while (overQueryLimit);
	}
	
	/**
	 * search places and return an ArrayList with all found places
	 * @return
	 */
	public ArrayList<Place> getList() {
		ArrayList<VirtualPoint> vpl = createVirtualPointList(latitude, longitude);
		ArrayList<Place> virtualPointList = new ArrayList<Place>();
		System.out.println("calculate & search. Please wait...");
		int placeCounter = 0;
		do {
			if (placeCounter < 61) {
				virtualPointList = search(vpl.get(placeCounter).getLatitude(), vpl.get(placeCounter).getLongitude());	
				for (Place place : virtualPointList) {	
					placeList.add(place);
				}
			}
			placeCounter ++;
		} while ( placeCounter < 61);
		
		System.out.println("total OVER_QUERY_LIMIT:" + totalOverQueryCounter + "\ttotal OK: " + totalOkCounter);
		System.out.println("Haltestellen gefunden:");
		for (Place place : placeList) {	
			System.out.println("--> "+ place.getName() + " " + place.getVicinity());
		}
		return placeList;
	}

	/**
	 * search places and return an ArrayList with found places
	 * @return
	 */
	public ArrayList<Place> searchGooglePlaces() {
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * create the virtal point
	 * @param latitude
	 * @param longitude
	 * @param times
	 * @param circle
	 * @param counter
	 * @return
	 */
	public VirtualPoint createNewVirtualPoint(double latitude, double longitude, int times, int circle, int counter) {
		VirtualPoint virtualPoint = null;
		double anlge = 2 * Math.PI/circle;
		double distance = times * Math.sqrt(2)* radius/1E5;
		double lat = latitude + distance * Math.cos(counter * anlge);
		double lng = longitude + distance * Math.sin(counter * anlge);
		lat = Math.round(lat*1000000) / 1000000.0;
		lng = Math.round(lng*1000000) / 1000000.0;
		virtualPoint = new VirtualPoint(lat,lng);
		return virtualPoint;
	}
	
	/**
	 * create a list of 61 virtual points 
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public ArrayList<VirtualPoint> createVirtualPointList(double latitude, double longitude) {
		ArrayList<VirtualPoint> virtualPointList = new ArrayList<VirtualPoint>();

		virtualPointList.add(createNewVirtualPoint(latitude, longitude, 0, 1, 1));

		int n1 = 4;
		for (int i = 1; i <= n1; i++) {
			virtualPointList.add(createNewVirtualPoint(latitude, longitude, 1, n1, i));
		}

		int n2 = 8;
		for (int i = 1; i <= n2; i++) {
			virtualPointList.add(createNewVirtualPoint(latitude, longitude, 2, n2, i));
		}

		int n3 = 16;
		for (int i = 1; i <= n3; i++) {
			virtualPointList.add(createNewVirtualPoint(latitude, longitude, 3, n3, i));
		}

		int n4 = 32;
		for (int i = 1; i <= n4; i++) {
			virtualPointList.add(createNewVirtualPoint(latitude, longitude, 4, n4, i));
		}

		return virtualPointList;
	}
}