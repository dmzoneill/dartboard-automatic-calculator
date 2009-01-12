package dartBoardExtreme;



import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class BufferedImageMouseDrag extends JFrame {
  DisplayCanvas canvas;

  public BufferedImageMouseDrag() {
    super();
    Container container = getContentPane();

    canvas = new DisplayCanvas();
    container.add(canvas);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    setSize(1000, 800);
    setVisible(true);
  }

  public static void main(String arg[]) {
    new BufferedImageMouseDrag();
  }
}

class DisplayCanvas extends JPanel {
  int x, y;
  int width,height;

  BufferedImage bi;

  DisplayCanvas() {
	  

	    Image image = getToolkit().getImage("DSCN0504.JPG");

	    MediaTracker mt = new MediaTracker(this);
	    mt.addImage(image, 1);
	    try {
	      mt.waitForAll();
	    } catch (Exception e) {
	      System.out.println("Exception while loading image.");
	    }
	    
	    
	    if (image.getWidth(this) == -1) {
	      System.out.println("no gif file");
	      System.exit(0);
	    }

	    width = image.getWidth(this);
	    height = image.getHeight(this);  
	  
    setBackground(Color.white);
    setSize(width, height);
    addMouseMotionListener(new MouseMotionHandler());

    
    bi = new BufferedImage(image.getWidth(this), image.getHeight(this),
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D big = bi.createGraphics();
    big.drawImage(image, 0, 0, this);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D) g;
   
    g2D.drawImage(bi, x + width, y + height, this);
  }

  class MouseMotionHandler extends MouseMotionAdapter {
    public void mouseDragged(MouseEvent e) {
      x = e.getX() - width;
      y = e.getY() - height;
      repaint();
    }
    public void mouseClick (MouseEvent e)
    {
    	
    }
  }
}