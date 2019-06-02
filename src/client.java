
// A Java program for a client 
import java.net.*;
import java.util.Map;
import java.util.HashMap;
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
	private HashMap<String, Integer[]> serverMap = new HashMap<String, Integer[]>();
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
			System.out.println("...Program complete, Connection closed.");
			for (Map.Entry<String, Integer[]> entry : serverMap.entrySet()) {
				System.out.print(entry.getKey());
			}

		} catch (

		IOException i) {
			System.out.println(i);
		}
	}

	private void populateServerList() {// Request all servers and order from largest to smallest
		String RCVD = "";
		sendReceive("RESC All");
		RCVD = sendReceive("OK");
		int largestCpu = 0;
		while (!RCVD.equals(".")) {
			String[] server = RCVD.split(" ");
			String serverName = server[0];
			Integer[] serverDetails = new Integer[3];
			serverDetails[0] = Integer.parseInt(server[4]);
			serverDetails[1] = Integer.parseInt(server[5]);
			serverDetails[2] = Integer.parseInt(server[6]);
			if (!serverMap.containsKey(serverName)) {
				serverMap.put(serverName, serverDetails);
			}

			RCVD = sendReceive("OK");
		}
		for (Map.Entry<String, Integer[]> entry : serverMap.entrySet()) {
			if (entry.getValue()[0] > largestCpu) {
				largestServer = entry.getKey();
				largestCpu = entry.getValue()[0];
			}
		}
	}

	private void runScheduler(String alg) {
		String buffer = sendReceive("REDY");
		String serverChoice = null;
		while (!buffer.equals("NONE")) {// server sends NONE when out of jobs
			String[] jobN = buffer.split(" ");// split job into parts
			int cpuREQ = Integer.parseInt(jobN[4]);
			int memREQ = Integer.parseInt(jobN[5]);
			int diskREQ = Integer.parseInt(jobN[6]);

			switch (alg) {// check argument case
			case "wf":
				serverChoice = worstFit(cpuREQ, memREQ, diskREQ);
				break;
			case "mf":
				serverChoice = modFit(cpuREQ, memREQ, diskREQ);
			default:
				serverChoice = modFit(cpuREQ, memREQ, diskREQ);// largestServer
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

	private String modFit(int cpuREQ, int memREQ, int diskREQ) {
		String bestConfType = "";
		double maxConf = 0;

		for (Map.Entry<String, Integer[]> entry : serverMap.entrySet()) {// check server type list
			double typeCPU = entry.getValue()[0];
			double typeMEM = entry.getValue()[1];
			double typeDISK = entry.getValue()[2];

			if (typeCPU >= cpuREQ && typeMEM >= memREQ && typeDISK >= diskREQ) {// if server type can run job
				sendReceive("RESC Type " + entry.getKey());// get list of server type
				String buffer = sendReceive("OK");

				while (!buffer.contains(".")) {// check each individual server
					String[] thisServer = buffer.split(" ");// save parts
					String serverName = thisServer[0] + " " + thisServer[1];
					int serverState = Integer.parseInt(thisServer[2]);
					double confidence = calculateConfidence(thisServer, cpuREQ, memREQ, diskREQ);

					if (confidence > maxConf && (serverState < 4)) {
						maxConf = confidence;
						bestConfType = serverName;
						System.out.println("Server CONF IS " + confidence + " AT " + bestConfType);
					}
					buffer = sendReceive("OK");
				}
			}
		}
		if (maxConf > 0) {
			return bestConfType;
		} else
			return largestServer;
	}

	private double calculateConfidence(String thisServer[], int jobCPU, int jobMEM, int jobDISK) {
		double serverCPU = Integer.parseInt(thisServer[4]);
		double serverMEM = Integer.parseInt(thisServer[5]);
		double serverDISK = Integer.parseInt(thisServer[6]);
		if (serverCPU > 0 && serverMEM > 0 && serverDISK > 0) {// calc individual confidence values
			double cpuConf = (double) jobCPU / serverCPU;
			double memConf = (double) jobMEM / serverMEM;
			double diskConf = (double) jobDISK / serverDISK;
			return (cpuConf * diskConf * memConf);// calculate combined confidence
		} else// give extra weight to cpu
			return 0;
	}

	public static void main(String args[]) {
		String argument = checkAlgorithm(args);// look through args for algorithm selector
		client client = new client("127.0.0.1", 8096);
		client.connect();// send connection strings to server
		client.runScheduler(argument);// run scheduler WITH arguments considered
		client.disconnect();// disconnect
	}

	// -------------------------------------------------------------------

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
}
