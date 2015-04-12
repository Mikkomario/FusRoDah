package fusrodah_main;

import java.util.TimerTask;

/**
 * Management tasks are repeated constantly as long as the server is active
 * @author Mikko Hilpinen
 * @since 9.4.2015
 */
public abstract class MaintenanceTask extends TimerTask
{
	// ABSTRACT METHODS	------------------------
	
	/**
	 * @return How many minutes there are between each maintenance task
	 */
	public abstract int getMaintenanceIntervalMinutes();
}
