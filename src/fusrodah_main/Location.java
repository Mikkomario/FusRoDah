package fusrodah_main;

import flow_recording.ObjectFormatException;
import genesis_util.HelpMath;
import genesis_util.Vector3D;

/**
 * Location represents a location in the real world. The location is immutable once created.
 * 
 * @author Mikko Hilpinen
 * @since 1.3.2015
 */
public class Location
{
	// ATTRIBUTES	-----------------------------
	
	private final Vector3D latLong;
	
	private static final int EARTH_RADIUS = 6371000; // metres
	
	
	// CONSTRUCTOR	-----------------------------
	
	/**
	 * Parses a new location from the given string
	 * @param s The string that contains the location data. Should contain latitude, longitude, 
	 * both in double format separated with a semicolon.
	 */
	public Location(String s)
	{
		if (s == null)
			throw new ObjectFormatException("Location cannot be null");
		
		String[] parts = s.split(";");
		
		if (parts.length < 2)
			throw new ObjectFormatException("Can't parse a location from " + s);
		
		try
		{
			double latitude = Double.parseDouble(parts[0]);
			double longitude = Double.parseDouble(parts[1]);
			
			this.latLong = new Vector3D(latitude, longitude);
		}
		catch (NumberFormatException e)
		{
			throw new ObjectFormatException("Location data must be in double format");
		}
	}
	
	/**
	 * Creates a new location
	 * @param latitudeLongitude The location's coordinates in latitude-longitude format
	 */
	public Location(Vector3D latitudeLongitude)
	{
		this.latLong = latitudeLongitude;
	}
	
	
	// IMPLEMENTED METHODS	-------------------------------
	
	@Override
	public String toString()
	{
		return getLatitude() + ";" + getLongitude();
	}
	
	
	// GETTERS & SETTERS	-------------------------------
	
	/**
	 * @return The latitude-longitude coordinates of this position
	 */
	public Vector3D getCoordinates()
	{
		return this.latLong;
	}
	
	/**
	 * @return The latitude coordinate of this location
	 */
	public double getLatitude()
	{
		return getCoordinates().getFirst();
	}
	
	/**
	 * @return The longitude coordinate of this location
	 */
	public double getLongitude()
	{
		return getCoordinates().getSecond();
	}
	
	
	// OTHER METHODS	-----------------------------------
	
	/**
	 * Calculates the direction from this location to the other location
	 * @param other The other location
	 * @return Direction from this location to the other. In degrees where 0 is east and 
	 * the value increases counter-clockwise.
	 * @see "http://www.movable-type.co.uk/scripts/latlong.html"
	 */
	public double getDirectionTowards(Location other)
	{	
		/*
		 * var y = Math.sin(λ2-λ1) * Math.cos(φ2);
			var x = Math.cos(φ1)*Math.sin(φ2) -
			        Math.sin(φ1)*Math.cos(φ2)*Math.cos(λ2-λ1);
			var brng = Math.atan2(y, x).toDegrees();
		 */
		
		Vector3D radianCoordinates1 = getRadianCoordinates();
		Vector3D radianCoordinates2 = other.getRadianCoordinates();
		
		double y = Math.sin(radianCoordinates2.getSecond() - radianCoordinates1.getSecond()) * 
				Math.cos(radianCoordinates2.getFirst());
		double x = Math.cos(radianCoordinates1.getFirst()) * 
				Math.sin(radianCoordinates2.getFirst()) - 
				Math.sin(radianCoordinates1.getFirst()) * 
				Math.cos(radianCoordinates2.getFirst() * 
				Math.cos(radianCoordinates2.getSecond() - radianCoordinates1.getSecond()));
		
		return Math.toDegrees(Math.atan2(y, x));//HelpMath.getVectorDirection(x, y);
	}
	
	/**
	 * Calculates the distance between the two locations
	 * @param other The other location
	 * @return The distance between the two locations
	 * @see "http://www.movable-type.co.uk/scripts/latlong.html"
	 */
	public double getDistanceFrom(Location other)
	{
		/*
		 * var R = 6371000; // metres
			var φ1 = lat1.toRadians();
			var φ2 = lat2.toRadians();
			var Δφ = (lat2-lat1).toRadians();
			var Δλ = (lon2-lon1).toRadians();
			
			var a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
			        Math.cos(φ1) * Math.cos(φ2) *
			        Math.sin(Δλ/2) * Math.sin(Δλ/2);
			var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
			
			var d = R * c;
		 */
		
		double latRads1 = getRadianCoordinates().getFirst();
		double latRads2 = other.getRadianCoordinates().getFirst();
		
		double deltaLatitudeRads = Math.toRadians(-HelpMath.checkDirection(
				other.getLatitude() - getLatitude()));
		double deltaLongitudeRads = Math.toRadians(-HelpMath.checkDirection(
				other.getLongitude() - getLongitude()));
		
		double a = Math.pow(Math.sin(deltaLatitudeRads / 2), 2) + 
				Math.cos(latRads1) * Math.cos(latRads2) * 
				Math.pow(Math.sin(deltaLongitudeRads / 2), 2);
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		
		return EARTH_RADIUS * c;
	}
	
	private Vector3D getRadianCoordinates()
	{
		return new Vector3D(Math.toRadians(-getLatitude()), Math.toRadians(-getLongitude()));
	}
	
	/*
	public static void main(String[] args)
	{
		Location jmt3 = new Location("60.187668;24.835167");
		Location west = new Location("60.177668;24.835167");
		Location north = new Location("60.187668;24.705167");
		Location rautsikka = new Location("60.171730;24.941385");
		System.out.println("Distance between jmt3 and rautsikka: " + jmt3.getDistanceFrom(rautsikka));
		System.out.println("Direction from jmt to rautsikka: " + jmt3.getDirectionTowards(rautsikka));
		System.out.println("Direction west: " + jmt3.getDirectionTowards(west));
		System.out.println("Direction north: " + jmt3.getDirectionTowards(north));
	}
	*/
}
