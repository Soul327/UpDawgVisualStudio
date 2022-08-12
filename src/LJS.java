import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import Misc.SDL;

/**
 * LJS - Local Java Server
 * A local server stared to collect information from sensors, IE. piData's information
 * 
 * @author Walter Ozmore
 */
public class LJS extends Thread {
	static ArrayList<Client> clients;
	
	public LJS() {
		this.setName("LJS");
	}
	
	/**
	 * Runs the server that waits for clients only, checking for data will be handled by the main thread to save performance
	 */
	public void run() {
		/* Start thread for checking if clients have sent data */
		(new Thread() {
			public void run() {
				while(true) {
					SDL.sleep(1000);
					checkClients();
				}
			}
		}).start();
		
		/* Create server to accept clients */
		try {
			ServerSocket ss = new ServerSocket(Config.ljs_port);
			clients = new ArrayList<Client>(); //Stores all clients
			UpDawgLauncher.log("Local Java Server started");
			UpDawgLauncher.log("Waiting for connections");
			
			while (!ss.isClosed()) {
				Socket sock = ss.accept();
				clients.add( new Client(sock) );
			}
			
			UpDawgLauncher.log("Local Java Server shuting down\n");
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void checkClients() {
		for(int z=0;z<clients.size();z++) {
			Client client = clients.get(z);
			client.getData();
			if( client.attemptToDrop() ) z--;
		}
	}
}

class Client {
	String name = "";
	Socket socket;
	InputStream in;
	OutputStream out;
	long lastResponseTime;
	
	public Client(Socket sock) {
		socket = sock;
		
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch(Exception e) {}

		name = socket.getInetAddress().getCanonicalHostName();
		name = name.substring(0,name.indexOf("."));

		lastResponseTime = System.currentTimeMillis();
	}
	
	
	/** Returns the address that matches the name of the connection to LJS if there is one */
	public Address getAddress() {
		for(int z=0;z<UpDawgLauncher.addresses.size();z++)
			if(UpDawgLauncher.addresses.get(z).pingingAddress.equalsIgnoreCase(name)) {
				return UpDawgLauncher.addresses.get(z);
			}
		return null;
	}
	
	public boolean attemptToDrop() {
		if(System.currentTimeMillis() - lastResponseTime <= 60_000) return false;
		if(!LJS.clients.contains(this)) return false;

		LJS.clients.remove(this);
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		UpDawgLauncher.log("Client " + name + "'s connection has timed out.\n");
		return true;
	}
	
	public void getData() {
		try {
			// Grab matching address
			Address address = getAddress();

			if(in.available() <= 0) return;

			// Grab the last update time
			lastResponseTime = System.currentTimeMillis();
			
			int head = in.read();
			// 255 TEMP HUMI
			switch(head) {
				case 255:
					int temperature = in.read();
					int humidity = in.read();
					UpDawgLauncher.log( String.format( "Temp: %d Humidity: %d", temperature, humidity ) );
					
					if(address != null) {
						address.lastTemp = temperature;
						address.lastHumidity = humidity;
					}
					break;
			}
		} catch(Exception e) { e.printStackTrace(); }
	}
}
