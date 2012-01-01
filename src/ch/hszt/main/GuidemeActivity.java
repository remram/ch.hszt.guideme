package ch.hszt.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import ch.hszt.main.R;
import ch.hszt.controller.GoogleDirectionsRequest;
import ch.hszt.controller.GooglePlaceRequest;
import ch.hszt.controller.PolylineDecoder;
import ch.hszt.model.Location;
import ch.hszt.model.Place;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author egon
 *
 */
public class GuidemeActivity extends MapActivity {
	/**
	 * initial parameter
	 */
	
	private final int CURRENT_MESSAGE_INFO = 0;

	private boolean publicStationOnly = false;

	private int distance = 0;	

	private int foundPoints = 0;

	private int zoomLevel = 17;

	private double latitude = 0.0;

	private double longitude = 0.0;

	private MapView mapView = null;

	private MapController mapController = null;

	private MyLocationOverlay whereAmI = null;

	private List<Overlay> overlayList;

	private Drawable drawable;

	private GooglePlaceOverlay itemizedoverlay ;

	private MapOverlay mapOverlay;

	private LocationManager locationMgr = null;

	private Button placeRequst;

	private TextView textView;

	private String message = null;

	private ArrayList<Place> placeList = null;

	private ArrayList<GeoPoint> gpList = null;

	private TreeMap<Integer, ArrayList<GeoPoint>> sortedRoutes;

	private ArrayList<GeoPoint> geoPointList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		placeList = new ArrayList<Place>();
		sortedRoutes = new TreeMap<Integer, ArrayList<GeoPoint>>();
		geoPointList = new ArrayList<GeoPoint>();

		mapView = (MapView)findViewById(R.id.geoMap);
		mapView.setBuiltInZoomControls(true);

		mapController = mapView.getController();
		mapController.setZoom(zoomLevel);

		whereAmI = new MyCustomLocationOverlay(this, mapView);
		mapView.getOverlays().add(whereAmI);
		mapView.postInvalidate();

		locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		placeRequst = (Button) findViewById(R.id.button);
		placeRequst.setText(R.string.request);
		placeRequst.setOnClickListener(onclickListener);
		textView = (TextView) findViewById(R.id.gps_pos);

