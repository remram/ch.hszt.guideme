package ch.hszt.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class GuidemeActivity extends MapActivity {
	private int zoomLevel = 19; 
	private MapView mapView = null;
	private MapController mapController = null;
	private MyLocationOverlay whereAmI = null;
	private LocationManager locationMgr = null;
	private Button placeRequst;
	private TextView textView;
	private double latitude = 47.434772;
	private double longitude = 8.571534;
	private final int CURRENT_GPS_INFO = 0;
	private String location = null;
	private ArrayList<Place> placeList = null;
	private ArrayList<GeoPoint> gpList = null;
	private List<Overlay> overlayList;
	private Drawable drawable;
	private GooglePlaceOverlay itemizedoverlay ;
	private MapOverlay mapOverlay;
	private int distance;
	@SuppressWarnings("unused")
	private int foundPoints;
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
		@SuppressWarnings({"rawtypes", "unchecked" })
		@Override
		public void onClick(View v) {
			System.out.println(whereAmI.getMyLocation());
			placeList = searchPlaces();

			if (!(getPlaces().equals(null))) {
				location = getPlaces();
			}
			else {
				location = getString(R.string.fail);
			}

			latitude = whereAmI.getMyLocation().getLatitudeE6()/1E6;
			longitude = whereAmI.getMyLocation().getLongitudeE6()/1E6;
			showDialog(CURRENT_GPS_INFO);
			if (! (getPoints().equals(null))) {
				gpList = getPoints();
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
				while (iterator.hasNext()) {
					mapOverlay = new MapOverlay(geoPointList, mapView);
					geoPointList = (ArrayList<GeoPoint>) iterator.next();
					if (counter == 0) {
						mapOverlay.setColor(Color.RED);
					}
					else if (counter == 1) {
						mapOverlay.setColor(Color.YELLOW);
					}
					else if (counter == 2) {
						mapOverlay.setColor(Color.GREEN);
					}			
					else {
						mapOverlay.setColor(Color.CYAN);
					}
					mapView.getOverlays().add(mapOverlay);
					mapView.invalidate();
					counter++;
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
		case CURRENT_GPS_INFO:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.info);
			builder.setMessage(location);
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
	private String getPlaces() {
		StringBuilder sb = new StringBuilder();
		for (Place place : this.placeList) {
			sb.append(place.getName()+ " " + place.getVicinity()  + " " + place.getLatitude() + " " + place.getLongitude() +"\n" );
		}	
		
		return sb.toString();
	}

	/**
	 * create an ArrayList<GeoPoint> from all places in ArrayList<Place>
	 * @return
	 */
	private ArrayList<GeoPoint> getPoints() {
		ArrayList<GeoPoint> gplist = new ArrayList<GeoPoint>();
		for (Place place : this.placeList) {
			gplist.add(new GeoPoint((int) (place.getLatitude()*1E6),(int) (place.getLongitude()*1E6)));
		}
		this.foundPoints = this.placeList.size();
		return gplist;
	}
}