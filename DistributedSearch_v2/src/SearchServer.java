import java.io.*; 
import java.util.*; 
import java.net.*; 
import java.util.logging.Level;
import java.util.logging.Logger;
  
// Server class 
public class SearchServer  
{ 
  
    // Vector to store active clients 
    static Vector<ClientHandler> ar = new Vector<>(); 
      
    // counter for clients 
    static int i = 0; 
  
    public static void main(String[] args) throws IOException  
    { 
        // server is listening on port 1234 
        ServerSocket ss = new ServerSocket(9876); 
          
        Socket s; 
        Collections.synchronizedCollection(ar);  
        // running infinite loop for getting 
        // client request 
        while (true)  
        { 
            System.out.println("WAITING FOR NEW REQUEST ");
            // Accept the incoming request 
            s = ss.accept(); 
  
            System.out.println("New client request received : " + s); 
              
            // obtain input and output streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
              
            System.out.println("Creating a new handler for this client..."); 
  
            // Create a new handler object for handling this request. 
            ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos); 
  
            // Create a new Thread with this object. 
            Thread t = new Thread(mtch); 
              
            System.out.println("Adding this client to active client list"); 
  
            // add this client to active clients list 
            ar.add(mtch); 
  
            // start the thread. 
            t.start(); 
  
            // increment i for new client. 
            // i is used for naming only, and can be replaced 
            // by any naming scheme 
            i++; 
  
        } 
    } 
} 
  
// ClientHandler class 
class ClientHandler implements Runnable  
{ 
    Scanner scn = new Scanner(System.in); 
    private String name; 
    final DataInputStream dis; 
    final DataOutputStream dos; 
    Socket s;
    String peerPort;
    boolean isloggedin; 
      
    // constructor 
    public ClientHandler(Socket s, String name, 
                            DataInputStream dis, DataOutputStream dos) { 
        this.dis = dis; 
        this.dos = dos; 
        this.name = name; 
        this.s = s; 
        this.isloggedin=true; 
    } 
  
    @Override
    public void run() { 
            System.out.println("THREAD STARTED");
            while(true){
            String received = null; 
        try { 
            while(dis.available()>0){
                System.out.println("DIS AVAILABLE");
            received = dis.readUTF();
            System.out.println(received); 
            if(received.split("\\s+")[0].equals("CONNECT")){
                   this.name=received.split("\\s+")[1];
                   this.peerPort=received.split("\\s+")[2];
            }
            if(received.equals("LOGOUT")){ 
                    System.out.println("DISCONNECTING");
                    this.isloggedin=false;
                    SearchServer.ar.remove(this);
                    this.s.close();
                    this.dis.close(); 
                    this.dos.close();
                    break; 
                }
            
            //Iterator value = UDPServer.ar.iterator();  
            System.out.println("The iterator values are: "); 
            int count=0;
            Vector<ClientHandler> copy = new Vector<>(SearchServer.ar);
            for (ClientHandler s : copy) {
                if(s.name.equals(this.name))
                {
                    count++;
                    if(count>1)
                    {
                        SearchServer.ar.remove(s);
                        count--;
                    }
                }
                
            }
            Vector<ClientHandler> copy1 = new Vector<>(SearchServer.ar);
            System.out.println(SearchServer.ar.size());
            for (ClientHandler s : copy1) {
                System.out.println(s.name+" "+s.peerPort);
                dos.writeUTF(s.s.getInetAddress()+" "+s.s.getPort()+" "+s.peerPort+" "+s.name);
            }
            dos.writeUTF("###");
            System.out.println(s.getInetAddress()+" "+s.getPort());
            /*for(int i=0; i< UDPServer.ar.size(); i++){
                
                    System.out.println(UDPServer.ar.get(i).name);
            } */
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
            }
            
         
} 
    }