package fusrodah_rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fusrodah_main.FusrodahTable;
import fusrodah_main.Location;
import fusrodah_main.SimpleDate;
import nexus_http.HttpException;
import nexus_http.InvalidParametersException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.RestEntityList;
import nexus_rest.SimpleRestData;
import nexus_rest.SimpleRestEntityList;
import alliance_rest.DatabaseEntity;

/**
 * This entity represents a single "shout" created by a user
 * 
 * @author Mikko Hilpinen
 * @since 13.2.2015
 */
public class ShoutEntity extends DatabaseEntity
{
	// ATTRIBUTES	-----------------------------
	
	private static final String ROOTPATH = "root/shouts/";
	
	
	
	// CONSTRUCTOR	-----------------------------
	
	/**
	 * Creates a new shout by reading it's data from the database
	 * @param id The identifier of the shout
	 * @throws HttpException If the shout couldn't be read
	 */
	public ShoutEntity(String id) throws HttpException
	{
		super(new SimpleRestData(), ROOTPATH, FusrodahTable.SHOUTS, id);
	}

	/**
	 * Creates a new shoutEntity with the given parameters.
	 * @param parent The entity that creates this entity
	 * @param parameters The parameters provided by the client. Supports optional parameters 
	 * "lastShoutID" and "shouterID"
	 * @throws HttpException If the shout couldn't be created
	 */
	public ShoutEntity(RestEntity parent, Map<String, String> parameters) throws HttpException
	{
		super(new SimpleRestData(), parent, FusrodahTable.SHOUTS, checkParameters(parameters), 
				new HashMap<>());
	}
	
	
	// IMPLEMENTED METHODS	-----------------------------

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.PUT);
	}

	@Override
	protected Map<String, RestEntity> getMissingEntities(
			Map<String, String> parameters) throws HttpException
	{
		Map<String, RestEntity> links = new HashMap<>();
		
		links.put("template", new ShoutTemplateEntity(getAttributes().get("templateID")));
		links.put("shouters", getShouters());
		
		return links;
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		if (pathPart.equals("template"))
			return new ShoutTemplateEntity(getAttributes().get("templateID"));
		else if (pathPart.equals("shouters"))
			return getShouters();
		
		throw new NotFoundException(getPath() + pathPart);
	}

	
	// OTHER METHODS	----------------------------------
	
	/**
	 * Checks whether the shout should be presented
	 * @param location The location at which the shout might be heard
	 * @param userID The user that might hear the shout
	 * @return Should the user hear the shout at the given location
	 */
	public boolean isValidFor(Location location, String userID)
	{
		return canBeHeardBy(userID) && reaches(location);
	}
	
	/**
	 * This method checks whether the given user can hear this shout
	 * @param userID The identifier of the user that might hear the shout
	 * @return Can the user hear the shout
	 */
	public boolean canBeHeardBy(String userID)
	{
		String[] shouterIDs = getShouterIds();
		
		for (int i = 0; i < shouterIDs.length; i++)
		{
			if (shouterIDs[i].equals(userID))
				return false;
		}
		
		return true;
	}
	
	/**
	 * @return How far the shout reaches
	 */
	public double getRange()
	{
		// TODO: Create a better version at some point
		return 1000;
	}
	
	/**
	 * @return The origin location of this shout
	 */
	public Location getLocation()
	{
		return new Location(getAttributes().get("location"));
	}
	
	/**
	 * Checks whether this shout reaches the given location
	 * @param location The location this shout should reach
	 * @return Does this shout reach the given location
	 */
	public boolean reaches(Location location)
	{
		return getLocation().getDistanceFrom(location) < getRange();
	}
	
	private static Map<String, String> checkParameters(Map<String, String> parameters) throws 
			HttpException
	{
		// Checks that the given location is valid
		if (!parameters.containsKey("location"))
			throw new InvalidParametersException("Parameter 'location' required");
		
		if (!parameters.containsKey("shouterID"))
			throw new InvalidParametersException("Parameter 'shouterID' required");
		
		// The shouter must be an existing user
		UserEntity shouter = new UserEntity(parameters.get("shouterID"));
		
		// Also checks for authorization
		FusrodahTable.checkUserKey(shouter.getDatabaseID(), parameters);
		
		// Updates the shouter location
		shouter.updateLocation(new Location(parameters.get("location")));
		
		// Adds the "created" parameter itself
		parameters.put("created", new SimpleDate().toString());
		
		// If parameter "lastShoutID" has been provided, uses the data of that shout
		if (parameters.containsKey("lastShoutID"))
		{
			// TODO: Check that the last shout can actually be heard from the given location
			
			Map<String, String> lastShoutData = 
					new ShoutEntity(parameters.get("lastShoutID")).getAttributes();
			
			parameters.put("shouterIDs", lastShoutData.get("shouterIDs") + "+" + 
					parameters.get("shouterID"));
			
			// Uses the same template, but also updates the template's last shout time
			ShoutTemplateEntity template = 
					new ShoutTemplateEntity(lastShoutData.get("templateID"));
			parameters.put("templateID", template.getDatabaseID());
			template.updateLastShoutTime(new SimpleDate());
		}
		else
			parameters.put("shouterIDs", shouter.getDatabaseID());
		
		return parameters;
	}
	
	private RestEntityList getShouters() throws HttpException
	{
		// Parses the ids from the shouterIDs
		String[] shouterIDs = getShouterIds();
		List<RestEntity> shouters = new ArrayList<>();
		
		for (int i = 0; i < shouterIDs.length; i++)
		{
			shouters.add(new UserEntity(shouterIDs[i]));
		}
		
		return new SimpleRestEntityList("shouters", this, shouters);
	}
	
	private String[] getShouterIds()
	{
		return getAttributes().get("shouterIDs").split("\\+");
	}
}
