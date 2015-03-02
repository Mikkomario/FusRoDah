package fusrodah_rest;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import fusrodah_main.FusrodahTable;
import fusrodah_main.Location;
import nexus_http.HttpException;
import nexus_http.InvalidParametersException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.SimpleRestData;
import alliance_authorization.SecureEntity;
import alliance_rest.DatabaseEntity;

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
		links.put("secure", new Secure());
		
		return links;
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		if (pathPart.equals("secure"))
			return new Secure();
		
		throw new NotFoundException(getPath() + "/" + pathPart);
	}
	
	@Override
	protected void prepareDelete(Map<String, String> parameters) throws HttpException
	{
		super.prepareDelete(parameters);
		
		// Also deletes the secure
		new Secure().delete(parameters);
	}
	
	@Override
	public void writeContent(String serverLink, XMLStreamWriter writer, 
			Map<String, String> parameters) throws HttpException, XMLStreamException
	{
		// The content can only be written with proper authorization
		FusrodahTable.checkUserKey(getDatabaseID(), parameters);
		super.writeContent(serverLink, writer, parameters);
	}

	
	// OTHER METHODS	-------------------------------
	
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
	
	private static Map<String, String> checkParameters(Map<String, String> parameters) 
			throws HttpException
	{
		// TODO: Check that the userName is not already in use
		
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
		
		return parameters;
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
			FusrodahTable.checkUserKey(getDatabaseID(), parameters);
		}
	}
}
