package fusrodah_rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fusrodah_main.FusrodahTable;
import fusrodah_main.SimpleDate;
import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.RestEntityList;
import nexus_rest.SimpleRestData;
import nexus_rest.SimpleRestEntityList;
import alliance_rest.DatabaseEntity;

/**
 * A victory entity represents a completed shout chain that has reached its destination
 * @author Mikko Hilpinen
 * @since 28.4.2015
 */
public class VictoryEntity extends DatabaseEntity
{
	// ATTRIBUTES	--------------------------
	
	private static final String ROOTPATH = "root/victories/";
	
	
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

	
	// OTHER METHODS	------------------------
	
	private ShoutTemplateEntity getTemplate() throws HttpException
	{
		return new ShoutTemplateEntity(getAttributes().get("templateID"));
	}
	
	private RestEntityList getReceivers() throws HttpException
	{
		String[] receiverIDs = getAttributes().get("receiverIDs").split("\\+");
		List<RestEntity> receivers = new ArrayList<>();
		for (int i = 0; i < receiverIDs.length; i++)
		{
			receivers.add(new UserEntity(receiverIDs[i]));
		}
		
		return new SimpleRestEntityList("receivers", this, receivers);
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
