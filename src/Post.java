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
	
	public static void updateAddress(Address address) {
		final String link = "https://www.everyoneandeverything.org/UpDawg/res/Java/UpdateAddress";
		Map<String, String> args = new HashMap<String, String>();
		args.put("key", Config.sql_key);
		args.put("IPID", ""+address.uid );
		args.put("pingingAddress", address.pingingAddress );
		args.put("address", address.address );
		args.put("hostname", address.hostName );
		args.put("nickname", address.nickname );
		args.put("stat", ""+address.status );
		post(link, args);
	}
	
	/**
	 * Queries
	 */
	public static void getSQLInfo() {
		final String link = "https://www.everyoneandeverything.org/UpDawg/res/Java/GetSQLInfo";
		Map<String, String> args = new HashMap<String, String>();
		args.put("key", Config.sql_key);
		String re = post(link, args).get(0);
		re = re.replace("<br>", "");
		String[] list = re.split("<com>");
		
		Config.sql_username = list[0].split("<eql>")[1];
		Config.sql_password = list[1].split("<eql>")[1];
		Config.sql_address = list[2].split("<eql>")[1];
		Config.sql_database = list[3].split("<eql>")[1];
	}
	
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
//				System.out.println( line );// Prints out website
				reList.add( line );
			}
			in.close();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reList;
	}
}
