
// A Java program for a client 
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.io.*;
//

public class client {
	// initialize socket and input output streams
	private Socket socket = null;
	private DataInputStream input = null;
	private DataOutputStream out = null;
	private DataInputStream dInput = null;
	private String address;
	private int port;
	private int sortingArr[] = null;
	private HashMap<String, Integer> serverMap = new HashMap<String, Integer>();

	// constructor for IP and Port
	public client(String IP, int portNum) {
		this.address = IP;
		this.port = portNum;
	}

	private void connect() {// connect and send intro strings
		try {
			System.out.println("# Client Started, attempting connection...");
			socket = new Socket(address, port);
			dInput = new DataInputStream(socket.getInputStream());// input stream from socket (server)
			out = new DataOutputStream(socket.getOutputStream()); // output to server (socket)

			input = new DataInputStream(System.in);// not currently used, allows messages from console

			System.out.println("# ...Connected");// message confirmation of connection
		} catch (UnknownHostException u) {
			System.out.println(u);
		} catch (IOException i) {
			System.out.println(i);
		}

		String user = System.getProperty("user.name");// get user profile for auth
		sendReceive("HELO");
		sendReceive("AUTH " + user);
		sendReceive("REDY");
		populateServerList();
		
	}

	private void disconnect() {
		sendReceive("QUIT");// send quit msg
		try {
			dInput.close();// close any remaining streams
			input.close();
			out.close();
			socket.close();
		} catch (IOException i) {
			System.out.println(i);
		}
	}
	
	private void populateServerList() {//Request all servers and order from largest to smallest
		String buffer = sendReceive("REDY");//pretend ready so we can receive info
		String RCVD = "";
		sendReceive("RESC All");
		RCVD = sendReceive("OK");
		
		while (!RCVD.contains(".")) {
			String[] server = RCVD.split(" ");
			
			int cpuSize = Integer.parseInt(server[4]);
			String serverName = server[0];
			System.out.println(serverName + "--");
			RCVD = sendReceive("OK");
			serverMap.put(serverName, cpuSize);//list of all servers by CPUsize
		} 
	}

	private void runScheduler() { //
		String buffer = sendReceive("REDY");

		while (!buffer.equals("NONE")) {// server sends NONE when out of jobs

			String[] jobN = buffer.split(" ");// split job into parts
			// String jobInfo = jobN[4] + "|" + jobN[5] + "|" + jobN[6];// save relevant info (potentially useful for next task)
			//System.out.println("runScheduler cpu cores: "+jobN[4]);
			
		//	if(Integer.parseInt(jobN[2])<250) {
			String firstFit = firstFit(Integer.parseInt(jobN[4]));// find firstfit server
			
			sendReceive("SCHD " + jobN[2] + " " + firstFit);// assign job to first fit server
			buffer = sendReceive("REDY");// ready for next job
			
		//	}
		}
	}

	
	private String firstFit(int jobRequirement) {
		String serverID = "";
		String RCVD = "";
		boolean changed = false;
		sendReceive("RESC All");//request
		RCVD = sendReceive("OK");
		int smallest = 99999;
		
		while (!RCVD.contains(".")) {
			
			String[] server = RCVD.split(" ");// split response into parts
			int cpuSize = Integer.parseInt(server[4]);// store CPU
			int serverState = Integer.parseInt(server[2]);
	/*		
			System.out.println("CPU size "+cpuSize+" Job Requirement: "+jobRequirement+" Server State: "+serverState);
			System.out.println("CPU size "+cpuSize+" Job Requirement: "+jobRequirement +" smallest: "+ smallest +" Server State: "+serverState);
			System.out.println("changed: "+changed);
	*/		if (cpuSize >= jobRequirement && smallest>cpuSize && serverState<4) {
				if(cpuSize<serverMap.get(server[0]) && changed == true) {
					
				}else {
				serverID = server[0]+" "+server[1];
				changed = true;
	/*		System.out.println("smallest: "+smallest);
				System.out.println("server: "+serverID);
				System.out.println("CPU Size: "+cpuSize);
				System.out.println("CPU's Required: "+jobRequirement); 
		*/		smallest = serverMap.get(server[0]);
				
		//		System.out.println("smallest: "+smallest);
				}
			}
			
			
			
			RCVD = sendReceive("OK");
			
		}
	if(serverID == "") {
			int smallestInitial = 9999999;
			String smallestServer = "";
			for (HashMap.Entry<String, Integer> entry : serverMap.entrySet()) {
			    String key = entry.getKey();
			    Integer value = entry.getValue();
			   
			    if(jobRequirement<value && value<smallestInitial) {
			    	smallestInitial = value;
			    	smallestServer = key+ " 0";
			    }
			    
			    if(jobRequirement == value) {
			    	smallestServer = key+" 0";
			    	break;
			    }
			}
			serverID = smallestServer;
		} 
		return serverID;
	}
	
	private String sendReceive(String s) {// sends a message and prints it with the response from the server
		try {
			String send = s + "\n";
			out.write(send.getBytes());

			String RCVD = dInput.readLine();

			System.out.print("SENT: " + s + "\n");
			System.out.println("RCVD: " + RCVD + "\n");// extra newline included intentionally for legibility

			return RCVD;
		} catch (IOException i) {
			System.out.println(i);
			return "";
		}
	}
	
	public static void main(String args[]) {

		client client = new client("127.0.0.1", 8096);
		client.connect();// send connection strings to server
		//client.runScheduler();// receive and allocate jobs
		client.disconnect();// disconnect
	}
}