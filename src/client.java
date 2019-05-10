
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
	private HashMap<String, Integer> serverMap = new HashMap<String, Integer>();
	private String largestServer = "";

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

			// input = new DataInputStream(System.in);// not currently used, allows messages
			// from console

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
			// input.close();
			out.close();
			socket.close();
		} catch (IOException i) {
			System.out.println(i);
		}
	}

	private void populateServerList() {// Request all servers and order from largest to smallest
		sendReceive("REDY");// pretend ready so we can receive info
		String RCVD = "";
		sendReceive("RESC All");
		RCVD = sendReceive("OK");
		int prevLargestCPU = -1;

		while (!RCVD.contains(".")) {
			String[] server = RCVD.split(" ");

			int cpuSize = Integer.parseInt(server[4]);
			String serverName = server[0];
			RCVD = sendReceive("OK");
			serverMap.put(serverName, cpuSize);// list of all servers by CPUsize
			if (cpuSize > prevLargestCPU) {
				prevLargestCPU = cpuSize;
				largestServer = server[0] + " " + server[1];
			}
		}
	}

	private void runScheduler(String alg) { //
		if (alg.compareTo("ff") != 0 && alg.compareTo("bf") != 0 && alg.compareTo("wf") != 0) {
			System.out.println("No algorithm argument");
			return;
		}

		String buffer = sendReceive("REDY");
		String serverChoice = null;
		while (!buffer.equals("NONE")) {// server sends NONE when out of jobs
			String[] jobN = buffer.split(" ");// split job into parts
			int cpuREQ = Integer.parseInt(jobN[4]);
			int memREQ = Integer.parseInt(jobN[5]);
			int diskREQ = Integer.parseInt(jobN[6]);

			switch (alg) {// check argument case
			case "ff":
				serverChoice = firstFit(cpuREQ);
				break;
			case "wf":
				serverChoice = worstFit(cpuREQ, memREQ, diskREQ);
				break;
			case "bf":
				serverChoice = bestFit(cpuREQ);
				break;
			default:
				serverChoice = largestServer;
				break;
			}
			sendReceive("SCHD " + jobN[2] + " " + serverChoice);// assign job to server based on alg
			buffer = sendReceive("REDY");// ready for next job
		}
	}

	private String sendReceive(String s) {// sends a message and prints it with the response from the server
		try {
			String send = s + "\n";
			out.write(send.getBytes());
			String RCVD = dInput.readLine();
			System.out.print("SENT: " + s + "\n");
			System.out.println("RCVD: " + RCVD);// extra newline included
			// intentionally for legibility
			return RCVD;
		} catch (IOException i) {
			System.out.println(i);
			return "";
		}
	}

	private static String checkAlgorithm(String str[]) {
		if (str.length > 1) {
			for (int i = 0; i < str.length; i++) {
				if (str[i].compareTo("-a") == 0 && str[i + 1] != null) {
					return str[i + 1];
				}
			}
		}
		return "";
	}

	private String firstFit(int jobRequirement) {
		String serverID = "";
		String RCVD = "";
		boolean changed = false;
		sendReceive("RESC All");// request
		RCVD = sendReceive("OK");
		int smallest = 99999;
		int smallestICPU = 99999;
		while (!RCVD.contains(".")) {

			String[] server = RCVD.split(" ");// split response into parts
			int cpuSize = Integer.parseInt(server[4]);// store CPU
			int serverState = Integer.parseInt(server[2]);

			if (cpuSize >= jobRequirement && smallest > cpuSize && serverState < 4) {
				if (cpuSize < serverMap.get(server[0]) && changed == true && smallest <= serverMap.get(server[0])) {

				} else {
					serverID = server[0] + " " + server[1];
					changed = true;
					smallest = serverMap.get(server[0]);
				}
			}
			RCVD = sendReceive("OK");
		}
		if (serverID == "") {
			int smallestInitial = 9999999;
			String smallestServer = "";
			for (HashMap.Entry<String, Integer> entry : serverMap.entrySet()) {
				String key = entry.getKey();
				Integer value = entry.getValue();

				if (jobRequirement < value && value < smallestInitial) {
					smallestInitial = value;
					smallestServer = key + " 0";
				}

				if (jobRequirement == value) {
					smallestServer = key + " 0";
					break;
				}
			}
			serverID = smallestServer;
		}
		return serverID;
	}

	private String worstFit(int cpuREQ, int memREQ, int diskREQ) {
		int worstFit = -1;
		int altFit = -1;
		String worstFitType = "";
		String altFitType = "";
		String worstFitID = "";
		String altFitID = "";

		sendReceive("RESC All");
		String RCVD = sendReceive("OK");

		while (!RCVD.contains(".")) {
			String[] server = RCVD.split(" ");// split response into parts
			int serverState = Integer.parseInt(server[2]); // server availability
			int cpuSize = Integer.parseInt(server[4]); // CPU Size

			int fitness = cpuSize - cpuREQ;

			if (serverState != 1 && serverState != 4) {
				if (fitness > worstFit && (serverState == 2 || serverState == 3)) {
					worstFit = fitness;
					worstFitID = server[1];
					worstFitType = server[0];
				} else if (fitness > altFit) {
					altFit = fitness;
					altFitID = server[1];
					altFitType = server[0];
				}
			}

			RCVD = sendReceive("OK");
		}

		if (worstFit > -1) {
			return worstFitType + " " + worstFitID;
		} else if (altFit > -1) {
			return altFitType + " " + altFitID;
		}
		return largestServer;
	}

	private String bestFit(int jobReq) {
		int bestFit = 10000;
		int minAva = 10000;
		String serverID = "";
		String bestID = "";
		String bestType = "";

		sendReceive("RESC All");
		String RCVD = sendReceive("OK");

		while (!RCVD.contains(".")) {

			String[] server = RCVD.split(" ");// split response into parts
			int cpuSize = Integer.parseInt(server[4]); // store CPU
			int serverState = Integer.parseInt(server[2]); // store server's state
			int serverAvaiTime = Integer.parseInt(server[3]); // store server's available time

			System.out.println("CPU size " + cpuSize + " Job Requirement: " + jobReq + " Server State: " + serverState);

			int fitnessValue = cpuSize - jobReq;

			if (cpuSize >= jobReq && serverState < 4) {

				if ((fitnessValue < bestFit) || (fitnessValue == bestFit && minAva > serverAvaiTime)) {
					bestFit = fitnessValue;
					minAva = serverAvaiTime;
					bestID = server[0];
					bestType = server[1];
				} else {
					serverID = server[0] + " " + server[1];
				}
			}
			RCVD = sendReceive("OK");
		}

		if (bestFit != 10000) { // checks if bestFit is available
			return bestID + " " + bestType;
		} else {
			return serverID;
		}

	}

	public static void main(String args[]) {
		String argument = checkAlgorithm(args);// look through args for algorithm selector
		for(int i=0;i<args.length;i++)
			System.out.println(args[i]);
		client client = new client("127.0.0.1", 8096);
		client.connect();// send connection strings to server
		client.runScheduler(argument);// run scheduler WITH arguments considered
		client.disconnect();// disconnect
	}
}