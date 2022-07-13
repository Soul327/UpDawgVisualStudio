/**
 * A simple class just to store information obtained from nmap scans
 */
public class Port {
	public int number;
	public String conType = "", service = "", state = "";
	
	public Port(String str) {
		String list[] = str.split("[\\/]| +");
		number = Integer.parseInt( list[0] );
		conType = list[1];
		state = list[2];
		service = list[3];
	}
}
