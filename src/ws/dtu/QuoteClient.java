package ws.dtu;

import java.awt.Graphics;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.CORBA.portable.IDLEntity;

//comment/uncomment System.out.prints to improve/degrade performance

public class QuoteClient extends Thread{
    //States---------------------------
    public static enum State{IDLE,START, WFR1, WFR2, REC_STREAM,TEST};
        public static State currentState;
        public static State nextState=State.START;
    //---------------------------------
             static Timer timer = new Timer(40000); 
             
             public static int packet_size=256; 
             
             public static int message_size = 512; //change for packet size only here!!------------------!!             
    @Override
             public void run(){
        try {
            
            main2(null);
            
        } catch (IOException ex) {
            Logger.getLogger(QuoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
             }
             
    public static void main2(String[] args) throws IOException {
        

        //start timer:
        timer.start();
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = new byte[packet_size];
        String data=null;
        InetAddress address = InetAddress.getByName("192.168.1.50");////enter ip here 192.168.1.50  127.0.0.1 
        DatagramPacket send_packet;
        DatagramPacket recv_packet;
        int pkt_amount=0;
        ArrayList recv_seqList=new ArrayList();
        ArrayList recv_dataList=new ArrayList();
        Integer recvd_counter=0;
        int pkt_timeout = 1;
        Map<Integer,String> map = new TreeMap<Integer, String>();
        
        ArrayList missing_pkt_num=new ArrayList();
        
    while(true){
        currentState=nextState;
        
        
        
        switch(currentState){
            
            case START:
                recvd_counter=0;
                //System.out.println("In state START");
                data="REQUEST:";
                buf=data.getBytes();
                send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                //System.out.println("Sending: REQUEST:");
                socket.send(send_packet);
                //timer.reset();
                nextState=State.WFR1;
                buf = new byte[packet_size];
                
                break;
                
            case WFR1:
                //System.out.println("In state WFR1");
                buf = new byte[packet_size];
                recv_packet = new DatagramPacket(buf, buf.length);
                socket.setSoTimeout(pkt_timeout);//----------- timeout for a packet which is not arriving
                try {
                    socket.receive(recv_packet);
                } catch (Exception e) {
                    data="REQUEST:";
                    buf=data.getBytes();
                    send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                    
                    socket.send(send_packet);
                    //System.out.println("Sending: REQUEST");
                }
                
                String received = new String(recv_packet.getData(), 0, recv_packet.getLength());
                //System.out.println("Recieved: " + received);
                
                if (received.startsWith("pkt_amount:")) {
                    pkt_amount=Integer.parseInt(received.substring(11));
                    //System.out.println("Amount of packets(int): " + pkt_amount);
                    
                    packet_size =  (int)(Math.log10(pkt_amount)+1)+message_size+2;
                    
                    MyPanel.set_size(pkt_amount);//set size of painted rectangles
                    
                    data="ACK";
                    buf=data.getBytes();
                    send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                    socket.send(send_packet);
                    //System.out.println("Sending: ACK");
                    
                    nextState=State.REC_STREAM; 
                    for (int i = 0; i < pkt_amount; i++) {
                                if (map.containsKey(i)) {
                                    //System.out.println("Got index: "+ i);
                                }
                                else{
                                    //System.out.println("Missing: "+i);
                                    map.put(i, null);
                                }
                            }
                    timer.reset();
                }
                
                
                break;
                
            case REC_STREAM:
                //System.out.println("In state REC_STREAM");
                missing_pkt_num.clear();
                
                    buf = new byte[packet_size];
                    recv_packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(recv_packet);
                        timer.reset();
                    } catch (Exception e) {
                    data="is_all_sent?";
                    buf=data.getBytes();
                    send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                    socket.send(send_packet);
                    //System.out.println("Sending: is_all_sent?");
                    }  
                    
                received = new String(recv_packet.getData(), 0, recv_packet.getLength());
                //System.out.println("Recieved: message");
                    
                    if (received.startsWith("|")) {
                        timer.reset();
                        int first = received.indexOf("|");
                        int second = received.indexOf("|", first+1);
                        //System.out.println(first + " " + second);
                        Integer seq = Integer.valueOf(received.substring(first+1, second));
                        //System.out.println(seq);
                        String dataString = received.substring(second+1,received.length());
                        
                        //System.out.println("Received message nr: " + seq);
                        
                        //System.out.println("REC.length: " + received.length());
                        //System.out.println("LENGTH: " + dataString.length());
                                     
                        
                                if (map.get(seq)==null) {
                                    map.put(seq, dataString);
                                    MyPanel.updatePackets(map);
                                    recvd_counter++; 
                                    }
                        

                    }
                    else if (received.equals("sent_all")) {
                        //end if got all pkts
                        if (pkt_amount==recvd_counter) {
                            System.out.println("Got_all_pkts!");
                                data="got_all_pkts";
                                buf=data.getBytes();
                                send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                                socket.send(send_packet);
                                //System.out.println("Sending: got_all_pkts");

                                Frame.elapsed_time=System.currentTimeMillis();
                                long exec_time = Frame.elapsed_time - Frame.time;
                                System.out.println("Execution time: " + exec_time + " ms");
                                
                            try (PrintWriter writer = new PrintWriter("C:/testJava/result.txt", "UTF-8")) {
                                for(Map.Entry<Integer,String> entry : map.entrySet()) {
                                    //System.out.println(entry.getKey() + " => " + entry.getValue());
                                    writer.print(entry.getValue());
                                } 
                                System.out.println("Written to file (C:/testJava/result.txt)...");
                                
                            }
                            timer.reset();
                            nextState=State.IDLE;
                        }
                        else{
                            //fill in "null" for packet numbers that are missing
//                            for (int i = 0; i < pkt_amount; i++) {
//                                if (map.containsKey(i)) {
//                                    //System.out.println("Got index: "+ i);
//                                }
//                                else{
//                                    //System.out.println("Missing: "+i);
//                                    map.put(i, null);
//                                }
//                            }
                            //find missing pkts:
                            for(Map.Entry<Integer,String> entry : map.entrySet()) {
                              //System.out.println(entry.getKey() + " => " + entry.getValue());
 
                              //missing packets:
                                if (entry.getValue()==null) {
                                    missing_pkt_num.add(entry.getKey());
                                }
                              
                            }
                            //send missing_pkt_numbers:
                            for (int i = 0; i < missing_pkt_num.size(); i++) {
                                data=missing_pkt_num.get(i).toString();
                                buf=data.getBytes();
                                send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                                //System.out.println("Sending missing: " + data);
                                socket.send(send_packet);
                                
                            }
                                data="END";
                                buf=data.getBytes();
                                send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                                socket.send(send_packet);
                                //System.out.println("Sending: END");
                                
                            nextState=State.WFR2;
                        }
                    }
                break;
                
            case WFR2:
                //System.out.println("In State WFR2");
                    buf = new byte[packet_size];
                    recv_packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(recv_packet);
                        timer.reset();
                    } catch (Exception e) {
                        //resend missing_pkts_numbers
                        for (int i = 0; i < missing_pkt_num.size(); i++) {
                        data=missing_pkt_num.get(i).toString();
                        buf=data.getBytes();
                        send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                        //System.out.println("Sending missing: " + new String(send_packet.getData(), 0, send_packet.getLength() ));
                        socket.send(send_packet);
                        }
                        data="END";
                        buf=data.getBytes();
                        send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                        socket.send(send_packet);
                        //System.out.println("Sending: END");
                    }
                    received = new String(recv_packet.getData(), 0, recv_packet.getLength());
                    //System.out.println("Recieved: " + received);
                    if(received.equals("ACK")){
                        nextState=State.REC_STREAM;
                        timer.reset();
                    }
                    else if (received.startsWith("|")) {
                    nextState=State.REC_STREAM;
                    timer.reset();
                }
                break;
                
            case IDLE:
                //System.out.println("In state IDLE");
                
                timer.reset();
                break;
                
            case TEST:
                
                break;
        }
       
    }
   }

}