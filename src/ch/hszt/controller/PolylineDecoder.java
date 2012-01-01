package ch.hszt.controller;

import java.util.ArrayList;

import ch.hszt.model.Location;

/**
 * class to decode the polylines from google places
 * @author egi
 *
 */
public class PolylineDecoder {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArrayList decodePoly(String encoded) {
		ArrayList poly = new ArrayList();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;
		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;
			Location p = new Location((((double) lat*10)), (((double) lng*10 )));

			poly.add(p);
		}
		return poly;
	}
}