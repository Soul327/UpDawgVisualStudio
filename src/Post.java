import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Post {
	// Arguments for spliting and creating post strings
	final static String equals = "<eql>";
	final static String separator = "<com>";
	final static String lineSeparator = "<br>";

	/**
	 * Sends a post request to the server to update the given address by creating an array of the address and passing it to update(ArrayList<Address> addresses)
	 * 
	 * @param address
	 * 
	 * @author Walter Ozmore
	 */
	public static void update(Address address) {
		ArrayList<Address> addresses = new ArrayList<>();
		addresses.add(address);
		update( addresses );
	}

	/**
	 * Sends a post request to the server to update the given addresses
	 * 
	 * @param addresses ArrayList of addresses
	 * 
	 * @author Walter Ozmore
	 */
	public static void update(ArrayList<Address> addresses) {
		// Skip if there are no addresses
		if(addresses == null || addresses.size() <= 0) return;

		UpDawgLauncher.log("Updating " + addresses.size() + " addresses");

		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		// List of addresses
		for(int x = 0;x < addresses.size(); x++) {
			Address address = addresses.get(x);
			
			// if(address.time > 0) continue;

			Map<String, String> map = new HashMap<String, String>();
			map.put("uID", address.uid + "");
			map.put("Stat", address.status + "");
			map.put("UpdateTime", address.time + "");
			list.add(map);
		}

		// Create sendString
		String str = getSendStr(list);
		if(str == null) return;

		String link = "https://www.everyoneandeverything.org/UpDawg/res/Java/UpdateAddresses.php";
		Map<String, String> args = new HashMap<String, String>();
		args.put("version", UpDawgLauncher.version);
		args.put("q", str);
		ArrayList<String> re = post(link, args);
		// Print out output from post
		if(re != null)
			for(String r:re)
				System.out.println( r );
	}

	/**
	 * Sends a post request to the server that returns a long string that is then parced by readSendStr(String str). Then it checks each new address with each old addresses and combinds them allowing without deleting the old address, only updating their information.
	 * 
	 * @author Walter Ozmore
	 */
	public static void getAddresses() {
		UpDawgLauncher.log("Loading addresses from server\n");

		Map<String, String> args = new HashMap<String, String>();
		args.put("key", Config.sql_key);
		String link = "https://www.everyoneandeverything.org/UpDawg/res/Ajax/GetAddresses.php";
		ArrayList<String> postOutput = post(link, args);

		// When there is no post output skip this function
		if(postOutput == null || postOutput.size() == 0) return;
		
		ArrayList<Map<String, String>> output = readSendStr( postOutput.get(0) );

		ArrayList<Address> newAddresses = new ArrayList<Address>();

		for(int x=0;x<output.size();x++) {
			Map<String, String> map = output.get(x);
			Address address = new Address();
			if(map.containsKey("uID")) {
				try {
					address.uid = Integer.parseInt(map.get("uID"));
					
					// Check if the address already exists
					for(int y=0;y<UpDawgLauncher.addresses.size();y++) {
						if(UpDawgLauncher.addresses.get(y).uid == address.uid)
							address = UpDawgLauncher.addresses.get(y);
					}

				} catch(NumberFormatException e) {
					UpDawgLauncher.log("Number format exception Post.getAddresses\n"+map.get("uID")+"\n");
				}
			} else {
				// No uID found
				continue;
			}
			if(map.containsKey("NickName")) address.nickname = map.get("NickName");
			if(map.containsKey("PingingAddress")) address.pingingAddress = map.get("PingingAddress");


			newAddresses.add( address );
		}

		UpDawgLauncher.addresses = newAddresses;
	}
	
	/**
	 * Get the data from a URL by sending data via post and returning the results
	 * 
	 * @param args Auguments to be sent as post data
	 * @return arraylist<String> of the webpage
	 * 
	 * @author Walter Ozmore
	 */
	public static ArrayList<String> post(String link, Map<String, String> args) {
		ArrayList<String> reList = new ArrayList<String>();
		try {
			// open a connection to the site
			URL url = new URL(link);
			URLConnection con = url.openConnection();
			// activate the output
			con.setDoOutput(true);
			PrintStream ps = new PrintStream(con.getOutputStream());
			// send your parameters to your site
			String arg = "";
			for (Map.Entry<String, String> entry : args.entrySet())
				arg += entry.getKey() + "=" + entry.getValue() + "&";
			if(arg.endsWith("&")) arg = arg.substring(0, arg.length() - 1); // Remove the last & from the string
			if(arg.length() > 0) ps.print(arg); // Send parameters if there are any
			ps.close(); // close the print stream
			
			// Read the webpage and close input
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			String line;
			while((line = in.readLine()) != null) {
				reList.add( line );
			}
			in.close();
			
		} catch (MalformedURLException e) {
			UpDawgLauncher.log(e.getMessage());
			return null;
		} catch (IOException e) {
			UpDawgLauncher.log(e.getMessage());
			return null;
		}
		return reList;
	}

	/**
	 * Parses the String in to an ArrayList<Map<String, String>> using {lineSeparator, separator, equals} strings
	 * 
	 * @param str Inputed line
	 * @return ArrayList<Map<String, String>> of all variables
	 * 
	 * @author Walter Ozmore
	 */
	public static ArrayList<Map<String, String>> readSendStr(String str) {
		ArrayList<Map<String, String>> re = new ArrayList<Map<String, String>>();

		String[] lines = str.split( lineSeparator );
		for(int x=0;x<lines.length;x++) {
			Map<String, String> map = new HashMap<String, String>();
			String[] variables = lines[x].split( separator );
			for(int y=0;y<variables.length;y++) {
				String[] variable = variables[y].split( equals );
				map.put(variable[0], ((variable.length == 2)?variable[1]:null));
			}
			re.add( map );
		}

		return re;
	}

	/**
	 * Converts the arguments in to a string to send as a post request
	 * 
	 * @param args Arguments to map to send as a post request
	 * @return String to send as a post request
	 * 
	 * @author Walter Ozmore
	 */
	public static String getSendStr(ArrayList<Map<String, String>> args) {
		if(args.size() == 0) return null;

		String sendString = "";
		for(int x=0;x<args.size();x++) {
			for (Map.Entry<String,String> entry : args.get(x).entrySet())
				sendString += entry.getKey() + equals + entry.getValue() + separator;
			// Remove last separator
			sendString = sendString.substring(0, sendString.length() - separator.length());
			sendString += lineSeparator;
		}

		// Remove last line break
		sendString = sendString.substring(0, sendString.length() - lineSeparator.length());
		return sendString;
	}
}
