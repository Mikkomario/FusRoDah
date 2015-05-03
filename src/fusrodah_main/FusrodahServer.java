package fusrodah_main;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import alliance_authorization.LoginKeyRemovalTask;
import alliance_util.MaintenanceTask;
import alliance_util.MaintenanceTimer;
import alliance_util.SimpleDate;
import fusrodah_rest.FusRoDahLoginManagerEntity;
import fusrodah_rest.ShoutListEntity;
import fusrodah_rest.TemplateListEntity;
import fusrodah_rest.UsersListEntity;
import nexus_rest.ImmutableRestEntity;
import nexus_rest.RestEntity;
import nexus_rest.StaticRestServer;
import nexus_test.HttpServerAnalyzer;
import vault_database.DatabaseSettings;
import vault_database.DatabaseUnavailableException;

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
		if (args.length < 4)
		{
			System.out.println("Please provide the correct parameters (ip, port, encoding, "
					+ "password, user (optional), database address (optional))");
			System.exit(0);
		}
		
		String connectionTarget = "jdbc:mysql://localhost:3306/";
		String user = "root";
		
		if (args.length >= 6)
			connectionTarget = args[5];
		if (args.length >= 5)
			user = args[4];
		
		// Initializes database settings
		try
		{
			DatabaseSettings.initialize(connectionTarget, user, args[3], 1000, 
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
		serverAttributes.put("version", "1.07");
		serverAttributes.put("wisdom", 
				"Kuin kultarengas sian kärsässä on kaunis nainen, älyä vailla.");
		
		RestEntity root = new ImmutableRestEntity("root", null, serverAttributes);
		
		new FusRoDahLoginManagerEntity(root);
		new ShoutListEntity(root);
		new TemplateListEntity(root);
		new UsersListEntity(root);
		
		// Starts the maintenance tasks
		MaintenanceTimer maintenance = new MaintenanceTimer();
		maintenance.addTask(new TemplateRemovalTask(), 2);
		maintenance.addTask(new VictoryRemovalTask(), 
				MaintenanceTask.getMinutesTillMidnight() + 2 * 60);
		maintenance.addTask(new LoginKeyRemovalTask(FusrodahLoginTable.LOGINKEYS, 22));
		
		// Starts the server
		StaticRestServer.setRootEntity(root);
		StaticRestServer.setEventListener(new HttpServerAnalyzer());
		StaticRestServer.startServer(args);
	}
}
