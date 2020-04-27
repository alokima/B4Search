// Java implementation for multithreaded chat client 
import search.LuceneTester;
import java.io.*; 
import java.lang.Thread.State;
import java.net.*; 
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner; 
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchClient 
{ 
	final static int ServerPort = 9876;
        final static int PeerPort = 9879;
        final static String ID="B";
        public static int PeerServerPort = 9000;
        public static List<String> addr=new ArrayList<String>();
        public static String QUERY=null;
        public static Socket peer_s;
	public static ServerSocket peer_ss;
        public static Socket peerServer_s;
        
        public static DataInputStream peer_dis = null;
        public static DataOutputStream peer_dos = null;
        public static DataInputStream peerServer_dis=null;
        public static DataOutputStream peerServer_dos=null;
        public static Thread broadcastPeer=null;
        public boolean lock=false;
        public Thread getBroadcastPeerThread(String id)
        {
           Thread broadcastPeer = new Thread(new Runnable()
		{ 
			@Override
			public void run() {
                            System.out.println("BroadcastPeer From Peer "+id+" "+addr.size());
                            int k=1;
                            int size=addr.size();
                            lock=false;
                            for (String i : addr) {
                                System.out.println("BroadcastPeer Before DATA"+lock);
                                while(lock){}
                                System.out.println("BroadcastPeer DATA"+i);
                                if(i.charAt(0)=='/'){
                                String s=i.split("\\s+")[3];
                               
                                if(!s.equals(id))
                                {
                            System.out.println("Connecting to Peer "+s);
                            try {
                                PeerServerPort=Integer.parseInt(i.split("\\s+")[2]);
                                if(k!=size-1)
                                {
                                    lock=true;
                                    k++;
                                }
                                
                                new Thread()
                                {
                                    public void run() {
                                        try {
                                            
                                            System.out.println("blah");
                                            peer_s = new Socket("172.26.221.126", PeerServerPort);
                                            peer_dis = new DataInputStream(peer_s.getInputStream());
                                            peer_dos = new DataOutputStream(peer_s.getOutputStream());
                                            peer_dos.writeUTF(QUERY);
                                            outerloop:
                                            while(true)
                                            {
                                                while(peer_dis.available()>0){
                                                String msg = peer_dis.readUTF(); 
                                                System.out.println(msg);
                                                if(msg.equals("DONE"))
                                                {
                                                    peer_dos.writeUTF("CLOSE");
                                                    lock=false;
                                                    break outerloop;
                                                }
                                                }
                                            }
                                        } catch (IOException ex) {
                                            Logger.getLogger(SearchClient.class.getName()).log(Level.SEVERE, null, ex);
                                        } 
                                    }
                                }.start();
                               
                            } catch (Exception ex) {
                                Logger.getLogger(SearchClient.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                            }
                                }
                           }
                            
                            }
                             
		});
            return broadcastPeer;
        }
        public static void main(String args[]) throws UnknownHostException, IOException 
	{ 
		Scanner scn = new Scanner(System.in); 
		
		// getting localhost ip 
		InetAddress ip = InetAddress.getByName("localhost"); 
		
		// establish the connection 34.201.66.83 
		Socket s = new Socket("34.201.66.83", ServerPort);
                
                peer_ss = new ServerSocket(PeerPort);
                
		// obtaining input and out streams 
		DataInputStream dis = new DataInputStream(s.getInputStream()); 
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                
		// sendMessage thread 
		Thread sendMessage = new Thread(new Runnable() 
		{ 
			@Override
			public void run() { 
                            // write on the output stream 
                            while(true){
                            System.out.println("Connecting to Server from "+ID);
                            try {
                                dos.writeUTF("CONNECT"+" "+ID+" "+PeerPort);
                                dos.writeUTF("WRITING TO SERVER DONE ");
                                Thread.sleep(10000);
                                for (String i : addr) {
                                    System.out.println(i); 
                                }    
                                //QUERY="INDIA";
                                //LuceneTester.main(null);
                                //broadcastPeer.start();
                            } catch (IOException ex) {
                                Logger.getLogger(SearchClient.class.getName()).log(Level.SEVERE, null, ex);
                            }catch (InterruptedException ex) {
                                Logger.getLogger(SearchClient.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                            }
			} 
		}); 
		
		// readMessage thread 
		Thread readMessage = new Thread(new Runnable() 
		{ 
			@Override
			public void run() { 
                                
				while (true) {
                                    
					try
                                        { // read the message sent to this client 
                                            //int flag=0;
                                            while(dis.available()>0){
                                              
                                              /*if(flag==0)
                                              {
                                                  addr.clear();
                                                  flag=1;
                                              }*/
                                              String msg = dis.readUTF(); 
                                              if(!addr.contains(msg))
                                              {
                                                  addr.add(msg);
                                              }
                                              System.out.println(msg);
                                          }
                                             
					} catch (IOException e) { 

						e.printStackTrace(); 
					} 
				} 
			} 
		}); 
                
             broadcastPeer = new Thread(new Runnable() 
		{ 
			@Override
			public void run() {
                            System.out.println("BroadcastPeer "+addr.size());
                            for (String i : addr) {
                                System.out.println("BroadcastPeer DATA"+i);
                                if(i.charAt(0)=='/'){
                                String s=i.split("\\s+")[3];
                               
                                if(!s.equals(ID))
                                {
                            System.out.println("Connecting to Peer");
                            try {
                                PeerServerPort=Integer.parseInt(i.split("\\s+")[2]);
                                new Thread()
                                {
                                    public void run() {
                                        try {
                                            System.out.println("blah");
                                            peer_s = new Socket("localhost", PeerServerPort);
                                            peer_dis = new DataInputStream(peer_s.getInputStream());
                                            peer_dos = new DataOutputStream(peer_s.getOutputStream());
                                            peer_dos.writeUTF(QUERY);
                                            outerloop:
                                            while(true)
                                            {
                                                while(peer_dis.available()>0){
                                                String msg = peer_dis.readUTF(); 
                                                System.out.println(msg);
                                                if(msg.equals("DONE"))
                                                {
                                                    peer_dos.writeUTF("CLOSE");
                                                    break outerloop;
                                                }
                                                }
                                            }
                                        } catch (IOException ex) {
                                            Logger.getLogger(SearchClient.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }.start();
                               
                            } catch (Exception ex) {
                                Logger.getLogger(SearchClient.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                            }
                                }
                           }
                            
                            }
                             
		});

                Thread serverPeer = new Thread(new Runnable() 
		{ 
			@Override
			public void run() { 
                            try { 
                                
                                while(true){
                                    String received = null;
                                    peerServer_s = peer_ss.accept();
                                    new Thread()
                                {
                                    public void run() {
                                        try {
                                            System.out.println("blah Server");
                                            peerServer_dis = new DataInputStream(peerServer_s.getInputStream()); 
                                            peerServer_dos = new DataOutputStream(peerServer_s.getOutputStream());
                                            outerloop:
                                            while(true)
                                            {
                                                 while(peerServer_dis.available()>0){
                                                 String received = peerServer_dis.readUTF();
                                                 System.out.println(received);
                                                 if(received.equals("CLOSE")){
                                                     break outerloop;
                                                 }
                                                 else{
                                                   String result=search.LuceneTester.main(received);
                                                   peerServer_dos.writeUTF(result);
                                                 }
                                                 }
                                                 peerServer_dos.writeUTF("DONE");
                                            }
                                        } catch (IOException ex) {
                                            Logger.getLogger(SearchClient.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }.start();
                                    
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(SearchClient.class.getName()).log(Level.SEVERE, null, ex);
                            }
				 
			} 
		});
                
		sendMessage.start(); 
		readMessage.start();
                serverPeer.start();
                
                ProjectWindow.main(null);
                //QUERY="INDIA";
                //LuceneTester.main(null);
                //broadcastPeer.start();
                //broadcastPeer.start();
          

	} 
} 
