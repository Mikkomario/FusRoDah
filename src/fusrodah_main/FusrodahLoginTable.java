package fusrodah_main;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import nexus_http.HttpException;
import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;
import alliance_authorization.LoginKey;
import alliance_authorization.LoginKeyTable;

/**
 * FusrodahLoginTable is used for logging in
 * @author Mikko Hilpinen
 * @since 3.5.2015
 */
public enum FusrodahLoginTable implements LoginKeyTable
{
	/**
	 * The loginKeys table contains the login keys
	 */
	LOGINKEYS;
	
	
	// ATTRIBUTES	----------------------------------
	
	private static List<String> columnNames = null;
	
	
	// IMPLEMENTED METHODS	--------------------------

	@Override
	public String getIDColumnName()
	{
		return "userID";
	}

	@Override
	public List<String> getColumnNames()
	{
		if (columnNames == null)
		{
			try
			{
				columnNames = DatabaseTable.readColumnNamesFromDatabase(this);
			}
			catch (DatabaseUnavailableException | SQLException e)
			{
				System.err.println("Failed to read the column names");
				e.printStackTrace();
			}
		}
		
		return columnNames;
	}

	@Override
	public String getDatabaseName()
	{
		return "fusrodah_management_db";
	}

	@Override
	public String getTableName()
	{
		return "loginKeys";
	}

	@Override
	public boolean usesAutoIncrementIndexing()
	{
		return false;
	}

	@Override
	public boolean usesIndexing()
	{
		return true;
	}

	@Override
	public String getCreationTimeColumnName()
	{
		return "created";
	}

	@Override
	public String getKeyColumnName()
	{
		return "userKey";
	}

	@Override
	public String getUserIDColumnName()
	{
		return getIDColumnName();
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
		LoginKey.checkKey(LOGINKEYS, userID, parameters);
	}
}
