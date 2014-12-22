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
        
    public static void main(String[] args) throws IOException {
        
        Timer timer = new Timer(5000);
        //start timer:
        timer.start();
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = new byte[256];
        String data=null;
        InetAddress address = InetAddress.getByName("127.0.0.1");
        DatagramPacket packet;
        
    while(true){
        currentState=nextState;
        
        switch(currentState){
            
            case START:
                data="REQUEST:";
                buf=data.getBytes();
                packet = new DatagramPacket(buf, buf.length, address, 4445);
                System.out.println("Sending: " + new String(packet.getData(), 0, packet.getLength() ));
                socket.send(packet);
                timer.reset();
                nextState=State.WFR1;
                buf = new byte[256];
                break;
                
            case WFR1:
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);                
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Recieved: " + received);
                
                if (received.startsWith("pkt_amount:")) {
                    int pkt_amount=Integer.parseInt(received.substring(11));
                    System.out.println("Amount of packets(int): " + pkt_amount);
                    
                    data="ACK";
                    buf=data.getBytes();
                    packet = new DatagramPacket(buf, buf.length, address, 4445);
                    System.out.println("Sending: " + new String(packet.getData(), 0, packet.getLength() ));
                    socket.send(packet);
                    
                    nextState=State.REC_STREAM; 
                    timer.reset();
                }
                
                
                break;
                
            case REC_STREAM:
                break;
                
            case IDLE:
                break;
        }
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