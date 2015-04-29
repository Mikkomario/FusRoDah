package fusrodah_main;

import nexus_http.HttpException;
import fusrodah_rest.VictoryEntity;

/**
 * This maintenance task removes the completed templates after they have been on the server 
 * long enough
 * @author Mikko Hilpinen
 * @since 29.4.2015
 */
public class VictoryRemovalTask extends MaintenanceTask
{	
	// IMPLEMENTED METHODS	--------------------------

	@Override
	public int getMaintenanceIntervalMinutes()
	{
		// The maintenance is done once a day
		return 24 * 60;
	}

	@Override
	public void run()
	{
		// Removes any victory element that is too old
		try
		{
			for (String victoryID : VictoryEntity.getAllVictoryIDs())
			{
				VictoryEntity victory = new VictoryEntity(victoryID);
				if (victory.shouldBeRemoved())
					victory.delete();
			}
		}
		catch (HttpException e)
		{
			System.err.println("Failed to remove old victories");
			e.printStackTrace();
		}
	}
}
