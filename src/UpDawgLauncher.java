import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import Misc.SDL;
import Misc.SimpleWindow;

public class UpDawgLauncher {
	static boolean run = true;
	public static String version = "Version 2.3.4";
	public static SimpleWindow window;
	
	public static ArrayList<Address> addresses = new ArrayList<Address>();
	public static ArrayList<Address> addressesToUpdate = new ArrayList<Address>();
	
	public static void main(String[] args) throws IOException, InterruptedException {
		if( !lockInstance("lock.dat") ) {
			System.out.println("ALREADY RUNNING");
			System.exit(0);
		}

		log("UpDawg " + version + "\n");
		
		// Grab config and update config file
		Config.init();
		Config.writeConfigFile();

		// Add shutdown hook
		shutdownHook();
		
		// Run local window client
		if(Config.localWindowClient)
			new WindowClient();
		WindowClient.tray();

		// Start local java server for extra data
		if(Config.ljs_enable) {
			LJS ljs = new LJS();
			ljs.start(); // Waits for connections
		}

		moveUpdater();
		while(run) tick();
	}

	/**
	 * Sends a post update request periodically, this is put on a seprate thread
	 * 
	 * @author Walter Ozmore
	 */
	static void run_update() {
		(new Thread() {
			public void run() {
				while(true) {
					ArrayList<Address> updateList = addressesToUpdate;
					addressesToUpdate = new ArrayList<Address>();
					Post.update( updateList );
					SDL.sleep(2_000);
				}
			}
		}).start();
	}

	/**
	 * Uses a post request get addresses from the server, then pings all the addresses from the server sequentially
	 * 
	 * @author Walter Ozmore 
	 */
	static void tick() {
		// Grab addresses
		Post.getAddresses();

		// Ping every address
		for(int x=0;x<addresses.size();x++) {
			Address address = addresses.get(x);
			address.ping();
			if( !addressesToUpdate.contains(address))
				addressesToUpdate.add(address);
		}
		Post.update( addresses );
		Post.checkUpdates();
	}
	
	/**
	 * Logs the given message to the console and a file with the name of the date and the number of log
	 * 
	 * @param message
	 * 
	 * @author Walter Ozmore
	 */
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
				Config.currentLogFile = new File("local\\logs\\" + fileName);
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

	/**
	 * Creates a shutdown hook that will run when the program closes though either the user closing the program or when a fatal error is reached
	 * 
	 * @author Walter Ozmore
	 */
	public static void shutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				UpDawgLauncher.log("Shutting down");
			}
		}, "Shutdown-thread"));
	}

	static boolean lockInstance(final String lockFile) {
    try {
			final File file = new File(lockFile);
			final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();

			if (fileLock != null) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						try {
							fileLock.release();
							randomAccessFile.close();
							file.delete();
						} catch (Exception e) {
							log("Unable to remove lock file: " + lockFile+"\n"+e.getMessage());
						}
					}
				});
				return true;
			}
    } catch (Exception e) {
        log( e.getMessage() );
    }
    return false;
	}

	static void moveUpdater() {
		File newUpdate = new File("update.jar");
		if( !newUpdate.exists() ) return;

		File oldUpdate = new File("local\\update.jar");
		if( oldUpdate.exists() ) FileUtil.delete( oldUpdate );

		newUpdate.renameTo( oldUpdate );
		newUpdate.delete();
	}
}