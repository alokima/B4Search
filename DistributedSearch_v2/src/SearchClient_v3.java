/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


// Java implementation for multithreaded chat client 

import search.LuceneTester;
import java.io.*; 
import java.lang.Thread.State;
import java.net.*; 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner; 
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchClient_v3 
{ 
        final static int PeerPort = 2224;
        public final static String ID="C";
        public static int PeerServerPort = 2222;
        public static String PeerServerIP = "localhost";
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
        public static Map<String,String> result_map=new HashMap<String,String>();
        public static String activeID=null;
        
        public static String message="";
        public void getBroadcastPeerThread(String id)
        {
            File file = new File("peer_info.txt"); 
            BufferedReader br = null; 
            String st;
            try {
                br = new BufferedReader(new FileReader(file));
                while ((st = br.readLine()) != null)
                {
                    if(!addr.contains(st))
                    {
                        addr.add(st);
                    }
                } 
                for (String i : addr) {
                   // System.out.println(i);
                    }
                } catch (FileNotFoundException ex) 
                {
                    Logger.getLogger(SearchClient_v3.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) 
                {
                    Logger.getLogger(SearchClient_v3.class.getName()).log(Level.SEVERE, null, ex);
                }
                 
           Thread broadcastPeer = new Thread(new Runnable()
		{ 
                    @Override
                    public void run() {
                        try {
                           // System.out.println("blah");
                            peer_s = new Socket(PeerServerIP, PeerServerPort);
                            peer_dis = new DataInputStream(peer_s.getInputStream());
                            peer_dos = new DataOutputStream(peer_s.getOutputStream());
                            peer_dos.writeUTF(activeID+"###"+QUERY);
                            outerloop:
                            while(true)
                            {
                                while(peer_dis.available()>0){
                                    String msg = peer_dis.readUTF(); 
                                    //System.out.println(msg);
                                    message+=msg;
                                    if(msg.equals("DONE"))
                                    {
                                       // System.out.println("Inner Server Thread");
                                        peer_dos.writeUTF(activeID+"###"+"CLOSE");
                                        break outerloop;
                                    }
                                    else
                                    {
                                        result_map.put(activeID, msg);
                                    }
                                }
                            }
                           } catch (IOException ex) {
                                            Logger.getLogger(SearchClient_v3.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                        }
                });
                
                //System.out.println("BroadcastPeer From Peer "+id+" "+addr.size());
                int k=1;
                int size=addr.size();
                
                for (String i : addr) 
                {
                  //  System.out.println("BroadcastPeer DATA"+i);
                    String s=i.split("\\s+")[2];
                    activeID=s;
                    if(!s.equals(id))
                    {
                    //    System.out.println("Connecting to Peer "+s);
                        try {
                            PeerServerPort=Integer.parseInt(i.split("\\s+")[1]);
                            PeerServerIP=i.split("\\s+")[0];
                            broadcastPeer.run();
                            broadcastPeer.join();
                               
                        } catch (Exception ex) {
                                Logger.getLogger(SearchClient_v3.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                    }
                                
                }
                
                System.out.println("FINAL MESSAGE :"+message);
                
                //ResultWindow.showWindow();
                //ResultWindow.links(result_map);
        }
        public static void main(String args[]) throws UnknownHostException, IOException 
	{ 
                Scanner scn = new Scanner(System.in); 
		
		peer_ss = new ServerSocket(PeerPort);
                
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
                                           // System.out.println("blah Server :"+ID);
                                            peerServer_dis = new DataInputStream(peerServer_s.getInputStream()); 
                                            peerServer_dos = new DataOutputStream(peerServer_s.getOutputStream());
                                            outerloop:
                                            while(true)
                                            {
                                                 while(peerServer_dis.available()>0){
                                                 String received = peerServer_dis.readUTF();
                                                 System.out.println(received);
                                                 String[] parts = received.split("###");
                                                 received=parts[1];
                                                 String i_d=parts[0];
                                                 if(received.equals("CLOSE")){
                             
                                                     break;
                                                 }
                                                 else{
                                                   String result=search.LuceneTester.main(received);
                                                   peerServer_dos.writeUTF("-"+i_d+"-"+result);
                                                 }
                                                 }
                                                 peerServer_dos.writeUTF("DONE");
                                            }
                                        } catch (IOException ex) {
                                            Logger.getLogger(SearchClient_v3.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }.start();
                                    
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(SearchClient_v3.class.getName()).log(Level.SEVERE, null, ex);
                            }
				 
			} 
		});
                
		
                serverPeer.start();
                
                ProjectWindow.main(null);
                //QUERY="INDIA";
                //LuceneTester.main(null);
                //broadcastPeer.start();
                //broadcastPeer.start();
          

	} 
} 
