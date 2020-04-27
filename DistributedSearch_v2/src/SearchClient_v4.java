import search.LuceneTester;
import java.io.*; 
import java.lang.Thread.State;
import java.net.*; 
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner; 
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class SearchClient_v4 
{ 
        final static int PeerPort = 2222;
        public final static String ID="A";
        public static int PeerServerPort = 2222;
        public static String PeerServerIP = "localhost";
        public static int hop = 1;
        public static List<String> addr=new ArrayList<String>();
        public static String QUERY=null;
        public static String Recieved_QUERY=null;
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
        public static String trackID="";
        
        public static String message="";
        public void getBroadcastPeerThread(String id,int h,String id_track)
        {
            hop=h;
            trackID=id_track+" "+id;
            
            File file = new File("peer_info_"+ID+".txt"); 
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
                    Logger.getLogger(SearchClient_v4.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) 
                {
                    Logger.getLogger(SearchClient_v4.class.getName()).log(Level.SEVERE, null, ex);
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
                            peer_dos.writeUTF(activeID+","+hop+","+trackID+"###"+QUERY);
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
                                        peer_dos.writeUTF(activeID+","+hop+","+trackID+"###"+"CLOSE");
                                        
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
                    String splitTrackID[]=trackID.split(" ");
                    int flag=0;
                    for(String j: splitTrackID)
                    {
                        if(activeID.equals(j))
                        {
                            flag=1;
                        }
                    }
                    if(flag==1)
                    {
                        continue;
                    }
                    if(!s.equals(id))
                    {
                    //    System.out.println("Connecting to Peer "+s);
                        try {
                            PeerServerPort=Integer.parseInt(i.split("\\s+")[1]);
                            PeerServerIP=i.split("\\s+")[0];
                            broadcastPeer.run();
                            broadcastPeer.join();
                               
                        } catch (Exception ex) 
                        {
                            Logger.getLogger(SearchClient_v3.class.getName()).log(Level.SEVERE, null, ex);
                        } 
                    }
                                
                }
                
                System.out.println("FINAL MESSAGE :"+message);
                String result=search.LuceneTester.main(QUERY);
                System.out.println("LUCENE TESTER MAIN RESULT ");
                String[] data=result.split("File:");
                String fileContent="";
                int ck1=0;
                for(String d : data)
                {
                   if(ck1==0)
                   {
                       ck1++;
                       continue;
                   }
                   String content = null;
                try 
                {
                    content = new String(Files.readAllBytes(Paths.get(d.trim())));
                    fileContent+=content+":::::::";
                } catch (IOException ex) {
                    Logger.getLogger(SearchClient_v4.class.getName()).log(Level.SEVERE, null, ex);
                }
                }
                message+="-"+ID+"-"+result+":::::::"+fileContent;
                //System.out.println(message);
            try {
                if(message!=null&&peerServer_dos!=null)
                {
                    peerServer_dos.writeUTF(message);
                    
                }
                else
                {
                    String[] displayMessage=message.split("DONE");
                    Map<String,String> Resultmap=new HashMap<String,String>();
                    String key=null;
                    String value=null;
                    String resultPane="";
                    for(String i:displayMessage)
                    {
                        //ProjectWindow.result.append(i);
                        //ProjectWindow.result.append("\n");
                        
                        key= i.substring(0, 2);
                        value=i.substring(3);
                        
                        
                       //- ProjectWindow.result.append(String.valueOf(key.charAt(1)));
                       //- ProjectWindow.result.append("\n");
                        resultPane+="<br />";
                        resultPane+=String.valueOf(key.charAt(1));
                        resultPane+="<br />";
                        
                        key=String.valueOf(key.charAt(1));
                        
                       //-- ProjectWindow.result.append(value);
                       //-- ProjectWindow.result.append("\n");
                        
                        String[] temp=value.split("File:");
                        value="";
                        int ck=0;
                        System.out.println("LENGTH :"+temp.length);
                        for(String j : temp)
                        {
                            //ProjectWindow.result.append(j);
                            //ProjectWindow.result.append("\n");
                            if(ck==0)
                            {    
                                ck++;
                                continue;
                            }
                            if(ck==temp.length-1)
                            {
                                String[] fj=j.split(":::::::");
                                
                                j=fj[0]+"  <a href='"+key+"-"+(ck-1)+"'>LINK</a>";
                                //Dump Content to File
                                int cck=1;
                                for(int iter=1;iter<fj.length;iter++)
                                {
                                    if(cck<=(ck))
                                    {
                                        String path = "dat\\result\\"+key+"_"+(iter-1)+".txt";
                                        Files.write( Paths.get(path.trim()), fj[iter].getBytes());
                                    }
                                    cck++;
                                }
                                resultPane+=j;
                                resultPane+="<br />";
                                value+=j+",";
                            }
                            else
                            {
                                j+="<a href='"+key+"-"+(ck-1)+"'>LINK</a>";
                                resultPane+=j;
                                resultPane+="<br />";
                                value+=j+",";
                            }
                            
                            ck++;
                        }
                        


                        
                        //result Listener
                        Resultmap.put(key, value);
                    }
                    System.out.println("RESULT PANE ----");
                    System.out.println(resultPane);
                    ProjectWindow.result.setText(resultPane);
                    for (Map.Entry<String, String> entry : Resultmap.entrySet()) 
                    {
                        System.out.println(entry.getKey() + "/" + entry.getValue());
                    }
                }


            } catch (Exception ex) {
                Logger.getLogger(SearchClient_v4.class.getName()).log(Level.SEVERE, null, ex);
            }
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
                                
                                while(true)
                                {
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
                                                 Recieved_QUERY=received;
                                                 String[] info=parts[0].split(",");
                                                 String i_d=info[0];
                                                 int h=Integer.parseInt(info[1]);
                                                 String t=info[2];
                                                 if(received.equals("CLOSE")){
                             
                                                     break;
                                                 }
                                                 else{
                                                     QUERY=received;
                                                   if(h>0)
                                                   {
                                                       h--;
                                                       SearchClient_v4 sc=new SearchClient_v4();
                                                       sc.getBroadcastPeerThread(i_d,h,t);
                                                   }
                                                   else if(h==0)
                                                   {
                                                        String result=search.LuceneTester.main(received);
                                                        System.out.println("LUCENE TESTER MAIN RESULT "+result);
                                                        String[] data=result.split("File:");
                                                        String fileContent="";
                                                        int ck1=0;
                                                        for(String d : data)
                                                        {
                                                            if(ck1==0)
                                                            {
                                                                ck1++;
                                                                continue;
                                                            }
                                                            String content = null;
                                                            try 
                                                            {
                                                                content = new String(Files.readAllBytes(Paths.get(d.trim())));
                                                                fileContent+=content+":::::::";
                                                            } catch (IOException ex) 
                                                            {
                                                                Logger.getLogger(SearchClient_v4.class.getName()).log(Level.SEVERE, null, ex);
                                                            }
                                                        }
                                                        peerServer_dos.writeUTF("-"+i_d+"-"+result+":::::::"+fileContent);
                                                   }
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

          

	} 
} 
