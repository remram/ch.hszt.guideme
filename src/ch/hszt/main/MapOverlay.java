package ch.hszt.main;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * @author egon
 *
 */
public class MapOverlay extends com.google.android.maps.Overlay{
	private MapView mapView = null;
	private ArrayList<GeoPoint> geoPointList;
	private int color;
	
	/**
	 * Overwrited constructor. create an instance of this class with an ArrayList<GeoPoint> inside the mapView 
	 * @param geoPointList
	 * @param color
	 * @param mapView
	 */
	public MapOverlay(ArrayList<GeoPoint> geoPointList, int color, MapView mapView) {
			this.geoPointList = geoPointList;
			this.color = color;
			
			int moveToLat = (this.geoPointList.get(0).getLatitudeE6() + 
					( this.geoPointList.get(this.geoPointList.size() - 1).getLatitudeE6() - this.geoPointList.get(0).getLatitudeE6() ) / 2);
			
			int moveToLong = (this.geoPointList.get(0).getLongitudeE6() + 
					( this.geoPointList.get(this.geoPointList.size() - 1).getLongitudeE6() - this.geoPointList.get(0).getLongitudeE6() ) / 2);		
			
			GeoPoint moveTo = new GeoPoint(moveToLat, moveToLong);
			this.mapView = mapView;
			this.mapView.getController().animateTo(moveTo);
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		super.draw(canvas, mapView, shadow);
		drawPath(mapView, canvas);
		return true;
	}

	/**
	 * Draw methode to draw the path
	 * @param mapView
	 * @param canvas
	 */
	public void drawPath(MapView mapView, Canvas canvas) {
		int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
		Paint paint = new Paint();
		paint.setColor(this.color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
	    paint.setAlpha(123);

		for (int i = 0; i < this.geoPointList.size(); i++) {
			Point point = new Point();
			mapView.getProjection().toPixels(this.geoPointList.get(i), point);
			x2 = point.x;
			y2 = point.y;
			if (i > 0) {
				canvas.drawLine(x1, y1, x2, y2, paint);
			}
			x1 = x2;
			y1 = y2;
		}
	}
	
}