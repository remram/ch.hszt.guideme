package ch.hszt.main;

import android.content.Context;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

/**
 * @author egon
 *
 */
public class MyCustomLocationOverlay extends MyLocationOverlay {
	private MapView mapView = null;

	/**
	 * overwrited constructor
	 * create a new instance from this class within the given context (the context where this instance had been called )
	 * set this instance on top of the mapView
	 * @param context
	 * @param mapView
	 */
	public MyCustomLocationOverlay(Context context, MapView mapView) {
		super(context, mapView);
		this.mapView = mapView;
	}

	/**
	 * onLocationChanged()
	 * update the mMapview as soon the position changed
	 */
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		GeoPoint geoPoint = new GeoPoint((int) (location.getLatitude()*1E6),
				(int) (location.getLongitude()*1E6));
		this.mapView.getController().animateTo(geoPoint);
	}
}