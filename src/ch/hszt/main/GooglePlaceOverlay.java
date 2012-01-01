package ch.hszt.main;
import java.util.ArrayList;

import com.google.android.maps.OverlayItem;

import android.content.Context;

import android.graphics.drawable.Drawable;


@SuppressWarnings("rawtypes")
public class GooglePlaceOverlay extends com.google.android.maps.ItemizedOverlay {
	@SuppressWarnings("unused")
	private Context context;
	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();


	public GooglePlaceOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}



	public GooglePlaceOverlay(Drawable defaultMarker, Context context) {
		super(defaultMarker);
		this.context = context;
	}


	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}

	@Override
	public int size() {
		return overlays.size();
	}


	public void addOverlay(OverlayItem overlay) {
		overlays.add(overlay);
		populate();
	}

}
