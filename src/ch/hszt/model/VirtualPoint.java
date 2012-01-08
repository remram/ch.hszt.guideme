package ch.hszt.model;

public class VirtualPoint {
	private double latitude;
	private double longitude;
	private VirtualPoint pos;
	
	public VirtualPoint(double latitude, double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public VirtualPoint(VirtualPoint pos){
		this.setPos(pos);
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public VirtualPoint getPos() {
		return pos;
	}

	public void setPos(VirtualPoint pos) {
		this.pos = pos;
	}
	
}
