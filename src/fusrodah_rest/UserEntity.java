package fusrodah_rest;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vault_database.DatabaseAccessor;
import vault_database.DatabaseUnavailableException;
import fusrodah_main.FusrodahLoginTable;
import fusrodah_main.FusrodahTable;
import fusrodah_main.Location;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.RestEntityLinkList;
import nexus_rest.SimpleRestData;
import alliance_authorization.SecureEntity;
import alliance_rest.DatabaseEntity;
import alliance_util.SimpleDate;

/**
 * User entity represents a single user in the service
 * 
 * @author Mikko Hilpinen
 * @since 11.2.2015
 */
public class UserEntity extends DatabaseEntity
{
	// ATTRIBUTES	------------------------
	
	/**
	 * The path preceding each user entity
	 */
	private static final String ROOTPATH = "root/users/";
	private static final int shoutDelayMinutes = 15;
	
	
	// CONSTRUCTOR	---------------------------------
	
	/**
	 * Creates a new user by reading its data from the database
	 * @param id The identifier of the entity
	 * @throws HttpException If the entity couldn't be created
	 */
	public UserEntity(String id) throws HttpException
	{
		super(new SimpleRestData(), ROOTPATH, FusrodahTable.USERS, id);
	}

	/**
	 * Creates a new user and writes their data to the database
	 * @param parent The parent of this entity
	 * @param parameters The parameters provided by the client
	 * @throws HttpException If the user couldn't be created or written
	 */
	public UserEntity(RestEntity parent, Map<String, String> parameters) throws HttpException
	{
		super(new SimpleRestData(), parent, FusrodahTable.USERS, 
				checkParameters(parameters), new HashMap<>());
		
		// Also creates the secure entity for the user
		new Secure(parameters);
	}
	
	
	// IMPLEMENTED METHODS	---------------------------------

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
		Secure secure = new Secure();
		VictoryEntityList victories = new VictoryEntityList(this);
		
		links.put(secure.getName(), secure);
		links.put(victories.getName(), victories);
		
		return links;
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		if (pathPart.equalsIgnoreCase("secure"))
			return new Secure();
		else if (pathPart.equalsIgnoreCase("victories"))
			return new VictoryEntityList(this);
		
