package ws.dtu;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;

class MyPanel extends JPanel implements Runnable{

    public static int blax=0;
    public static int blay=0;
    public static boolean paint=false;
    
    public static int x_size=0;
    public static int y_size=0;
    
    public static int _pkt_amount;
    
    
    
    
    public MyPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250,200);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);       

        // Draw 
        
            y_size = (int)Math.ceil((double)_pkt_amount/60.0);
            
        //System.out.println("-----------------------------------------------"+y_size);
        for(int y = 0; y < y_size*8; y+=8) {
            for(int x = 0; x < 60*8; x+=8) {
                System.out.println("y/8= " + y/8.0 + " x/8= " + x/8.0 + "---------------------------");
                if (y/8.0 * x/8.0 < _pkt_amount) {
                    g.setColor(Color.red);               
                    g.fillRect(x, y, 5, 5);  
                }
               
                              
            }
            
        }
        
//                Graphics2D g2d = (Graphics2D) g;
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//				RenderingHints.VALUE_ANTIALIAS_ON);
//		g2d.fillOval(blax, blay, 30, 30);
//        
    }
    
//    @Override
//    public void update(Graphics g){ 
//        
//       super.update(g);
//        
//        for(int y = 0; y < 100; y+=8) {
//            for(int x = 0; x < 100; x+=8) {
//                g.setColor(Color.green);               
//                g.fillRect(x, y, 5, 5);
//                 for (int i = 0; i < 100; i++) {
//                    // System.out.println("x= "+x + ",y= " + y);
//                }
//               
//            }
//            
//        }
//    }
    

    
    public static void updatePackets(Map<Integer,String> map){
        paint=true;
        
        for(Map.Entry<Integer,String> entry : map.entrySet()) {
            
        }
       
    }
    
    public static void move(){
     
        blax+=1;
        blay+=1;
    }
    public static void set_size(int pkt_amount){
        _pkt_amount=pkt_amount;
    }
  
    
//    public static void main(String[] args) throws InterruptedException {
//        MyPanel panel = new MyPanel();
//        while (true) {   
//            panel.move();
//            panel.repaint();
//                
//        }
//    }

    @Override
    public void run() {
                  
        while (true) {            
            
        
        //move();
            if (paint) {
                repaint();
                paint=false;  
            }
        
        try {  
            Thread.sleep(50);  
        } catch (Exception ex) {}
        }
    
        
    }

}