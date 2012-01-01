package ch.hszt.main;

import java.util.ArrayList;
/**
 * @author egon
 *
 */

import com.google.android.maps.OverlayItem;

import android.content.Context;

import android.graphics.drawable.Drawable;

/**
 * @author egon
 *
 */
@SuppressWarnings("rawtypes")
public class GooglePlaceOverlay extends com.google.android.maps.ItemizedOverlay {
	@SuppressWarnings("unused")
	private Context context;
	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();

	/**
	 * 
	 * @param defaultMarker
	 */
	public GooglePlaceOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	/**
	 * 
	 * @param defaultMarker
	 * @param context
	 */
	public GooglePlaceOverlay(Drawable defaultMarker, Context context) {
		super(defaultMarker);
		this.context = context;
	}
	
	/**
	 * create new overlayitem
	 */
	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}


	
	/**
	 * set getSize of overlay
	 */
	@Override
	public int size() {
		return overlays.size();
	}

	/**
	 * add overlay
	 * @param overlay
	 */
	public void addOverlay(OverlayItem overlay) {
		overlays.add(overlay);
		populate();
	}
}