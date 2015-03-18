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
 * This class works as an accessor for the user entities
 * 
 * @author Mikko Hilpinen
 * @since 15.2.2015
 */
public class UsersListEntity extends DatabaseTableEntity
{
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new users list under the given entity
	 * @param parent The parent entity for this list
	 */
	public UsersListEntity(RestEntity parent)
	{
		super("users", new ImmutableRestData(new HashMap<>()), parent, FusrodahTable.USERS);
	}
	
	
	// IMPLEMENTED METHODS	-----------------------

	@Override
	protected RestEntity loadEntityWithID(String id) throws HttpException
	{
		return new UserEntity(id);
	}

	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		return new UserEntity(this, parameters);
	}

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		// Not supported
		throw new MethodNotSupportedException(MethodType.PUT);
	}

	@Override
	protected void prepareDelete(Map<String, String> parameters) throws HttpException
	{
		// Not supported
		throw new MethodNotSupportedException(MethodType.DELETE);
	}
}
