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
 * This list provides access to all victory elements that are currently available
 * @author Mikko Hilpinen
 * @since 29.4.2015
 */
public class VictoryListEntity extends DatabaseTableEntity
{
	// CONSTRUCTOR	------------------------------
	
	/**
	 * Creates a new victory list
	 * @param parent The element above this element
	 */
	public VictoryListEntity(RestEntity parent)
	{
		super("victories", new ImmutableRestData(new HashMap<>()), parent, 
				FusrodahTable.VICTORIES);
	}
	
	
	// IMPLEMENTED METHODS	---------------------

	@Override
	protected RestEntity loadEntityWithID(String id) throws HttpException
	{
		return new VictoryEntity(id);
	}

	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		throw new MethodNotSupportedException(MethodType.POST);
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
