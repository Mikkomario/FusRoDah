package fusrodah_rest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vault_database.DatabaseUnavailableException;
import flow_recording.ObjectFormatException;
import fusrodah_main.ForbiddenActionException;
import fusrodah_main.FusrodahLoginTable;
import fusrodah_main.FusrodahTable;
import fusrodah_main.Location;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.ImmutableRestData;
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
	
	@Override
	protected RestEntity getMissingEntity(String pathPart, Map<String, String> parameters) 
			throws HttpException
	{
		// Best is a valid entity under this one
		if (pathPart.equalsIgnoreCase("best"))
			return new BestShoutList(this, parameters);
		
		return super.getMissingEntity(pathPart, parameters);
	}
	
	@Override
	protected Map<String, RestEntity> getMissingEntities(Map<String, String> parameters) 
			throws HttpException
	{
		Map<String, RestEntity> entities = super.getMissingEntities(parameters);
		if (entities == null)
			entities = new HashMap<>();
		
		entities.put("best", new BestShoutList(this, parameters));
		
		return entities;
	}

	
	// SUBCLASSES	--------------------------------
	
	private static class BestShoutList extends RestEntityList
	{
		// ATTRIBUTES	----------------------------
		
		private List<RestEntity> bestEntities;
		private UserEntity user;
		private Location location;
		
		
		// CONSTRUCTOR	----------------------------
		
		public BestShoutList(RestEntity parent, Map<String, String> parameters) throws 
				HttpException
		{
			super("best", parent);
			
			this.bestEntities = null;
			
			// Checks the parameters ('userID' and 'location') required
			if (!parameters.containsKey("userID") || !parameters.containsKey("location"))
				throw new InvalidParametersException(
						"Parameters 'userID' and 'location' required");
			
			// Checks that the user is valid
			this.user = new UserEntity(parameters.get("userID"));
			try
			{
				this.location = new Location(parameters.get("location"));
			}
			catch (ObjectFormatException e)
			{
				throw new InvalidParametersException(e.getMessage());
			}
			
			// And checks for authorization
			FusrodahLoginTable.checkUserKey(this.user.getDatabaseID(), parameters);
			
			// Checks if the user is still on cooldown
			if (!this.user.canShout())
				throw new ForbiddenActionException("The user is still on a cooldown period.");
			
			// Also updates user location
			this.user.updateLocation(this.location);
		}
		
		
		// IMPLEMENTED METHODS	-------------------

		@Override
		protected List<RestEntity> getEntities() throws HttpException
		{
			// If the entities haven't bee requested yet, finds them
			if (this.bestEntities == null)
			{
				List<ShoutEntity> bestShouts = findBestShouts(this.location, 
						this.user.getDatabaseID());
				this.bestEntities = new ArrayList<>();
				this.bestEntities.addAll(bestShouts);
			}
			
			return this.bestEntities;
		}

		@Override
		public void trim(Map<String, String> parameters)
		{
			// No trimming required
		}

		@Override
		public void Put(Map<String, String> parameters) throws HttpException
		{
			throw new MethodNotSupportedException(MethodType.PUT);
		}
		
		
		// OTHER METHODS	-------------------------
		
		private static List<ShoutEntity> findBestShouts(Location location, String userID) 
				throws HttpException
		{
			try
			{
				// Finds all possible shoutIDs
				List<String> shoutIDs = DatabaseEntityTable.findMatchingIDs(
						FusrodahTable.SHOUTS, new String[0], new String[0]);
				
				List<ShoutEntity> bestShouts = new ArrayList<>();
				
				// Goes through the shouts
				for (String shoutID : shoutIDs)
				{
					ShoutEntity shout = new ShoutEntity(shoutID);
					
					// Checks if the shout can be heard at all
					if (!shout.isValidFor(location, userID))
						continue;
					
					// Tries to place the shout to the list of best shouts
					for (int i = 0; i < 3; i++)
					{
						if (bestShouts.size() <= i)
						{
							bestShouts.add(shout);
							break;
						}
						else if (shoutIsBetterThanAnother(shout, bestShouts.get(i)))
						{
							bestShouts.add(i, shout);
							
							if (i == 2 || bestShouts.size() > 3)
								bestShouts.remove(3);
							
							break;
						}
					}
				}
				
				return bestShouts;
			}
			catch (DatabaseUnavailableException | SQLException e)
			{
				throw new InternalServerException("Couldn't read the shout IDs", e);
			}
			catch (NotFoundException e)
			{
				throw new InternalServerException("Couldn't create a shout", e);
			}
		}
		
		private static boolean shoutIsBetterThanAnother(ShoutEntity shout, 
				ShoutEntity another) throws HttpException
		{
			// TODO: Implement this (Also, this may be better at the shout class body)
			// Things to consider:
			// - Number of shouters
			// - Last shout time
			// - Direction
			// - Distance
			// - % complete
			return another.getShoutTime().isPast(shout.getShoutTime());
		}
	}
}
