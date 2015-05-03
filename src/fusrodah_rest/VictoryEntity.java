package fusrodah_rest;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vault_database.DatabaseUnavailableException;
import fusrodah_main.FusrodahTable;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.RestEntityList;
import nexus_rest.SimpleRestData;
import nexus_rest.SimpleRestEntityLinkList;
import alliance_rest.DatabaseEntity;
import alliance_rest.DatabaseEntityTable;
import alliance_util.SimpleDate;

/**
 * A victory entity represents a completed shout chain that has reached its destination
 * @author Mikko Hilpinen
 * @since 28.4.2015
 */
public class VictoryEntity extends DatabaseEntity
{
	// ATTRIBUTES	--------------------------
	
	private static final String ROOTPATH = "root/victories/";
	// Victories last for a week on the server
	private static final int VICTORY_DURATION_MINUTES = 7 * 24 * 60;
	
	
	// CONSTRUCTOR	--------------------------
	
	/**
	 * Creates a new victory entity by loading it from the database
	 * @param id The unique identifier of the victory entity
	 * @throws HttpException If the entity couldn't be read
	 */
	public VictoryEntity(String id) throws HttpException
	{
		super(new SimpleRestData(), ROOTPATH, FusrodahTable.VICTORIES, id);
	}

	/**
	 * Registers a new victory
	 * @param victoryShout The shout that caused the template to be completed
	 * @throws HttpException If the creation of the entity failed
	 */
	public VictoryEntity(ShoutEntity victoryShout) throws HttpException
	{
		super(new SimpleRestData(), victoryShout, FusrodahTable.VICTORIES, 
				generateParameters(victoryShout), new HashMap<>());
	}
	
	
	// IMPLEMENTED METHODS	------------------
	
	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.PUT);
	}

	@Override
	protected Map<String, RestEntity> getMissingEntities(
			Map<String, String> parameters) throws HttpException
	{
		Map<String, RestEntity> linkedEntities = new HashMap<>();
		
		linkedEntities.put("receivers", getReceivers());
		linkedEntities.put("template", getTemplate());
		
		return linkedEntities;
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		if (pathPart.equalsIgnoreCase("template"))
			return getTemplate();
		else if (pathPart.equalsIgnoreCase("receivers"))
			return getReceivers();
		
		throw new NotFoundException(getPath() + "/" + pathPart);
	}
	
	@Override
	protected void prepareDelete(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.DELETE);
		// Checks for authorizarion
		//FusrodahTable.checkUserKey(getTemplate().getSenderID(), parameters);
		
		// TODO: Should a collaborator be deleted instead? You shouldn't delete a shared resource
		
		// Deletes any template connected to this entity
		//delete();
	}

	
	// OTHER METHODS	------------------------
	
	/**
	 * Checks if a user has received points from this victory
	 * @param userID The identifier of the user
	 * @return Has the given user received points from this victory
	 */
	public boolean hasCollaborated(String userID)
	{
		String[] receiverIDs = getReceiverIDs();
		for (int i = 0; i < receiverIDs.length; i++)
		{
			if (receiverIDs[i].equals(userID))
				return true;
		}
		
		return false;
	}
	
	/**
	 * @return Is the entity old enough to be removed from the server
	 * @throws HttpException If the operation failed
	 */
	public boolean shouldBeRemoved() throws HttpException
	{
		try
		{
			return new SimpleDate().isPast(new SimpleDate(getAttributes().get("created")).plus(
					VICTORY_DURATION_MINUTES));
		}
		catch (ParseException e)
		{
			throw new InternalServerException("Couldn't define victory creation time", e);
		}
	}
	
	/**
	 * Deletes the entity without authorization. Also removes the connected template
	 * @throws HttpException If the operation failed
	 */
	public void delete() throws HttpException
	{
		getTemplate().delete();
		super.prepareDelete(new HashMap<>());
	}
	
	/**
	 * @return A list containing all the victory ids that are currently in use
	 * @throws HttpException If the ids couldn't be read for some reason
	 */
	public static List<String> getAllVictoryIDs() throws HttpException
	{
		try
		{
			return DatabaseEntityTable.findMatchingIDs(
					FusrodahTable.VICTORIES, new String[0], new String[0]);
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			throw new InternalServerException("Couldn't read the victory ids", e);
		}
	}
	
	private ShoutTemplateEntity getTemplate() throws HttpException
	{
		return new ShoutTemplateEntity(getAttributes().get("templateID"));
	}
	
	private RestEntityList getReceivers() throws HttpException
	{
		String[] receiverIDs = getReceiverIDs();
		List<RestEntity> receivers = new ArrayList<>();
		for (int i = 0; i < receiverIDs.length; i++)
		{
			receivers.add(new UserEntity(receiverIDs[i]));
		}
		
		return new SimpleRestEntityLinkList("receivers", this, receivers);
	}
	
	private String[] getReceiverIDs()
	{
		return getAttributes().get("receiverIDs").split("\\+");
	}
	
	private static Map<String, String> generateParameters(ShoutEntity shout) throws HttpException
	{
		Map<String, String> parameters = new HashMap<>();
		
		parameters.put("created", new SimpleDate().toString());
		
		// Updates the template
		String templateID = shout.getAttributes().get("templateID");
		ShoutTemplateEntity template = new ShoutTemplateEntity(templateID);
		parameters.put("templateID", templateID);
		int receivedPoints = template.calculateGainedPoints();
		parameters.put("receivedPoints", "" + receivedPoints);
		template.markCompleted();
		
		// TODO: What if the template was already complete?
		
		// Also adds points to all contributed players
		parameters.put("receiverIDs", shout.getAttributes().get("shouterIDs"));
		String[] receiverIDs = shout.getShouterIds();
		for (int i = 0; i < receiverIDs.length; i++)
		{
			new UserEntity(receiverIDs[i]).addPoints(receivedPoints);
		}
		
		return parameters;
	}
}
