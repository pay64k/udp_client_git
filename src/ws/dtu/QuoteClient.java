package ws.dtu;

import java.io.*;
import java.net.*;
import java.util.*;

public class QuoteClient {
    public static void main(String[] args) throws IOException {


        DatagramSocket socket = new DatagramSocket();
        //setup timer timeout in milliseconds:
        Timer timer = new Timer(5000);
        //start timer:
        timer.start();
                        
        
        // send request
        String test="TEST11";
        byte[] buf_transmit = new byte[256];
        byte[] buf_recieve = new byte[256];
        buf_transmit=test.getBytes();
       
        
        InetAddress address = InetAddress.getByName("127.0.0.1");
        DatagramPacket packet = new DatagramPacket(buf_transmit, buf_transmit.length, address, 4445);
        for(int i=0; i<1;i++)
        {
        socket.send(packet);
        System.out.println("Sending: " + new String(packet.getData(), 0, packet.getLength() ));
        
            // get response
        packet = new DatagramPacket(buf_recieve, buf_recieve.length);
        socket.receive(packet);
        System.out.println("Current timeout: " + timer.getTimer());
        timer.reset();
        
        System.out.println("Current timeout: " + timer.getTimer());
        
            

	    // display response
        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Recieved: " + received);
        }
        
        socket.close();

    }
}