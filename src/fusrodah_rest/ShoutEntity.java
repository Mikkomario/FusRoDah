package fusrodah_rest;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fusrodah_main.ForbiddenActionException;
import fusrodah_main.FusrodahTable;
import fusrodah_main.Location;
import fusrodah_main.SimpleDate;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
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
	/**
	 * How long the shout can be heard after it has been shouted. In minutes.
	 */
	public static final int SHOUT_CAN_BE_HEARD_DURATION = 15;
	/**
	 * How long the shout can be shouted forward after it has been shouted last. In minutes.
	 */
	public static final int SHOUT_CAN_BE_SHOUTED_DURATION = 45;
	
	
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
	 * Creates a new shoutEntity with the given parameters. The entity must be based on another 
	 * shout.
	 * @param parent The entity that creates this entity
	 * @param parameters The parameters provided by the client.
	 * @throws HttpException If the shout couldn't be created
	 */
	public ShoutEntity(RestEntity parent, Map<String, String> parameters) throws HttpException
	{
		super(new SimpleRestData(), parent, FusrodahTable.SHOUTS, 
				checkShoutParameters(parameters), new HashMap<>());
	}
	
	/**
	 * Creates a new shoutEntity with the given parameters. The entity must be the first shout 
	 * created when a new template is created.
	 * @param template The template that creates this entity
	 * @param parameters The parameters provided by the client.
	 * @throws HttpException If the shout couldn't be created
	 */
	public ShoutEntity(ShoutTemplateEntity template, Map<String, String> parameters) throws 
			HttpException
	{
		super(new SimpleRestData(), template, FusrodahTable.SHOUTS, 
				checkTemplateParameters(parameters, template), new HashMap<>());
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
	 * @return The moment when the shout was first shouted
	 * @throws HttpException If the time couldn't be read
	 */
	public SimpleDate getShoutTime() throws HttpException
	{
		try
		{
			return new SimpleDate(getAttributes().get("created"));
		}
		catch (ParseException e)
		{
			throw new InternalServerException("Failed to check shout creation time", e);
		}
	}
	
	/**
	 * Checks whether the shout should be presented
	 * @param location The location at which the shout might be heard
	 * @param userID The user that might hear the shout
	 * @return Should the user hear the shout at the given location
	 * @throws HttpException If the operation failed
	 */
	public boolean isValidFor(Location location, String userID) throws HttpException
	{
		return canBeHeardBy(userID) && reaches(location);
	}
	
	/**
	 * This method checks whether the given user can hear this shout
	 * @param userID The identifier of the user that might hear the shout
	 * @return Can the user hear the shout
	 * @throws HttpException If the operation failed
	 */
	public boolean canBeHeardBy(String userID) throws HttpException
	{
		if (!canBeHeard())
			return false;
		
		String[] shouterIDs = getShouterIds();
		
		for (int i = 0; i < shouterIDs.length; i++)
		{
			if (shouterIDs[i].equals(userID))
				return false;
		}
		
		return true;
	}
	
	/**
	 * @return Can the shout still be heard at all
	 * @throws HttpException If the shout creation time couldn't be determined
	 */
	public boolean canBeHeard() throws HttpException
	{
		return getShoutTime().plus(SHOUT_CAN_BE_HEARD_DURATION).isPast(new SimpleDate());
	}
	
	/**
	 * @return Can the shout still be shouted forward
	 * @throws HttpException If the shout creation time couldn't be determined
	 */
	public boolean canBeReshouted() throws HttpException
	{
		return getShoutTime().plus(SHOUT_CAN_BE_SHOUTED_DURATION).isPast(new SimpleDate());
	}
	
	/**
	 * @return How far the shout reaches
	 */
	public double getReach()
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
		return getLocation().getDistanceFrom(location) < getReach();
	}
	
	private static Map<String, String> checkShoutParameters(Map<String, String> parameters) throws 
			HttpException
	{
		parameters = checkCommonParameters(parameters);
		
		// Checks that the given location is valid
		if (!parameters.containsKey("location"))
			throw new InvalidParametersException("Parameter 'location' required");
		
		if (!parameters.containsKey("lastShoutID"))
			throw new InvalidParametersException("Parameter 'lastShoutID' required");
		
		ShoutEntity lastShout = new ShoutEntity(parameters.get("lastShoutID"));
		
		// Checks that the last shout can still be reshouted
		if (!lastShout.canBeReshouted())
			throw new ForbiddenActionException(
					"The provided shout can no longer be reshouted");
		
		Map<String, String> lastShoutData = lastShout.getAttributes();
		
		parameters.put("shouterIDs", lastShoutData.get("shouterIDs") + "+" + 
				parameters.get("shouterID"));
		
		// Uses the same template, but also updates the template's last shout time
		parameters.put("templateID", lastShoutData.get("templateID"));
		
		// Checks that the template exists and can be shouted forward. Also updates the last 
		// shout time
		ShoutTemplateEntity template = 
				new ShoutTemplateEntity(parameters.get("templateID"));
		if (!template.canBeShouted())
			throw new ForbiddenActionException("The provided template can no longer be used");
		template.updateLastShoutTime(new SimpleDate());
		
		return parameters;
	}
	
	private static Map<String, String> checkTemplateParameters(Map<String, String> parameters, 
			ShoutTemplateEntity template) throws HttpException
	{
		parameters.put("shouterID", template.getAttributes().get("senderID"));
		parameters.put("shouterIDs", parameters.get("shouterID"));
		parameters.put("templateID", template.getDatabaseID());
		parameters.put("location", template.getAttributes().get("startLocation"));
		
		return checkCommonParameters(parameters);
	}
	
	private static Map<String, String> checkCommonParameters(Map<String, String> parameters) throws HttpException
	{
		if (!parameters.containsKey("shouterID"))
			throw new InvalidParametersException("Parameter 'shouterID' required");
		
		// The shouter must be an existing user
		UserEntity shouter = new UserEntity(parameters.get("shouterID"));
		
		// Also checks for authorization
		FusrodahTable.checkUserKey(shouter.getDatabaseID(), parameters);
		
		// Checks if the shouter can shout at this time
		if (!shouter.canShout())
			throw new ForbiddenActionException("The user can't shout yet due to cooldown.");
		
		// Updates the shouter location and shout time
		shouter.updateLocation(new Location(parameters.get("location")));
		shouter.updateLastShoutTime();
		
		// Adds the "created" parameter itself
		parameters.put("created", new SimpleDate().toString());
		
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
