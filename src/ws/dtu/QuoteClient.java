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
             static Timer timer = new Timer(5000);   
    public static void main(String[] args) throws IOException {
        

        //start timer:
        timer.start();
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = new byte[256];
        String data=null;
        InetAddress address = InetAddress.getByName("127.0.0.1");////enter ip here
        DatagramPacket send_packet;
        DatagramPacket recv_packet;
        int pkt_amount=0;
        ArrayList recv_seqList=new ArrayList();
        ArrayList recv_dataList=new ArrayList();
        Integer recvd_counter=0;
        int pkt_timeout = 300;
        
    while(true){
        currentState=nextState;
        
        switch(currentState){
            
            case START:
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
                buf = new byte[256];
                recv_packet = new DatagramPacket(buf, buf.length);
                socket.setSoTimeout(pkt_timeout);//----------- timeout for a packet which is not arriving
                try {
                    socket.receive(recv_packet);
                } catch (Exception e) {
                    data="isNAK:";
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
                    timer.reset();
                }
                
                
                break;
                
            case REC_STREAM:
                for (int i = 0; i < pkt_amount; i++) {
                    buf = new byte[256];
                    recv_packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(recv_packet);
                    } catch (Exception e) {
                        System.out.println("No_pkts!");
                    }  
                    
                received = new String(recv_packet.getData(), 0, recv_packet.getLength());
                System.out.println("Recieved: " + received);
                    
                    if (received.startsWith("|")) {
                        recvd_counter++;                   
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
                                break;
                            }
                            
                        }
                    }

                
                }

                
                break;
                
            case IDLE:
                break;
        }
        //System.out.println(timer.getTimerModulo(10));
    }
//        DatagramSocket socket = new DatagramSocket();
//     
//
//                
//        
//        // send request
//        String test="REQUEST:";
//        byte[] buf_transmit = new byte[256];
//        byte[] buf_recieve = new byte[256];
//        buf_transmit=test.getBytes();
//       
//        
//        InetAddress address = InetAddress.getByName("127.0.0.1");
//        DatagramPacket packet = new DatagramPacket(buf_transmit, buf_transmit.length, address, 4445);
//       
//        socket.send(packet);
//        System.out.println("Sending: " + new String(packet.getData(), 0, packet.getLength() ));
//        
//            // get response
//        packet = new DatagramPacket(buf_recieve, buf_recieve.length);
//        socket.receive(packet);
//       
//	    // display response
//        String received = new String(packet.getData(), 0, packet.getLength());
//        System.out.println("Recieved: " + received);
//        
//        
//        socket.close();

    }
        
}