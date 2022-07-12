import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import Misc.SDL;
import Misc.SimpleWindow;

public class UpDawgLauncher {
	static boolean run = true;
	public static String version = "Version 2.2.7";
	public static SimpleWindow window;
	
	public static ArrayList<Address> addresses = new ArrayList<Address>();
	public static ArrayList<Address> addressesToUpdate = new ArrayList<Address>();
	
	public static void main(String[] args) throws IOException, InterruptedException {
		log("UpDawg " + version + "\n");
		
		// Grab config and update config file
		Config.init();
		Config.writeConfigFile();

		// Add shutdown hook
		shutdownHook();
		
		// Run local window client
		if(Config.localWindowClient) new WindowClient();

		// Start local java server for extra data
		if(Config.ljs_enable) {
			LJS ljs = new LJS();
			ljs.start(); // Waits for connections
		}

		// Grab addresses
		Post.getAddresses();

		// run();
		new Thread(() -> { run_update(); }).start();
		while(run) {
			tick();
		}
	}

	static void run_update() {
		while(true) {
			ArrayList<Address> updateList = addressesToUpdate;
			addressesToUpdate = new ArrayList<Address>();
			Post.update( updateList );
			SDL.sleep(2_000);
		}
	}

	static void tick() {
		// Ping every address
		for(int x=0;x<UpDawgLauncher.addresses.size();x++) {
			Address address = UpDawgLauncher.addresses.get(x);
			address.ping();
			if( !addressesToUpdate.contains(address))
				addressesToUpdate.add(address);
			SDL.sleep(500);
		}
	}
	
	public static void log(String message) {
		if(!message.endsWith("\n")) message += "\n";

		// Get date time string to put in front of messages
		String dt = Config.dtf.format( LocalDateTime.now() );
		String log = String.format("%s: %s", dt, message);
		System.out.print( log );
		
		/* Write to file */
		// Check if the log file exists
		if(Config.currentLogFile == null || !Config.currentLogFile.exists()) {
			int num = 0;
			do {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-YYYY");
				String fileName = dtf.format( LocalDateTime.now() ) + " " + num + ".log";
				Config.currentLogFile = new File("logs\\" + fileName);
				num++;
			} while( Config.currentLogFile.exists() );
			try {
				if( !Config.currentLogFile.getParentFile().exists() )
					Config.currentLogFile.getParentFile().mkdirs();
				Config.currentLogFile.createNewFile();
			} catch (IOException e) { e.printStackTrace(); return; }
		}
		// Write to log file
		try {
			Files.write(Config.currentLogFile.toPath(), log.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void shutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				UpDawgLauncher.log("Shutting down");
			}
		}, "Shutdown-thread"));
	}

	public static String printDate(long time) {
		// Print out date
		long millis = time * 1000;
		Date date = new Date(millis);
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("CDT"));
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
}