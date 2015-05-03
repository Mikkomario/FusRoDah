package fusrodah_main;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * The victories table contains victory data for completed shouts
	 */
	VICTORIES;
	
	
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
		return "fusrodah_db";
	}

	@Override
	public String getTableName()
	{	
		return toString().toLowerCase();
	}

	@Override
	public boolean usesAutoIncrementIndexing()
	{
		if (this == SECURE)
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
		return "id";
	}
}
