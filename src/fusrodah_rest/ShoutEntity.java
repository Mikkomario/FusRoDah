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
	
	private static Map<String, String> checkParameters(Map<String, String> parameters) throws 
			HttpException
	{
		// Adds the "created" parameter itself
		parameters.put("created", new SimpleDate().toString());
		
		// If parameter "lastShoutID" has been provided, uses the data of that shout
		if (parameters.containsKey("lastShoutID"))
		{
			Map<String, String> lastShoutData = 
					new ShoutEntity(parameters.get("lastShoutID")).getAttributes();
			
			if (parameters.containsKey("shouterID"))
				parameters.put("shouterIDs", lastShoutData.get("shouterIDs") + "+" + 
						parameters.get("shouterID"));
			else
				parameters.put("shouterIDs", lastShoutData.get("shouterIDs") + "+" + 
						parameters.get("shouterIDs"));
			
			parameters.put("templateID", lastShoutData.get("templateID"));
		}
		else if (parameters.containsKey("shouterID"))
			parameters.put("shouterIDs", parameters.get("shouterID"));
		
		return parameters;
	}
	
	private RestEntityList getShouters() throws HttpException
	{
		// Parses the ids from the shouterIDs
		String[] shouterIDs = getAttributes().get("shouterIDs").split("+");
		List<RestEntity> shouters = new ArrayList<>();
		
		for (int i = 0; i < shouterIDs.length; i++)
		{
			// TODO: Handle this more carefully?
			shouters.add(new UserEntity(shouterIDs[i]));
		}
		
		return new SimpleRestEntityList("shouters", this, shouters);
	}
}
