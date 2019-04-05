
// A Java program for a client 
import java.net.*;
import java.io.*;

public class client {
	// initialize socket and input output streams
	private Socket socket = null;
	private DataInputStream input = null;
	private DataOutputStream out = null;
	private DataInputStream dInput = null;

	private String address;
	private int port;

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

	private void runScheduler() {
		String buffer = sendReceive("REDY");
		String largest = largestServer();// find largest server

		while (!buffer.equals("NONE")) {// server sends NONE when out of jobs

			String[] jobN = buffer.split(" ");// split job into parts
			// String jobInfo = jobN[4] + "|" + jobN[5] + "|" + jobN[6];// save relevant info (potentially useful for next task)

			sendReceive("SCHD " + jobN[2] + " " + largest + " 0");// assign job to largest server
			buffer = sendReceive("REDY");// ready for next job
		}
	}

	private String largestServer() {
		int largestParameter = 0;// parameter to check each server against
		String largest = "";
		String RCVD = "";

		sendReceive("RESC All");//request 
		sendReceive("OK");
		
		RCVD = sendReceive("OK");// receive "DATA"
		RCVD = sendReceive("OK");// receive first string
		while (!RCVD.contains(".")) {

			// System.out.println(RCVD);
			if (RCVD.contains(".")) {
				break;
			}
			String[] server = RCVD.split(" ");// split response into parts
			int cpuSize = Integer.parseInt(server[4]);// store CPU
			if (cpuSize > largestParameter) {
				largestParameter = cpuSize; // if cpu is larger than prev largest, update prev largest
				largest = server[0];
			}
			RCVD = sendReceive("OK");

		}
		return largest;
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
		client.runScheduler();// receive and allocate jobs
		client.disconnect();// disconnect
	}
}