// A Java program for a Client 
import java.net.*;
import java.util.HashMap;
import java.io.*; 
  
public class client 
{ 
    // initialize socket and input output streams 
    private Socket socket            = null; 
    private DataInputStream  input   = null; 
    private DataOutputStream out     = null;
    private DataInputStream  dInput  = null;

    // constructor to put ip address and port 
    public client(String address, int port) 
    { 
        // establish a connection 
        try
        { 
            socket = new Socket(address, port); 
            System.out.println("Connected"); 
  
            // takes input from terminal 
            
            input  = new DataInputStream(System.in); 
            
            dInput = new DataInputStream(socket.getInputStream());
  
            // sends output to the socket 
            out    = new DataOutputStream(socket.getOutputStream()); 
            out.write("HELO\n".getBytes());
            out.write("AUTH Jake\n".getBytes());
        } 
        catch(UnknownHostException u) 
        { 
            System.out.println(u); 
        } 
        catch(IOException i) 
        { 
            System.out.println(i); 
        } 
        
        // string to read message from input 
        String str1 = ""; 
        String str2 = "";
  
        // keep reading until "Over" is input 
        while (!str1.equals("Over")) 
        { 
            try
            { 
                str1 = input.readLine() + "\n"; 
                out.write(str1.getBytes()); 
                str2 = dInput.readLine();
               
                
              System.out.println("Server: " + str2);
              /*  if(input.readLine().equals("REDY")) {
                System.out.println(str2);
                } */
            } 
            catch(IOException i) 
            { 
                System.out.println(i); 
            } 
        } 
  
        // close the connection 
        try
        { 
            input.close(); 
            out.close(); 
            socket.close(); 
        } 
        catch(IOException i) 
        { 
            System.out.println(i); 
        } 
    } 
  
    public static void main(String args[]) 
    { 
        client client = new client("127.0.0.1", 8096); 
    } 
    
    //this section is for scheduling
    
    public class jobMap{
    	public jobMap(Integer jobID, Integer runtime, 
    			Integer jCores, Integer jMemory, Integer jDisk) {
    		
    		
    		
    	}
    }
    
    jobMap test = new jobMap(1,2,3,4,5);
    serverMap test2 = new serverMap("Large", 2,3,4);
    
    
    
    
    public class serverMap{
    	public serverMap(String a, Integer cores, Integer memory, Integer disk) {
    		
    		
    	}
    }
    
}