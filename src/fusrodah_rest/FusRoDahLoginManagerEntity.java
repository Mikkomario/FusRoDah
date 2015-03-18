package fusrodah_rest;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import vault_database.DatabaseAccessor;
import vault_database.DatabaseUnavailableException;
import fusrodah_main.FusrodahTable;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_rest.RestEntity;
import alliance_authorization.LoginManagerEntity;
import alliance_authorization.PasswordChecker;

/**
 * FusRoDahLoginManagerEntity allows login with just a userName instead of an userID
 * @author Mikko Hilpinen
 * @since 18.3.2015
 */
public class FusRoDahLoginManagerEntity extends LoginManagerEntity
{
	// CONSTRUCTOR	------------------------
	
	/**
	 * Creates a new loginManagerEntity below the given entity
	 * @param parent The entity over this entity
	 */
	public FusRoDahLoginManagerEntity(RestEntity parent)
	{
		super("login", parent, FusrodahTable.LOGINKEYS, "userKey", 
				new PasswordChecker(FusrodahTable.SECURE, "passwordHash", "id"));
	}
	
	
	// IMPLEMENTED METHODS	-------------------
	
	@Override
	protected RestEntity getMissingEntity(String pathPart, Map<String, String> parameters) 
			throws HttpException
	{
		// Accepts userNames as well
		if (!pathPart.isEmpty() && !Character.isDigit(pathPart.charAt(0)))
		{
			try
			{
				List<String> matchingIDs = DatabaseAccessor.findMatchingData(
						FusrodahTable.USERS, "userName", pathPart, 
						FusrodahTable.USERS.getIDColumnName());
				if (!matchingIDs.isEmpty())
					return super.getMissingEntity(matchingIDs.get(0), parameters);
			}
			catch (DatabaseUnavailableException | SQLException e)
			{
				throw new InternalServerException("Couldn't search through the users", e);
			}
		}
		
		return super.getMissingEntity(pathPart, parameters);
	}
}
