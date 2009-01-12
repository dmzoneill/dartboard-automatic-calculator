package dartBoardExtreme;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import dartBoardExtreme.edgeDetection.HoughLine;
import dartBoardExtreme.edgeDetection.SobelEdgeDetector;


public class CalibratorUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -408995032593017165L;
	protected BufferedImage fractal;
	protected BufferedImage outline;
	private Point2D.Double center;
	private int bullradius;
	private int boardradius;
	private int doubleradius;
	private int outertripleradius;
	private int innertripleradius;
	private int outerbullouterradius;
	private int outerbullinnerradius;
	
	protected JPanel panel;
	protected Container container;
	private JButton updateButton;
	
	public CalibratorUI(BufferedImage image,Point2D.Double center, int bullRadius,int boardRadius, int doubleRadius, int tripleouter, int tripleinner, int outerbull, int innerbull) {
		this.fractal = image;
		this.center = center;
		this.bullradius = bullRadius;
		this.boardradius = boardRadius;
		this.doubleradius = doubleRadius;
		this.outertripleradius = tripleouter;
		this.innertripleradius = tripleinner;
		this.outerbullouterradius = outerbull;
		this.outerbullinnerradius = innerbull;		

		this.container = getContentPane();
		
		this.panel = new JPanel();
		this.panel.setLocation(0, 0);
		this.panel.setSize(image.getWidth(),image.getHeight());
		this.container.add(this.panel);
		

		MediaTracker mediaTracker = new MediaTracker(this.panel);
		mediaTracker.addImage(this.fractal, 0);
	
		try
		{
			mediaTracker.waitForID(0);
		}
		catch (InterruptedException ie)
		{
			System.err.println(ie);
			System.exit(1);
		}
		addWindowListener(new WindowAdapter() {
      		public void windowClosing(WindowEvent e) {
        		System.exit(0);
      		}
		});
		setSize(this.fractal.getWidth(null), this.fractal.getHeight(null));
		
		this.setVisible(true);
	}
	
	
	public void paint(Graphics graphics) {

		Graphics2D g2D = (Graphics2D) graphics;
		graphics.drawImage(this.fractal, 0, 0, null);	 
		
		SobelEdgeDetector sobel = new SobelEdgeDetector(this.fractal);
	    BufferedImage edges = sobel.edges();
	    

		for (int m = (int)this.center.getX(); m < (int)this.center.getX()+1; m++) 
		{    	
	  		for (int c = (int)this.center.getY(); c < (int)this.center.getY()+1; c++) 
	  		 {
	  			edges.setRGB(m, c, new Color(255,255,255,255).getRGB());
	  		 }
		}	     
	    
		this.outline = edges;	
		
		
		
		ArrayList area  = new ArrayList();
		
		int x = (int)this.center.x + this.outertripleradius + (5 * this.bullradius);
		int y = (int) this.center.y +2;
		
		
		
		int v = this.outline.getRGB(x, y);
		
		while(v != Color.cyan.getRGB())
		{			
			v = this.outline.getRGB(x, y);
			y--;
		}
		
		y = y -1;
		
		double opposite = this.center.y +2 -y;
		double adjacent = x - this.center.x;
		double tan = opposite / adjacent;
		double angle = Math.atan(tan);
		
		System.out.println(" Center : " + this.center + " point on angle :" + x + "," + y);
		System.out.println(" opposit : " + opposite);
		System.out.println(" angle : " + Math.ceil(Math.toDegrees(angle)));
		
		g2D.setColor(Color.yellow);
		g2D.drawLine(x, (int)this.center.y +2, (int)x, (int)y);
		g2D.drawLine((int)this.center.x, (int)this.center.y +2, (int)x, (int) this.center.y +2);
		
		g2D.setColor(Color.yellow);
		g2D.drawLine((int)this.center.x, (int)this.center.y +2, (int)x, (int)y);
		
		HoughLine fail = new HoughLine(45,500);
		this.outline = fail.draw(this.outline, Color.magenta.getRGB());
		
		graphics.drawImage(this.outline, 0, 0, null);			
	    	    
	}
	
	public int[][] createPixelArray(BufferedImage image)
	{
		int[][] newImage = new int[image.getWidth()][image.getHeight()];
		
		for (int m = 0; m < image.getWidth(); m++) 
		{    	
	  		for (int c = 0; c < image.getHeight(); c++) 
	  		 {
	  			 newImage[m][c]= image.getRGB(m, c);
	  		 }
		}	 
		
		return newImage;
	}
	
	
	
}



