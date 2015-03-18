package fusrodah_main;

import flow_recording.ObjectFormatException;
import genesis_util.HelpMath;
import genesis_util.Vector2D;

/**
 * Location represents a location in the real world. The location is immutable once created.
 * 
 * @author Mikko Hilpinen
 * @since 1.3.2015
 */
public class Location
{
	// ATTRIBUTES	-----------------------------
	
	private final Vector2D latLong;
	
	private static final int EARTH_RADIUS = 6371000; // metres
	
	
	// CONSTRUCTOR	-----------------------------
	
	/**
	 * Parses a new location from the given string
	 * @param s The string that contains the location data. Should contain latitude, longitude, 
	 * both in double format separated with a semicolon.
	 */
	public Location(String s)
	{
		String[] parts = s.split(";");
		
		if (parts.length < 2)
			throw new ObjectFormatException("Can't parse a location from " + s);
		
		try
		{
			double latitude = Double.parseDouble(parts[0]);
			double longitude = Double.parseDouble(parts[1]);
			
			this.latLong = new Vector2D(latitude, longitude);
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
	public Location(Vector2D latitudeLongitude)
	{
		this.latLong = latitudeLongitude;
	}
	
	
	// GETTERS & SETTERS	-------------------------------
	
	/**
	 * @return The latitude-longitude coordinates of this position
	 */
	public Vector2D getCoordinates()
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
		
		Vector2D radianCoordinates1 = getRadianCoordinates();
		Vector2D radianCoordinates2 = other.getRadianCoordinates();
		
		double y = Math.sin(radianCoordinates2.getSecond() - radianCoordinates1.getSecond()) * 
				Math.cos(radianCoordinates2.getFirst());
		double x = Math.cos(radianCoordinates1.getFirst()) * 
				Math.sin(radianCoordinates2.getFirst()) - 
				Math.sin(radianCoordinates1.getFirst()) * 
				Math.cos(radianCoordinates2.getFirst() * 
				Math.cos(radianCoordinates2.getSecond() - radianCoordinates1.getSecond()));
		
		return HelpMath.getVectorDirection(x, y);
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
	
	private Vector2D getRadianCoordinates()
	{
		return new Vector2D(Math.toRadians(-getLatitude()), Math.toRadians(-getLongitude()));
	}
}
