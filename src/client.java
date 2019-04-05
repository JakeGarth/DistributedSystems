
// A Java program for a client 
import java.net.*;
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
		System.out.println("# client Server started!");
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

	public void connect() {


			sendReceive("HELO");
			sendReceive("AUTH Lewis");
			scheduler();
		    
		    
			//String largest = checkServer();
			//scheduler(largest);
		
	}
	
	private void scheduler() {
		String buffer = sendReceive("REDY");
		
		String largest = largestServer();
		
		while (!buffer.equals("NONE")) {
			
			String[] jobN = buffer.split(" ");//split job into parts
			String jobInfo = jobN[4] + "|" + jobN[5] + "|" + jobN[6];// save relevant info
			sendReceive("SCHD " + jobN[2] + " " + largest + " 0");

			buffer = sendReceive("REDY");
		}
		sendReceive("QUIT");
	}
	
	
	
	private String largestServer(){
		int largestParameter = 0;
		String largest = "";
		String RCVD = "";
		try {
			out.write("RESC All\n".getBytes());
			System.out.println("SENT: RESC All\nRCVD: \n");
			
			sendReceive("OK");
			RCVD=dInput.readLine();
			while(!RCVD.contains(".")) {
				out.write("OK\n".getBytes());
				//System.out.println(RCVD);

				String[] server = RCVD.split(" ");
				int diskSize = Integer.parseInt(server[5]);
				if (diskSize>largestParameter) {
					largestParameter = diskSize;
					largest=server[0];
				}
				RCVD=dInput.readLine();		
			}

			return largest;

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*while(!RCVD.contains("ERR")) {
			sendReceive(" ");
			//System.out.println(RCVD);
		
		}*/
		return largest;
	}

	private String sendReceive(String s) {
		try {
			String send = s + "\n";
			out.write(send.getBytes());
			String RCVD = dInput.readLine();	
			
			
			System.out.print("SENT: " + s + "\n");
			System.out.println("RCVD: " + RCVD + "\n");
			

			return RCVD;
		} catch (IOException i) {
			System.out.println(i);
			return "";
		}
	}
	
}