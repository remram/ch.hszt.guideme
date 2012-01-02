package ch.hszt.model;

/**
 * @author egon
 *
 */
public class Place {

	private String name;

	private String vicinity;

	private double longitude;

	private double latitude;

	/**
	 * Getter to get the name from each object of this class
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter to get the name from each object of this class
	 * @return
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter to get the vicinity from each object of this class
	 * @return
	 */
	public String getVicinity() {
		return vicinity;
	}

	/**
	 * Setter to set the vicinity from each object of this class
	 * @return
	 */
	public void setVicinity(String vicinity) {
		this.vicinity = vicinity;
	}

	/**
	 * Getter to get the longitude from each object of this class
	 * @return
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Setter to set the longitude from each object of this class
	 * @return
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Getter to get the latitude from each object of this class
	 * @return
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Setter to set the latitude from each object of this class
	 * @return
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Overwirte the default toString methode
	 * @return
	 */
	@Override
	public String toString() {
		return String.format("name:%s,vicinity:%s,latitude:%f,longitude:%f",
				name, vicinity, latitude, longitude);
	}
}