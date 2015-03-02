package fusrodah_main;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import fusrodah_rest.ShoutListEntity;
import fusrodah_rest.TemplateListEntity;
import fusrodah_rest.UsersListEntity;
import nexus_http.FileReaderClient;
import nexus_rest.ImmutableRestEntity;
import nexus_rest.RestEntity;
import nexus_rest.StaticRestServer;
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
	 * number. The third parameter is the database password. The fourth is whether to run the 
	 * tests at the start of the program (default = false), The fourth one is database user 
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
		boolean test = false;
		
		if (args.length >= 6)
			connectionTarget = args[5];
		if (args.length >= 5)
			user = args[4];
		if (args.length >= 4)
			test = Boolean.parseBoolean(args[3]);
		
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
		Map<String, String> serverAttributes = new HashMap<>();
		serverAttributes.put("started", new SimpleDate().toString());
		serverAttributes.put("version", "1.0");
		
		RestEntity root = new ImmutableRestEntity("root", null, serverAttributes);
		
		new LoginManagerEntity("login", root, new PasswordChecker(FusrodahTable.SECURE, 
				"passwordHash", "id"));
		new ShoutListEntity(root);
		new TemplateListEntity(root);
		new UsersListEntity(root);
		
		// Starts the server
		StaticRestServer.setRootEntity(root);
		StaticRestServer.startServer(args);
		
		// If the testing is set to true, runs the test
		if (test)
		{
			try
			{
				new FileReaderClient("Test/1.1", args[0], Integer.parseInt(args[1]), 
						true).readFile("basicTestInstructions.txt", "*");
			}
			catch (NumberFormatException | FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
}
