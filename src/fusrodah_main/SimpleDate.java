package fusrodah_main;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is a simplification used for date and time handling.
 * 
 * @author Mikko Hilpinen
 * @since 11.2.2015
 */
public class SimpleDate implements Comparable<SimpleDate>
{
	// ATTRIBUTES	-------------------------------
	
	private static DateFormat format = new SimpleDateFormat("MM-dd-HH-mm");
	
	private Date date;
	
	
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new date based on the given date instance
	 * @param date The date that is simplified
	 */
	public SimpleDate(Date date)
	{
		this.date = date;
	}

	/**
	 * Parses a new date from the given string
	 * @param dateString The string that contains the date information should be in format: 
	 * MM-dd-HH-mm (from months to days to hours to minutes, separated with a '-'). For 
	 * example: 11-21-18-30
	 * @throws ParseException If a date couldn't be parsed from the given string
	 */
	public SimpleDate(String dateString) throws ParseException
	{
		this.date = format.parse(dateString);
	}
	
	/**
	 * Creates a new date to represent the current moment
	 */
	public SimpleDate()
	{
		this.date = new Date(System.currentTimeMillis());
	}
	
	
	// IMPLEMENTED METHODS	-------------------------
	
	@Override
	public String toString()
	{
		return format.format(this.date);
	}

	@Override
	public int compareTo(SimpleDate o)
	{
		return this.date.compareTo(o.date);
	}
}
