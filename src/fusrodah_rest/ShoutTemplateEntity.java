package fusrodah_rest;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import vault_database.DatabaseAccessor;
import vault_database.DatabaseUnavailableException;
import flow_recording.ObjectFormatException;
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
import nexus_rest.SimpleRestData;
import alliance_rest.DatabaseEntity;

/**
 * This entity represents a shout template
 * 
 * @author Mikko Hilpinen
 * @since 11.2.2015
 */
public class ShoutTemplateEntity extends DatabaseEntity
{
	// ATTRIBUTES	---------------------------------
	
	private static final String ROOTPATH = "root/templates/";
	
	
	// CONSTRUCTOR	---------------------------------
	
	/**
	 * Creates a new template by reading its data from the database
	 * @param id The identifier of the entity
	 * @throws HttpException If the entity couldn't be read or found
	 */
	public ShoutTemplateEntity(String id) throws HttpException
	{
		super(new SimpleRestData(), ROOTPATH, FusrodahTable.TEMPLATES, id);
	}

	/**
	 * Creates a new template and writes its data to the database
	 * @param parent The parent entity of this template
	 * @param parameters The parameters provided by the client
	 * @throws HttpException If the template couln't be written or created
	 */
	public ShoutTemplateEntity(RestEntity parent, Map<String, String> parameters) throws 
			HttpException
	{
		super(new SimpleRestData(), parent, FusrodahTable.TEMPLATES, 
				checkParameters(parameters), getDefaultParameters());
		
		// Creates a new shout as well
		/*
		parameters.put("templateID", getDatabaseID());
		parameters.put("shouterID", parameters.get("senderID"));
		parameters.put("location", parameters.get("startLocation"));
		*/
		new ShoutEntity(this, parameters);
	}
	
	
	// IMPLEMENTED METHODS	----------------------------

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.PUT);
	}

	@Override
	protected Map<String, RestEntity> getMissingEntities(
			Map<String, String> parameters) throws HttpException
	{
		Map<String, String> attributes = getAttributes();
		Map<String, RestEntity> links = new HashMap<>();
		
		links.put("sender", new UserEntity(attributes.get("senderID")));
		if (!attributes.get("receiverID").equals("-1"))
			links.put("receiver", new UserEntity(attributes.get("receiverID")));
		
		return links;
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		if (pathPart.equals("sender"))
			return new UserEntity(getAttributes().get("senderID"));
		else if (pathPart.equals("receiver"))
		{
			String receiverID = getAttributes().get("receiverID");
			if (!receiverID.equals("-1"))
				return new UserEntity(receiverID);
		}
		
		throw new NotFoundException(getPath() + "/" + pathPart);
	}
	
	@Override
	protected void prepareDelete(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.DELETE);
	}

	
	// OTHER METHODS	------------------------------
	
	/**
	 * Deletes the template and each shout created from it
	 * @throws HttpException If the template or the shouts couldn't be deleted
	 */
	public void delete() throws HttpException
	{
		// Deletes the template
		super.prepareDelete(null);
		
		try
		{
			// Deletes all shouts created from this template
			DatabaseAccessor.delete(FusrodahTable.SHOUTS, "templateID", getDatabaseID());
		}
		catch (SQLException | DatabaseUnavailableException e)
		{
			throw new InternalServerException("Failed to delete the shouts", e);
		}
	}
	
	/**
	 * Updates the last shout time attribute to the database
	 * @param time The new last shout time
	 * @throws HttpException If the update couldn't be written
	 */
	public void updateLastShoutTime(SimpleDate time) throws HttpException
	{
		setAttribute("lastShoutTime", time.toString());
		writeData();
	}
	
	/**
	 * @return Can the template still be used for creating shouts
	 * @throws HttpException If the operation failed
	 */
	public boolean canBeShouted() throws HttpException
	{
		try
		{
			SimpleDate lastShoutTime = new SimpleDate(getAttributes().get("lastShoutTime"));
			return lastShoutTime.plus(ShoutEntity.SHOUT_CAN_BE_SHOUTED_DURATION).isPast(
					new SimpleDate());
		}
		catch (ParseException e)
		{
			throw new InternalServerException("Can't determine shoutTemplate shout time", e);
		}
	}
	
	private static Map<String, String> getDefaultParameters()
	{
		Map<String, String> defaults = new HashMap<>();
		
		defaults.put("receiverID", "-1");
		
		return defaults;
	}
	
	private static Map<String, String> checkParameters(Map<String, String> parameters) throws 
			HttpException
	{
		if (!parameters.containsKey("senderID"))
			throw new InvalidParametersException("Parameter 'senderID' required");
		
		// Checks that the sender exists
		UserEntity sender = new UserEntity(parameters.get("senderID"));
		
		// Checks the authorization
		FusrodahTable.checkUserKey(sender.getDatabaseID(), parameters);
		
		// Provides the end location if receiver is provided
		if (parameters.containsKey("receiverID"))
		{
			UserEntity receiver = new UserEntity(parameters.get("receiverID"));
			parameters.put("endLocation", receiver.getAttributes().get("location"));
		}
		else
		{
			try
			{
				new Location(parameters.get("endLocation"));
			}
			catch (ObjectFormatException e)
			{
				throw new InvalidParametersException(e.getMessage());
			}
		}
		
		// Checks that the location(s) can be parsed
		try
		{
			new Location(parameters.get("startLocation"));
		}
		catch (ObjectFormatException e)
		{
			throw new InvalidParametersException(e.getMessage());
		}
		
		parameters.put("lastShoutTime", new SimpleDate().toString());
		
		return parameters;
	}
}