		overlayList = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.blue_marker_a);
		itemizedoverlay = new GooglePlaceOverlay(drawable);
	}

	/**
	 * onResume methode
	 */
	@Override
	public void onResume() {
		super.onResume();	

		if (!locationMgr.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.alert)
			.setMessage(R.string.message).setCancelable(true)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					finish();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} 

		else
		{
			whereAmI.enableMyLocation();
			whereAmI.getMyLocation();
			whereAmI.runOnFirstFix(new Runnable() {
				public void run() {
					mapController.setCenter(whereAmI.getMyLocation());
					latitude = whereAmI.getMyLocation().getLatitudeE6()/1E6;	//required if this value is not actual, the pos from new-york is given back !!
					longitude = whereAmI.getMyLocation().getLongitudeE6()/1E6;	//required if this value is not actual, the pos from new-york is given back !!
				}
			});
		}
	}

	/**
	 * Listener which listens if button is pushed.
	 * if button has been pushed, perform all steps in onClick method
	 */
	View.OnClickListener onclickListener = new View.OnClickListener() {
		@SuppressWarnings({"rawtypes", "unchecked", "unused" })
		@Override

		public void onClick(View v) {
			mapView.getOverlays().clear();
			drawable = mapView.getResources().getDrawable(R.drawable.blue_marker_a);
			itemizedoverlay = new GooglePlaceOverlay(drawable);
			mapView.getOverlays().add(whereAmI);

			if (! (checkNetworkStatus()) ) {	
				showDialog(CURRENT_MESSAGE_INFO);
			}			
			else {
				placeList = searchPlaces();
				if (placeList.equals(null)) {
					setMessage(getString(R.string.no_connection));	// in case of no connection to google is possible
					showDialog(CURRENT_MESSAGE_INFO);
				}

				else {
					setMessage(getStringFromAllFountPlaces());	// only for dialog info requierd					
					if (getMessage().equals(null)) {
						setMessage(getString(R.string.no_places));	// in case no places had been found
					}
					
					latitude = whereAmI.getMyLocation().getLatitudeE6()/1E6;
					longitude = whereAmI.getMyLocation().getLongitudeE6()/1E6;
					gpList = getAllFoundPlaces();
					if (! (gpList.equals(null)) ) {
						for (GeoPoint point : gpList) {		
							GeoPoint geoPoint = new GeoPoint(point.getLatitudeE6(),point.getLongitudeE6());	
							OverlayItem overlayitem = new OverlayItem(geoPoint, "" ,"");
							itemizedoverlay.addOverlay(overlayitem);
							overlayList.add(itemizedoverlay);
							geoPointList = getWayPoints(latitude, longitude, (point.getLatitudeE6()/1E6), (point.getLongitudeE6()/1E6));					
							sortedRoutes.put(distance, geoPointList);
						}

						int counter = 0;
						Collection collection = sortedRoutes.values();
						Iterator iterator = collection.iterator();	
						Set keySet = sortedRoutes.keySet();
						Integer [] keys = new Integer [keySet.size()];
						keySet.toArray(keys);
						for (int i = 0; i < keys.length; i++) {
							geoPointList = sortedRoutes.get(keys[i]);
							if (counter == 0) {
								mapOverlay = new MapOverlay(geoPointList, Color.GREEN, mapView );
							}

							else if (counter == 1) {
								mapOverlay = new MapOverlay(geoPointList, Color.rgb(255, 165, 0), mapView);
							}		

							else {
								mapOverlay = new MapOverlay(geoPointList, Color.RED, mapView);
							}

							mapView.getOverlays().add(mapOverlay);
							counter++;
						}

					}

					else {
						setMessage(getString(R.string.no_ways_available));	// in case no ways could be calculated. No Waypoints availabe..
						showDialog(CURRENT_MESSAGE_INFO);
					}
				}
			}
		}
	};
	
	/**
	 * onPause methode
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		whereAmI.disableMyLocation();
	}

	/**
	 * is location displayed 
	 */
	@Override
	protected boolean isLocationDisplayed() {
		return whereAmI.isMyLocationEnabled();
	}

	/**
	 * is route displayed
	 */
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * methode to create a dialog for information
	 */
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CURRENT_MESSAGE_INFO:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.info);
			builder.setMessage(getMessage());
			builder.setCancelable(false);
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					textView.setText(getString(R.string.alert));
				}
			});
			return builder.create();

		default:
			return super.onCreateDialog(id);
		}
	}

	/**
	 * main methode to initiate the search. It return an ArrayList with found places
	 * @return
	 */
	private ArrayList<Place> searchPlaces() {
		try {
			this.placeList = new GooglePlaceRequest(this.latitude, this.longitude).search();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return placeList;
	}

	/**
	 * method which looks for all waypoints which are necessary to calulcate the way for a map
	 * @param fromLat
	 * @param fromLng
	 * @param toLat
	 * @param toLng
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	private ArrayList<GeoPoint> getWayPoints(double fromLat, double fromLng, double toLat, double toLng) {
		GoogleDirectionsRequest pgdr = new GoogleDirectionsRequest(fromLat, fromLng, toLat, toLng);
		ArrayList<Location> locationList = new ArrayList<Location>();
		ArrayList<GeoPoint> geoPointList = new ArrayList<GeoPoint>();
		PolylineDecoder pd = new PolylineDecoder();
		ArrayList<String> polylinepoints  = pgdr.searchGoogleDirections();


		for (String string : polylinepoints) {
			locationList.addAll(pd.decodePoly(string));
		}

		for (Location location : locationList) {
			geoPointList.add(new GeoPoint( ((int) location.getLatitude()), ((int) location.getLongitude())));
		}

		this.distance = pgdr.getDistance();

		return geoPointList;		
	}

	/**
	 * Methode to get inforsmation about places
	 * @return
	 */
	private String getStringFromAllFountPlaces() {
		StringBuilder sb = new StringBuilder();
		for (Place place : placeList) {
			sb.append(place.getName()+ " " + place.getVicinity() + "\n" );
		}	
		return sb.toString();
	}

	/**
	 * create an ArrayList<GeoPoint> from all places in ArrayList<Place>
	 * @return
	 */
	private ArrayList<GeoPoint> getAllFoundPlaces() {
		ArrayList<GeoPoint> gplist = new ArrayList<GeoPoint>();
		for (Place place : this.placeList) {

			if (publicStationOnly) {
				if (place.getVicinity().equals("Switzerland")) {
					gplist.add(new GeoPoint((int) (place.getLatitude()*1E6),(int) (place.getLongitude()*1E6)));
				}
			}
			else {
				gplist.add(new GeoPoint((int) (place.getLatitude()*1E6),(int) (place.getLongitude()*1E6)));
			}
		}
		this.foundPoints = this.placeList.size();
		return gplist;
	}

	/**
	 * check if wifi or mobile lan is available. Retrun true if Wifi is available if it isnt, it check if mobile Lan
	 * is availabe. if both are disabled it return false.
	 * @return
	 */
	public boolean checkNetworkStatus() {
		ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {	
			try {
				InetAddress addr = InetAddress.getByName("google.com");
				int port = 80;
				SocketAddress sockaddr = new InetSocketAddress(addr, port);
				Socket socket = new Socket();
				// This method will block no more than timeoutMs.
				// If the timeout occurs, SocketTimeoutException is thrown.
				int timeoutMs = 1000;   // 1 second
				socket.connect(sockaddr, timeoutMs);
			} catch (UnknownHostException e) {
				e.getStackTrace();
			} catch (SocketTimeoutException e) {
				setMessage(getString(R.string.no_connection));
				return false;
			} catch (IOException e) {
			}
			return true;
		}

		else if ( connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {

			try {
				InetAddress addr = InetAddress.getByName("google.com");
				int port = 80;
				SocketAddress sockaddr = new InetSocketAddress(addr, port);
				Socket socket = new Socket();
				// This method will block no more than timeoutMs.
				// If the timeout occurs, SocketTimeoutException is thrown.
				int timeoutMs = 1000;   // 1 second
				socket.connect(sockaddr, timeoutMs);
			} catch (UnknownHostException e) {
				e.getStackTrace();
			} catch (SocketTimeoutException e) {
				setMessage(getString(R.string.no_connection));
				return false;
			} catch (IOException e) {
			}
			return true;
		}
		else {
			setMessage(getString(R.string.network_failure));
			return false;			
		}
	}

	/**
	 * setter for dialog message 
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * getter for dialog message
	 * @return
	 */
	public String getMessage() {
		return this.message;
	}
}