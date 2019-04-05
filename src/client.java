
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
		System.out.println("# Client Started, attempting connection...");


		// establish a connection
		try {
			socket = new Socket(address, port);
			input = new DataInputStream(System.in);
			dInput = new DataInputStream(socket.getInputStream());
			
			System.out.println("# ...Connected");

			// sends output to the socket
			out = new DataOutputStream(socket.getOutputStream());

		} catch (UnknownHostException u) {
			System.out.println(u);
		} catch (IOException i) {
			System.out.println(i);
		}
		this.connect();//connect client
		this.disconnect();
	}

	
	private void disconnect() {
		try {
			input.close();
			out.close();
			socket.close();
		} catch (IOException i) {
			System.out.println(i);
		}
	}

	private void connect() {
			String user = System.getProperty("user.name");
			sendReceive("HELO");
			sendReceive("AUTH " + user);
			scheduler();
			sendReceive("QUIT");
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
				int cpuSize = Integer.parseInt(server[4]);
				if (cpuSize>largestParameter) {
					largestParameter = cpuSize;
					largest=server[0];
				}
				RCVD=dInput.readLine();		
			}
			return largest;		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return("");
		}
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
	
	public static void main(String args[]) {
		client client = new client("127.0.0.1", 8096);
	}
	
}