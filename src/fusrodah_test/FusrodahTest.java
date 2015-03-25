package fusrodah_test;

import java.io.FileNotFoundException;

import nexus_http.FileReaderClient;

/**
 * This class runs some test on the given server and prints the results
 * 
 * @author Mikko Hilpinen
 * @since 3.3.2015
 */
public class FusrodahTest
{
	// CONSTRUCTOR	--------------------------------
	
	private FusrodahTest()
	{
		// The interface is static
	}

	
	// MAIN METHOD	--------------------------------
	
	/**
	 * Runs a test on the given server
	 * @param args Server ip, server port (optional, default = 7777), testFileName 
	 * ('data/' automatically included, optional, default = basicTestInstructions.txt)
	 */
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.out.println("Please provide the correct arguments: host ip and port "
					+ "(default = 7777). You can also provide the test fileName as a "
					+ "third parameter.");
			System.exit(0);
		}
		
		int port = 7777;
		if (args.length > 1)
		{
			try
			{
				port = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException e)
			{
				System.err.println("Invalid port number.");
				System.exit(1);
			}
		}
		
		
		
		String fileName = "basicTestInstructions.txt";
		if (args.length > 2)
			fileName = args[2];
		
		try
		{
			new FileReaderClient("FusRoDahTest/1.1", args[0], port, 
					true).readFile(fileName, "*");
		}
		catch (FileNotFoundException e)
		{
			System.err.println("No such file");
			System.exit(1);
		}
	}
}
