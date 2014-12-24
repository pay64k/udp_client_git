package ws.dtu;

import java.io.*;
import java.net.*;
import java.util.*;
import org.omg.CORBA.portable.IDLEntity;

public class QuoteClient {
    //States---------------------------
    private enum State{IDLE,START, WFR1, WFR2, REC_STREAM};
        static State currentState;
        static State nextState=State.START;
    //---------------------------------
             static Timer timer = new Timer(10000);   
    public static void main(String[] args) throws IOException {
        

        //start timer:
        timer.start();
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = new byte[256];
        String data=null;
        InetAddress address = InetAddress.getByName("192.168.1.50");////enter ip here
        DatagramPacket send_packet;
        DatagramPacket recv_packet;
        int pkt_amount=0;
        ArrayList recv_seqList=new ArrayList();
        ArrayList recv_dataList=new ArrayList();
        Integer recvd_counter=0;
        int pkt_timeout = 500;
        Map<Integer,String> map = new TreeMap<Integer, String>();
        
        ArrayList missing_pkt_num=new ArrayList();
        
    while(true){
        currentState=nextState;
        
        switch(currentState){
            
            case START:
                recvd_counter=0;
                System.out.println("In state START");
                data="REQUEST:";
                buf=data.getBytes();
                send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                System.out.println("Sending: " + new String(send_packet.getData(), 0, send_packet.getLength() ));
                socket.send(send_packet);
                //timer.reset();
                nextState=State.WFR1;
                buf = new byte[256];
                break;
                
            case WFR1:
                System.out.println("In state WFR1");
                buf = new byte[256];
                recv_packet = new DatagramPacket(buf, buf.length);
                socket.setSoTimeout(pkt_timeout);//----------- timeout for a packet which is not arriving
                try {
                    socket.receive(recv_packet);
                } catch (Exception e) {
                    data="REQUEST:";
                    buf=data.getBytes();
                    send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                    System.out.println("Sending: " + new String(send_packet.getData(), 0, send_packet.getLength() ));
                    socket.send(send_packet);
                }
                
                String received = new String(recv_packet.getData(), 0, recv_packet.getLength());
                System.out.println("Recieved: " + received);
                
                if (received.startsWith("pkt_amount:")) {
                    pkt_amount=Integer.parseInt(received.substring(11));
                    System.out.println("Amount of packets(int): " + pkt_amount);
                    
                    data="ACK";
                    buf=data.getBytes();
                    send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                    System.out.println("Sending: " + new String(send_packet.getData(), 0, send_packet.getLength() ));
                    socket.send(send_packet);
                    
                    nextState=State.REC_STREAM; 
                    for (int i = 0; i < pkt_amount; i++) {
                                if (map.containsKey(i)) {
                                    System.out.println("Got index: "+ i);
                                }
                                else{
                                    System.out.println("Missing: "+i);
                                    map.put(i, null);
                                }
                            }
                    timer.reset();
                }
                
                
                break;
                
            case REC_STREAM:
                System.out.println("In state REC_STREAM");
                missing_pkt_num.clear();
                
                    buf = new byte[256];
                    recv_packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(recv_packet);
                        timer.reset();
                    } catch (Exception e) {
                    data="is_all_sent?";
                    buf=data.getBytes();
                    send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                    System.out.println("Sending: " + new String(send_packet.getData(), 0, send_packet.getLength() ));
                    socket.send(send_packet);
                    }  
                    
                received = new String(recv_packet.getData(), 0, recv_packet.getLength());
                System.out.println("Recieved: " + received);
                    
                    if (received.startsWith("|")) {
//                        recvd_counter++;                   
                        char[] array = received.toCharArray();
                        int distance=0;
                        //count distance between two '|' characters to find the length of the packet number
                        //first one is always '|'
                        for (int j = 1; j < array.length; j++) {
                            if (array[j] != '|') {
                                distance++;
                            }
                            else{
                                //System.out.println(distance);
                                //System.out.println(received.substring(1, distance+1));
                                //System.out.println(received.substring(distance+2,received.length()));
                                recv_seqList.add(received.substring(1, distance+1));
                                recv_dataList.add(received.substring(distance+2,received.length()));
                                //using map:
                                Integer seq = Integer.valueOf(received.substring(1, distance+1));
                                System.out.println("Seq: " + seq);
                                String dataString = received.substring(distance+2,received.length());
                                System.out.println("dataString: " + dataString);
                                
                                for(Map.Entry<Integer,String> entry : map.entrySet()) {
                                    if(entry.getKey().equals(seq)){
                                        
                                        if(entry.getValue() == null){
                                    map.put(seq, dataString);
                                    recvd_counter++; 
                                    System.out.println("Received packets++: "+ recvd_counter);
                                    System.out.println("Map size: "+map.size());
                                    break;
                                        }else{
                                        System.out.println("Allready got packet nr: " + seq);
                                    
                                    break;
                                } 
                                }
                                      
                                }
                                
                            }
                          }
                    }
                    else if (received.equals("sent_all")) {
                        //end if got all pkts
                        if (pkt_amount==recvd_counter) {
                            System.out.println("Got_all_pkts!");
                                data="got_all_pkts";
                                buf=data.getBytes();
                                send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                                System.out.println("Sending: " + new String(send_packet.getData(), 0, send_packet.getLength() ));
                                socket.send(send_packet);
                                System.out.println("Recieved text:");
                                for(Map.Entry<Integer,String> entry : map.entrySet()) {
                              System.out.println(entry.getKey() + " => " + entry.getValue());
                                } 
                            nextState=State.IDLE;
                        }
                        else{
                            //fill in "null" for packet numbers that are missing
                            for (int i = 0; i < pkt_amount; i++) {
                                if (map.containsKey(i)) {
                                    System.out.println("Got index: "+ i);
                                }
                                else{
                                    System.out.println("Missing: "+i);
                                    map.put(i, null);
                                }
                            }
                            //find missing pkts:
                            for(Map.Entry<Integer,String> entry : map.entrySet()) {
                              System.out.println(entry.getKey() + " => " + entry.getValue());
 
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
                                System.out.println("Sending missing: " + new String(send_packet.getData(), 0, send_packet.getLength() ));
                                socket.send(send_packet);
                            }
                                data="END";
                                buf=data.getBytes();
                                send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                                System.out.println("Sending missing: " + new String(send_packet.getData(), 0, send_packet.getLength() ));
                                socket.send(send_packet);
                                
                            nextState=State.WFR2;
                        }
                    }
                break;
                
            case WFR2:
                System.out.println("In State WFR2");
                    buf = new byte[256];
                    recv_packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(recv_packet);
                        timer.reset();
                    } catch (Exception e) {
                        //resend missing_pkts_numbers according to CLIENT FSM v3.2
                        for (int i = 0; i < missing_pkt_num.size(); i++) {
                        data=missing_pkt_num.get(i).toString();
                        buf=data.getBytes();
                        send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                        System.out.println("Sending missing: " + new String(send_packet.getData(), 0, send_packet.getLength() ));
                        socket.send(send_packet);
                        }
                        data="END";
                        buf=data.getBytes();
                        send_packet = new DatagramPacket(buf, buf.length, address, 4445);
                        System.out.println("Sending missing: " + new String(send_packet.getData(), 0, send_packet.getLength() ));
                        socket.send(send_packet);
                    }
                    received = new String(recv_packet.getData(), 0, recv_packet.getLength());
                    System.out.println("Recieved: " + received);
                    if(received.equals("ACK")){
                        nextState=State.REC_STREAM;
                    }
                    else if (received.startsWith("|")) {
                    nextState=State.REC_STREAM;
                }
                break;
                
            case IDLE:
                //System.out.println("In state IDLE");
                break;
        }
       
    }
   }
        
}