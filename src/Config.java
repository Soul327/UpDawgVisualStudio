import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

import Misc.GetSystemInfo;

public class Config {
	// Local config file variables
	static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ssa MM/dd/YYYY");
	
	// General
	public static File    programFolder         = new File("").getAbsoluteFile();
	public static File    configFile            = new File("local\\settings.txt");
	public static File    currentLogFile        = null;
	public static long    checkUpdateTime       = 60_000 * 10;
	
	//Ping Information
	public static boolean nmap                  = false;
	public static int     threadCount           = 10;
	public static int     pingTimeOutTime       = 1000;
	
	// Local Java Server
	public static boolean ljs_enable            = false;
	public static int     ljs_port              = 1919; // Server
	public static String  ljs_address           = "127.0.0.1"; // Client
	public static int     passDataTime          = 1000;
	public static int     pingCheckerTime       = 5000;
	public static int     tempServerRequestTime = 60000;
	
	// Local Window Client
	public static int     swFontSize            = 15;
	public static boolean localWindowClient     = false;
	
	// SQL
	public static boolean sql_enable            = true;
	public static boolean sql_lazy              = false;
	public static boolean sql_getAddresses      = true;
	public static boolean sql_updateAddresses   = false;
	public static int     sql_updateTime        = 1000 * 10; // In milliseconds, lazy SQL
	public static String  sql_key               = "";

	// Frame data
	public static boolean isMinimized;
	
	public static ArrayList<String> groups = new ArrayList<String>();
	
	/**
	 * Loads the config file
	 */
	public static void init() {
		

		// Move old config file as the old config file had an issue with .gitignore
		File oldConfig = new File("UpDawg.config");
		if( oldConfig.exists() ) oldConfig.renameTo( configFile );

		// Change location of config file if not on windows
		if(!GetSystemInfo.isWindows())
			configFile = new File("UpDawg.config\n");
		
		
		try {
			UpDawgLauncher.log("Loading config\n");
			if(!configFile.exists()) {
				UpDawgLauncher.log("No config file found.\n");
				if(configFile.getParentFile() != null)
					configFile.getParentFile().mkdirs();
				configFile.createNewFile();
				return;
			}
			Scanner scanner = new Scanner(configFile);
			
			while(scanner.hasNextLine()) {
				String temp = "";// Init variable
				String line = scanner.nextLine();// Get next line from file
				
				// Local Window Client
				if(line.startsWith(temp = "SWfontSize "         )) swFontSize          = Integer.parseInt( line.replaceAll(temp, "") );
				if(line.startsWith(temp = "localWindowClient "  )) localWindowClient   = line.contains("TRUE") || line.contains("true");
				
				// SQL 
				if(line.startsWith(temp = "sql_enable"          )) sql_enable          = line.contains("TRUE") || line.contains("true");
				if(line.startsWith(temp = "sql_lazy"            )) sql_lazy            = line.contains("TRUE") || line.contains("true");
				if(line.startsWith(temp = "sql_getAddresses"    )) sql_getAddresses    = line.contains("TRUE") || line.contains("true");
				if(line.startsWith(temp = "sql_updateAddresses" )) sql_updateAddresses = line.contains("TRUE") || line.contains("true");
				if(line.startsWith(temp = "sql_key "            )) sql_key             = line.replaceAll(temp, "");
				
				// Ping information
				if(line.startsWith(temp = "pingTimeOutTime "    )) pingTimeOutTime     = Integer.parseInt( line.replaceAll(temp, "") );
				// if(line.startsWith(temp = "nmap "               )) nmap                = line.contains("TRUE") || line.contains("true");
				
				// Local Java Server
				if(line.startsWith(temp = "ljs_enable "  )) ljs_enable   = line.contains("TRUE") || line.contains("true");
			}
			scanner.close();
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static boolean isVisualStudio() {
		File[] files = new File("").listFiles();
		if(files == null) return true;
		for(File file: files) {
			if( file.getName().equals(".vs") )     return true;
			if( file.getName().equals(".vscode") ) return true;
		}

		return false;
	}
	
	/**
	 * Write to the config file to fix formating and such
	 */
	public static void writeConfigFile() {
		try {
			FileWriter mw = new FileWriter( configFile );
			mw.write("# General\n");
			mw.write("SWfontSize "+swFontSize+"\n");
			mw.write("localWindowClient "+localWindowClient+"\n");
			mw.write("\n");
			mw.write("# Database\n");
			mw.write("sql_enable "+sql_enable+"\n");
			mw.write("sql_lazy "+sql_lazy+"\n");
			mw.write("sql_getAddresses "+sql_getAddresses+"\n");
			mw.write("sql_updateAddresses "+sql_updateAddresses+"\n");
			mw.write("sql_key "+sql_key+"\n");

			mw.write("\n");
			mw.write("# Ping Information\n");
			// mw.write("nmap "+nmap+"\n");
			mw.write("pingTimeOutTime "+pingTimeOutTime+"\n");
			mw.write("\n");
			mw.write("# Local Java Server\n");
			mw.write("ljs_enable "+ljs_enable+"\n");
			
			mw.close();
		} catch (IOException e) {
			UpDawgLauncher.log("An error occurred.");
			e.printStackTrace();
		}
	}
}
