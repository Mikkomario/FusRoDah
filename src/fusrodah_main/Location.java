package fusrodah_main;

import flow_recording.ObjectFormatException;

/**
 * Location represents a location in the real world. The location is immutable once created.
 * 
 * @author Mikko Hilpinen
 * @since 1.3.2015
 */
public class Location
{
	// ATTRIBUTES	-----------------------------
	
	private final double latitude, longitude, accuracy;
	
	
	// CONSTRUCTOR	-----------------------------
	
	/**
	 * Parses a new location from the given string
	 * @param s The string that contains the location data. Should contain latitude, longitude 
	 * and accuracy, all in double format separated with semicolons.
	 */
	public Location(String s)
	{
		String[] parts = s.split(";");
		
		if (parts.length < 3)
			throw new ObjectFormatException("Can't parse a location from " + s);
		
		try
		{
			this.latitude = Double.parseDouble(parts[0]);
			this.longitude = Double.parseDouble(parts[1]);
			this.accuracy = Double.parseDouble(parts[2]);
		}
		catch (NumberFormatException e)
		{
			throw new ObjectFormatException("Location data must be in double format");
		}
	}
	
	/**
	 * Creates a new location
	 * @param latitude The latitude coordinate
	 * @param longitude The longitude coordinate
	 * @param accuracy The accuracy of the location
	 */
	public Location(double latitude, double longitude, double accuracy)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
	}
	
	
	// GETTERS & SETTERS	-------------------------------
	
	/**
	 * @return The latitude coordinate of this location
	 */
	public double getLatitude()
	{
		return this.latitude;
	}
	
	/**
	 * @return The longitude coordinate of this location
	 */
	public double getLongitude()
	{
		return this.longitude;
	}
	
	/**
	 * @return The accuracy of this location
	 */
	public double getAccuracy()
	{
		return this.accuracy;
	}
	
	
	// OTHER METHODS	-----------------------------------
	
	/**
	 * Calculates the direction from this location to the other location
	 * @param other The other location
	 * @return Direction from this location to the other. In degrees where 0 is east and 
	 * the value increases counter-clockwise.
	 */
	public double getDirectionTowards(Location other)
	{
		// TODO: Implement
		return 0;
	}
	
	/**
	 * Calculates the distance between the two locations
	 * @param other The other location
	 * @return The distance between the two locations
	 */
	public double getDistanceFrom(Location other)
	{
		// TODO: Implement
		return 0;
	}
}
