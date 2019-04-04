
// A Java program for a Client 
import java.net.*;
import java.util.HashMap;
import java.io.*;

public class client {
	// initialize socket and input output streams
	private Socket socket = null;
	private DataInputStream input = null;
	private DataOutputStream out = null;
	private DataInputStream dInput = null;

	// constructor to put ip address and port
	public client(String address, int port) {
		System.out.println("# ds-sim COMP335@MQ, s1, 2019");
		System.out.println("# Client Server started!");
		System.out.println("# Connection Initiated...");

		// establish a connection
		try {
			socket = new Socket(address, port);
			System.out.println("# ...Connected");

			// takes input from terminal

			input = new DataInputStream(System.in);

			dInput = new DataInputStream(socket.getInputStream());

			// sends output to the socket
			out = new DataOutputStream(socket.getOutputStream());

		} catch (UnknownHostException u) {
			System.out.println(u);
		} catch (IOException i) {
			System.out.println(i);
		}

		connect();
		
		// string to read message from input
		String str1 = "";
		String str2 = "";

		// keep reading until "Over" is input
		while (!str1.equals("Over")) {
			try {
				str1 = input.readLine() + "\n";
				out.write(str1.getBytes());
				str2 = dInput.readLine();

				System.out.println("Server: " + str2);
				/*
				 * if(input.readLine().equals("REDY")) { System.out.println(str2); }
				 */
			} catch (IOException i) {
				System.out.println(i);
			}
		}

		// close the connection
		try {
			input.close();
			out.close();
			socket.close();
		} catch (IOException i) {
			System.out.println(i);
		}
	}

	public static void main(String args[]) {
		client client = new client("127.0.0.1", 8096);
	}

	// this section is for scheduling

	public class jobMap {
		public jobMap(Integer jobID, Integer runtime, Integer jCores, Integer jMemory, Integer jDisk) {

		}
	}

	jobMap test = new jobMap(1, 2, 3, 4, 5);
	serverMap test2 = new serverMap("Large", 2, 3, 4);

	public void connect() {
		try {
			String HELO = "HELO\n";
			String AUTH = "AUTH Lewis\n";

			System.out.print("SENT: " + HELO);

			out.write(HELO.getBytes());

			System.out.println("RCVD: " + dInput.readLine());
			System.out.print("SENT: " + AUTH);

			out.write(AUTH.getBytes());

			System.out.println("RDVD: " + dInput.readLine());
		} catch (IOException i) {
			System.out.println(i);
		}

	}

	public class serverMap {
		public serverMap(String a, Integer cores, Integer memory, Integer disk) {

		}
	}

}