		throw new NotFoundException(getPath() + "/" + pathPart);
	}
	
	@Override
	protected void prepareDelete(Map<String, String> parameters) throws HttpException
	{
		super.prepareDelete(parameters);
		
		// Also deletes the secure
		new Secure().delete(parameters);
	}

	
	// OTHER METHODS	-------------------------------
	
	/**
	 * @return Can the user post a new shout at this time
	 * @throws HttpException If the operation failed
	 */
	public boolean canShout() throws HttpException
	{
		SimpleDate lastShoutTime;
		try
		{
			lastShoutTime = new SimpleDate(getAttributes().get("lastShoutTime"));
		}
		catch (ParseException e)
		{
			throw new InternalServerException("Failed to check user shout time", e);
		}
		return (new SimpleDate().isPast(lastShoutTime.plus(shoutDelayMinutes)));
	}
	
	/**
	 * Updates the user's new location to the database
	 * @param newLocation The user's new location
	 * @throws HttpException If the update couldn't be performed
	 */
	public void updateLocation(Location newLocation) throws HttpException
	{
		setAttribute("location", newLocation.toString());
		writeData();
	}
	
	/**
	 * Marks the current moment as the user's latest shout time
	 * @throws HttpException If the update couldn't be performed
	 */
	public void updateLastShoutTime() throws HttpException
	{
		setAttribute("lastShoutTime", new SimpleDate().toString());
		writeData();
	}
	
	/**
	 * Gives some points to the user
	 * @param increment How many points are given to this player
	 * @throws HttpException If the update couldn't be performed
	 */
	public void addPoints(int increment) throws HttpException
	{
		int newPoints = Integer.parseInt(getAttributes().get("points")) + increment;
		
		setAttribute("points", "" + newPoints);
		writeData();
	}
	
	private static Map<String, String> checkParameters(Map<String, String> parameters) 
			throws HttpException
	{
		if (!parameters.containsKey("userName"))
			throw new InvalidParametersException("Parameter 'userName' required");
		
		// Checks if the userName is already in use
		if (UserEntity.userNameInUse(parameters.get("userName")))
			throw new InvalidParametersException("UserName already in use");
		
		// Sets the points to zero
		parameters.put("points", "0");
		
		// Checks that the parameter 'password' exists
		if (!parameters.containsKey("password"))
			throw new InvalidParametersException("Parameter 'password' required");
		
		// Also checks the userName
		if (parameters.containsKey("userName") && 
				Character.isDigit(parameters.get("userName").charAt(0)))
			throw new InvalidParametersException(
					"Parameter 'userName' must not start with a digit");
		
		// Sets the last shout time to default as well
		parameters.put("lastShoutTime", new SimpleDate(new Date(0)).toString());
		
		return parameters;
	}
	
	private static boolean userNameInUse(String userName) throws HttpException
	{
		try
		{
			List<String> matches = DatabaseAccessor.findMatchingData(FusrodahTable.USERS, 
					"userName", userName, "id");
			return !matches.isEmpty();
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			throw new InternalServerException("Failed to check existing userNames", e);
		}
	}
	
	
	// SUBCLASSES	--------------------------------
	
	/**
	 * This is the secure hashed information for the user entity, a separate entity below 
	 * the user.
	 * 
	 * @author Mikko Hilpinen
	 * @since 13.2.2015
	 */
	public class Secure extends SecureEntity
	{
		// CONSTRUCTOR	------------------------------
		
		/**
		 * Creates a new secure entity by reading its data from the database
		 * @throws HttpException If the entity couldn't be read
		 */
		public Secure() throws HttpException
		{
			super(FusrodahTable.SECURE, UserEntity.this.getPath() + "/", "secure", 
					UserEntity.this.getDatabaseID(), "passwordHash", "password");
		}

		/**
		 * Creates a new secure and writes it to the database
		 * @param parameters The parameters provided by the client
		 * @throws HttpException If the entity couldn't be created or written
		 */
		private Secure(Map<String, String> parameters) throws HttpException
		{
			super(FusrodahTable.SECURE, UserEntity.this, "secure", 
					UserEntity.this.getDatabaseID(), "passwordHash", "password", parameters);
		}
		
		
		// IMPLEMENTED METHODS	--------------------------------

		@Override
		protected void authorizeModification(Map<String, String> parameters)
				throws HttpException
		{
			FusrodahLoginTable.checkUserKey(getDatabaseID(), parameters);
		}
	}
	
	private static class VictoryEntityList extends RestEntityLinkList
	{
		// ATTRIBUTES	-------------------------
		
		private List<RestEntity> victories;
		private String userID;
		
		
		// CONSTRUCTOR	-------------------------
		
		public VictoryEntityList(UserEntity user)
		{
			super("victories", user);
			
			this.userID = user.getDatabaseID();
		}
		
		
		// IMPLEMENTED METHODS	-----------------

		@Override
		protected List<RestEntity> getEntities() throws HttpException
		{
			if (this.victories == null)
			{
				List<VictoryEntity> foundVictories = findGainedVictories();
				this.victories = new ArrayList<>();
				this.victories.addAll(foundVictories);
			}
			return this.victories;
		}

		@Override
		public void trim(Map<String, String> parameters)
		{
			// No trimming required
		}
		
		
		// OTHER METHODS	----------------------
		
		private List<VictoryEntity> findGainedVictories() throws HttpException
		{
			List<VictoryEntity> victories = new ArrayList<>();
			// For each victory, checks if the user has collaborated
			for (String victoryID : VictoryEntity.getAllVictoryIDs())
			{
				VictoryEntity victory = new VictoryEntity(victoryID);
				if (victory.hasCollaborated(this.userID))
					victories.add(victory);
			}
			
			return victories;
		}
	}
}
