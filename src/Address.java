import java.util.ArrayList;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

public class Address {
	public boolean updateSQL = false;
	public boolean pingWorking = false;
	public boolean hidden = false;
	
	public int status = -1;
	public int uid = -1;

	public long time = 0;

	public String address = "";
	public String hostName = "";
	public String nickname = "";
	public String pingingAddress = "";
	
	public ArrayList<Port> ports = new ArrayList<Port>();
	
	//Temp Info
	public double lastTemp = -1, lastHumidity = -1;
	
	public Address() {}

	public Address(int uid) {
		pingingAddress = address;
		this.uid = uid;
	}
	
	public Address(String address, String uid) {
		nickname = hostName = address;
		this.address = address;
		pingingAddress = address;
		this.uid = Integer.parseInt( uid );
	}
	

	/**
	 * Adds a port to the address using the String 
	 * @param str
	 */
	public void addPort(String str) {
		Port port = new Port(str);
		for(int z=0;z<ports.size();z++)
			if(port.number == ports.get(z).number) {
				ports.set(z, port);
				return;
			}
		ports.add(port);
		updateSQL = true;
	}
	
	/**
	 * Changes the address's status to warning if the address's status is already warning it will set it to down
	 */
	public void setDown() {
		// Log the change if it changes
		if(status != 0 && status != 1) UpDawgLauncher.log(nickname+((status == 1)?" missed a ping\n":" is down\n"));
		// Set status to warning, if it already at warning set it to down
		status = (status > 0)?status - 1:0;
		setTime();
		UpDawgLauncher.addressesToUpdate.add( this );
	}

	/**
	 * Changes the address's status to up
	 */
	public void setUp() {
		// Log the change if it changes
		if(status != 2) UpDawgLauncher.log(nickname+" is up.\n");
		// Set status to up
		status = 2;
		setTime();
		UpDawgLauncher.addressesToUpdate.add( this );
	}

	/**
	 * Sets the addresses's time to the current time
	 */
	public void setTime() {
		Date date = new Date();
		time = date.getTime() / 100;
	}

	/**
	 * Pings the given IP address via java's InetAddress.isReachable and then calls either setUp() or setDown()
	 */
	public void ping() {
		InetAddress inet;
		try {
			inet = InetAddress.getByName( pingingAddress );
			if(inet.isReachable(1000)) {
				setUp();
				return;
			}
		} catch (UnknownHostException e) {
			UpDawgLauncher.log("No host found for "+pingingAddress);
		} catch (IOException e) {
			UpDawgLauncher.log( e.getMessage() );
		}
		setDown();
	}
}