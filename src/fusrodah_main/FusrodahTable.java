package fusrodah_main;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexus_http.HttpException;
import alliance_authorization.LoginKey;
import alliance_rest.DatabaseEntityTable;
import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;

/**
 * These are the tables used in the project
 * 
 * @author Mikko Hilpinen
 * @since 11.2.2015
 */
public enum FusrodahTable implements DatabaseEntityTable
{
	/**
	 * The shouts table contains the shout data
	 */
	SHOUTS,
	/**
	 * The templates table contains the shout template data
	 */
	TEMPLATES,
	/**
	 * The users table contains the user data
	 */
	USERS,
	/**
	 * The secure table contains the secure user data
	 */
	SECURE,
	/**
	 * The loginKeys table contains the login keys
	 */
	LOGINKEYS;
	
	
	// ATTRIBUTES	---------------------------
	
	private static Map<DatabaseTable, List<String>> columnNames = null;
	
	
	// IMPLEMENTED METHODS	-------------------

	@Override
	public List<String> getColumnNames()
	{
		try
		{
			if (columnNames == null)
				columnNames = new HashMap<>();
			
			if (!columnNames.containsKey(this))
				columnNames.put(this, DatabaseTable.readColumnNamesFromDatabase(this));
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			System.err.println("Failed to read the column names");
			e.printStackTrace();
		}
		
		return columnNames.get(this);
	}

	@Override
	public String getDatabaseName()
	{
		if (this == LOGINKEYS)
			return "fusrodah_management_db";
		
		return "fusrodah_db";
	}

	@Override
	public String getTableName()
	{
		if (this == LOGINKEYS)
			return "loginKeys";
		
		return toString().toLowerCase();
	}

	@Override
	public boolean usesAutoIncrementIndexing()
	{
		if (this == SECURE || this == LOGINKEYS)
			return false;
		
		return true;
	}

	@Override
	public boolean usesIndexing()
	{
		return true;
	}
	
	@Override
	public String getIDColumnName()
	{
		if (this == LOGINKEYS)
			return "userID";
		
		return "id";
	}
	
	
	// OTHER METHODS	-----------------------
	
	/**
	 * Checks if the given key is correct
	 * @param userID The identifier of the user
	 * @param parameters The parameters provided by the client
	 * @throws HttpException If the given key was incorrect or couldn't be validated
	 */
	public static void checkUserKey(String userID, Map<String, String> parameters) throws 
			HttpException
	{
		LoginKey.checkKey(LOGINKEYS, userID, "userKey", parameters);
	}
}
