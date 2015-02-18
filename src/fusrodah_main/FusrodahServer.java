package fusrodah_main;

import java.sql.SQLException;

import nexus_rest.RestEntity;
import nexus_rest.StaticRestServer;
import nexus_test.TestRestEntity;
import vault_database.DatabaseSettings;
import vault_database.DatabaseUnavailableException;
import alliance_authorization.LoginManagerEntity;
import alliance_authorization.PasswordChecker;

/**
 * This is the main class for the project. The server can be started through this class.
 * 
 * @author Mikko Hilpinen
 * @since 11.2.2015
 */
public class FusrodahServer
{
	// CONSTRUCTOR	----------------------------
	
	private FusrodahServer()
	{
		// The interface is static
	}

	
	// MAIN METHOD	----------------------------
	
	/**
	 * Starts the server
	 * @param args The first parameter is the server ip. The second parameter is the port 
	 * number. The third parameter is the database password. The fourth one is database user 
	 * (default = root). The fifth is database address (default = jdbc:mysql://localhost:3306/)
	 */
	public static void main(String[] args)
	{
		if (args.length < 3)
		{
			System.out.println("Please provide the correct parameters (ip, port, password, "
					+ "user (optional), database address (optional)");
			System.exit(0);
		}
		
		String connectionTarget = "jdbc:mysql://localhost:3306/";
		String user = "root";
		
		if (args.length >= 5)
			connectionTarget = args[4];
		if (args.length >= 4)
			user = args[3];
		
		// Initializes database settings
		try
		{
			DatabaseSettings.initialize(connectionTarget, user, args[2], 1000, 
					"fusrodah_management_db", "tableamounts");
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			System.err.println("Couldn't initialize the database settings");
			e.printStackTrace();
			System.exit(1);
		}
		
		// Creates the server entities
		RestEntity root = new TestRestEntity("root", null);
		// TODO: Replace with new entities once done
		//new TestTableEntity("entities", root);
		new LoginManagerEntity("login", root, new PasswordChecker(FusrodahTable.SECURE, 
				"passwordHash", "id"));
		
		// Starts the server
		StaticRestServer.setRootEntity(root);
		StaticRestServer.startServer(args);
	}
}
