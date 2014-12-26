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

class MyPanel extends JPanel{

    public static int blax=10;
    public static int blay=0;
    
    
    
    
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
//        for(int y = 0; y < 100; y+=8) {
//            for(int x = 0; x < 100; x+=8) {
//                g.setColor(Color.red);               
//                g.fillRect(x, y, 5, 5);
//                 for (int i = 0; i < 100; i++) {
//                    // System.out.println("x= "+x + ",y= " + y);
//                }
//               
//            }
//            
//        }
        
                Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.fillOval(blax, blay, 30, 30);
        
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
    

    
    public static void updatePackets(Graphics g){
        g.drawString("blaaaaaaaa", 100, 20);
        
        //update();
    }
    
    public void move(){
        blax+=10;
        blay+=10;
    }
    
//    public static void main(String[] args) throws InterruptedException {
//        MyPanel panel = new MyPanel();
//        while (true) {   
//            panel.move();
//            panel.repaint();
//                
//        }
//    }
  
}