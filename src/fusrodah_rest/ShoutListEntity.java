package fusrodah_rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fusrodah_main.FusrodahTable;
import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_rest.ImmutableRestData;
import nexus_rest.RestData;
import nexus_rest.RestEntity;
import nexus_rest.RestEntityList;
import alliance_rest.DatabaseEntityTable;
import alliance_rest.DatabaseTableEntity;

/**
 * This entity works as an accessor for the shouts
 * 
 * @author Mikko Hilpinen
 * @since 15.2.2015
 */
public class ShoutListEntity extends DatabaseTableEntity
{
	// CONSTRUCTOR	------------------------------
	
	/**
	 * Creates a new shout list below the given entity
	 * @param parent The parent of this entity
	 */
	public ShoutListEntity(RestEntity parent)
	{
		super("shouts", new ImmutableRestData(new HashMap<>()), parent, FusrodahTable.SHOUTS);
	}

	
	// IMPLEMENTED METHODS	--------------------------
	
	@Override
	protected RestEntity loadEntityWithID(String id) throws HttpException
	{
		return new ShoutEntity(id);
	}

	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		return new ShoutEntity(this, parameters);
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

	
	// SUBCLASSES	--------------------------------
	
	private static class BestShoutList extends RestEntityList
	{
		// TODO: Change entityLists so that they add the data into the list only when it is 
		// requested / required (at nexus)
		
		// CONSTRUCTOR	----------------------------
		
		public BestShoutList(RestEntity parent,
				List<RestEntity> initialEntities)
		{
			super(name, parent, initialEntities);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void trim(Map<String, String> parameters)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void Put(Map<String, String> parameters) throws HttpException
		{
			// TODO Auto-generated method stub
			
		}
		
	}
}
