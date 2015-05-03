package fusrodah_main;

import java.sql.SQLException;
import java.util.List;

import nexus_http.HttpException;
import fusrodah_rest.ShoutTemplateEntity;
import alliance_rest.DatabaseEntityTable;
import alliance_util.MaintenanceTask;
import vault_database.DatabaseUnavailableException;

/**
 * This task removes the old templates
 * @author Mikko Hilpinen
 * @since 12.4.2015
 */
public class TemplateRemovalTask extends MaintenanceTask
{
	// IMPLEMENTED METHODS	--------------------------------

	@Override
	public int getMaintenanceIntervalMinutes()
	{
		return 90;
	}

	@Override
	public void run()
	{
		// Finds out all the id's and last shout times of the existing templates
		try
		{
			List<String> templateIDs = DatabaseEntityTable.findMatchingIDs(
					FusrodahTable.TEMPLATES, new String[0], new String[0]);
			
			// Deletes old templates that can't be shouted but are not complete either
			for (String templateID : templateIDs)
			{
				ShoutTemplateEntity template = new ShoutTemplateEntity(templateID);
				if (!template.isCompleted() && !template.canBeShouted())
					template.delete();
			}
		}
		catch (DatabaseUnavailableException | SQLException | HttpException e)
		{
			System.err.println("Failed to remove old templates");
			e.printStackTrace();
		}
	}
}
