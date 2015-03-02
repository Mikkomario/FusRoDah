package fusrodah_rest;

import java.util.HashMap;
import java.util.Map;

import fusrodah_main.FusrodahTable;
import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_rest.ImmutableRestData;
import nexus_rest.RestEntity;
import alliance_rest.DatabaseTableEntity;

/**
 * This list contains all the template entities
 * 
 * @author Mikko Hilpinen
 * @since 2.3.2015
 */
public class TemplateListEntity extends DatabaseTableEntity
{
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new template list entity
	 * @param parent
	 */
	public TemplateListEntity(RestEntity parent)
	{
		super("templates", new ImmutableRestData(new HashMap<>()), parent, 
				FusrodahTable.TEMPLATES);
	}
	
	
	// IMPLEMENTED METHODS	------------------------

	@Override
	protected RestEntity loadEntityWithID(String id) throws HttpException
	{
		return new ShoutTemplateEntity(id);
	}

	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		return new ShoutTemplateEntity(this, parameters);
	}

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.PUT);
	}

	@Override
	protected void prepareDelete(Map<String, String> parameters)
			throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.DELETE);
	}
}
