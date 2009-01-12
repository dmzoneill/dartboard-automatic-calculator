package dartBoardExtreme;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.File;

import javax.imageio.ImageIO;

public class BoardMapper 
{
	private File dartboardFile;
	private Color dartboardMapColors[][];
	private int dartboardMapRegions[][];
	private int dartboardWidth;
	private int dartboardHeight;
	protected BufferedImage dartboardImage;
	private BufferedImage dartBoard;
	protected Raster raster;
	
	protected DataBuffer dataBuffer;
	
	private Point2D.Double center;
	private int bullRadius;
	private int boardRadius;
	private int doubleInnerRadius;
	private int innerTripleRadius;
	private int outerTripleRadius;
	private int outerBullInnerRadius;
	private int outerBullOuterRadius;
	
	
	
	public BoardMapper() 
	{
		// TODO Auto-generated constructor stub
	}
	
	
	public BoardMapper(File board) 
	{
		// TODO Auto-generated constructor stub
		this.dartboardFile = board;
		try 
		{			
			this.dartboardImage = ImageIO.read(this.dartboardFile);
			this.dataBuffer = this.dartboardImage.getRaster().getDataBuffer();
			this.dartboardHeight = this.dartboardImage.getHeight();
			this.dartboardWidth = this.dartboardImage.getWidth();
			this.dartboardMapColors = new Color[this.dartboardWidth][this.dartboardHeight];
			this.dartboardMapRegions = new int[this.dartboardWidth][this.dartboardHeight];
			this.raster = this.dartboardImage.getData();
		}
		catch(Exception e)
		{
			System.out.println("Error buffering image file");
		}
	}
	
	
	public BoardMapper(BufferedImage board) 
	{
		// TODO Auto-generated constructor stub
		this.dartboardImage = board;
		try 
		{			
			this.dataBuffer = this.dartboardImage.getRaster().getDataBuffer();
			this.dartboardHeight = this.dartboardImage.getHeight();
			this.dartboardWidth = this.dartboardImage.getWidth();
			this.dartboardMapColors = new Color[this.dartboardWidth][this.dartboardHeight];
			this.dartboardMapRegions = new int[this.dartboardWidth][this.dartboardHeight];
			this.raster = this.dartboardImage.getData();
		}
		catch(Exception e)
		{
			System.out.println("Error buffering image file");
		}
	}
	
	
	public void MapDartboardColors()
	{		
		for (int i = 0; i < this.dartboardHeight; i++) 
	    {    	
		   for (int j = 0; j < this.dartboardWidth; j++) 
		   {
			   this.dartboardMapColors[j][i] = new Color(this.dartboardImage.getRGB(j, i));
		   }
	    }		
	}		
	
	
	public void findOutline()
	{
		boolean leftMost = false;
		int leftSide = 0;
		int rightSide = 0;
		int i = 0;
		int j = 0;
		int ylevel = 0;
		
		for (i = 0; i < this.dartboardWidth; i++) 
	    {    	
		   for (j = 0; j < this.dartboardHeight; j++) 
		   {
			   Color color = this.dartboardMapColors[i][j];
			   // if black red or green
			   if(color.getRed()<140 && color.getGreen()>120 && color.getBlue()<80)
			   {
				   if(leftMost==false)
				   {
					   ylevel = j;
					   leftSide = i -1;
					   leftMost = true;
				   }				   
				   rightSide = i + 1;
			   }
		    }	
	    }
		
		
		int middle = (rightSide + leftSide) / 2;
		int bullAspectRatio = (rightSide - leftSide) / 10;
		
		System.out.println("looking for bull");
		System.out.println("");
		System.out.println("looking in sector");
		System.out.println("Left - > right " + (middle - bullAspectRatio) + " to " + (middle + bullAspectRatio));
		System.out.println("Top - > bottom " + (ylevel - bullAspectRatio) + " to " + (ylevel + bullAspectRatio));
		
		// bull left		
		Point2D.Double bullLeftPoint = new Point2D.Double(0,0);
		boolean bullLeftPointBool = false;
		
		for (i = (middle - bullAspectRatio); i < (middle + bullAspectRatio); i++) 
	    {    	
			if(bullLeftPointBool==true)
			{
				break;				
			}
			for (j = (ylevel - bullAspectRatio); j < (ylevel + bullAspectRatio); j++) 
			{
				Color color = this.dartboardMapColors[i][j];
				// if black red or green
				if(color.getRed()>120 && color.getGreen()<80 && color.getBlue()<60)
				{
					bullLeftPoint = new Point2D.Double(i,j);
					bullLeftPointBool = true;
				}
			}	
	    }
		
		// bull right
		Point2D.Double bullRightPoint = new Point2D.Double(0,0);
		boolean bullRightPointBool = false;
		
		for (i = (middle + bullAspectRatio); i > (middle - bullAspectRatio); i--) 
	    {    	
			if(bullRightPointBool==true)
			{
				break;				
			}
			for (j = (ylevel - bullAspectRatio); j < (ylevel + bullAspectRatio); j++) 
			{
				Color color = this.dartboardMapColors[i][j];
				// if black red or green
				if(color.getRed()>120 && color.getGreen()<80 && color.getBlue()<60)
				{
					bullRightPoint = new Point2D.Double(i,j);
					bullRightPointBool = true;
				}
		    }	
	    }
		
		
		// bull top	
		Point2D.Double bullTopPoint = new Point2D.Double(0,0);
		boolean bullTopPointBool = false;
		
		for (j = (ylevel - bullAspectRatio); j < (ylevel + bullAspectRatio); j++) 
	    {    	
			if(bullTopPointBool==true)
			{
				break;				
			}
			for (i = (middle - bullAspectRatio); i < (middle + bullAspectRatio); i++) 
			{
				Color color = this.dartboardMapColors[i][j];
				// if black red or green
				if(color.getRed()>120 && color.getGreen()<80 && color.getBlue()<60)
				{
					bullTopPoint = new Point2D.Double(i,j);
					bullTopPointBool = true;
				}
			}	
	    }
		
		// bull bottom
		Point2D.Double bullBottomPoint = new Point2D.Double(0,0);
		boolean bullBottomPointBool = false;
		
		for (j = (ylevel + bullAspectRatio); j > (ylevel - bullAspectRatio); j--) 
	    {    	
			if(bullBottomPointBool==true)
			{
				break;				
			}
			for (i = (middle + bullAspectRatio); i > (middle - bullAspectRatio); i--) 
			{
				Color color = this.dartboardMapColors[i][j];
				// if black red or green
				if(color.getRed()>120 && color.getGreen()<80 && color.getBlue()<60)
				{
					bullBottomPoint = new Point2D.Double(i,j);
					bullBottomPointBool = true;
				}
		    }	
	    }
		
		this.center = new Point2D.Double((int)(bullLeftPoint.x + bullRightPoint.x) / 2,(int)(bullTopPoint.y + bullBottomPoint.y) / 2);
		this.boardRadius = (int)((leftSide + rightSide) / 2) - leftSide;
		this.bullRadius = (int)(bullRightPoint.x - bullLeftPoint.x) / 2;
		
		
		System.out.println("Top Bull " + bullTopPoint + " Bottom "+ bullBottomPoint);
		System.out.println("Left Bull " + bullLeftPoint + " Right "+ bullRightPoint);
		System.out.println("Center " + center);
		System.out.println("Board Radius " + this.boardRadius);		
		System.out.println("Left " + leftSide + " Right "+ rightSide);
		findRadii((int)center.x,(int)center.y);
	}
	
	
	public void MapDartboardRegions()
	{
		int y = 0;
		int i = 0;
		int j = 0;
		
		findOutline();
		
		for (i = 0; i < this.dartboardHeight; i++) 
	    {    	
		   for (j = 0; j < this.dartboardWidth; j++) 
		   {
			   Color color = this.dartboardMapColors[j][i];
			   // if black red or green
			   if(color.getRed()<140 && color.getGreen()>120 && color.getBlue()<80)
			   {
				   y++;
				   this.dartboardMapRegions[j][i] = this.dartboardMapColors[j][i].getRGB();
				   
			   }
			   if(color.getRed()>120 && color.getGreen()<80 && color.getBlue()<60)
			   {
				   y++;
				   this.dartboardMapRegions[j][i] = this.dartboardMapColors[j][i].getRGB();
			   }
		   }
	    }	
	}	
		
	
	public void findRadii(int x,int y)
	{
		int i = 0;
		int tripleInnerRadius = 0;
		int tripleOuterRadius = 0;
		int doubleInnerRadius = 0;
		int outerBullOuterRadius = 0;
		int outerBullInnerRadius = 0;
		
		
		// inner triple radius
		for (i = (int)(x + (this.boardRadius / 3)); i < this.dartboardWidth; i++) 
	    {    	
			Color color = this.dartboardMapColors[i][y];
			// if black red or green
			if(color.getRed()<140 && color.getGreen()>120 && color.getBlue()<80)
			{
				tripleInnerRadius = i;
				break;
			}
	    }		
		
		// outer triple radius
		for (i = (int)(x + (this.boardRadius * 0.75)); i > x; i--) 
		{    	
			Color color = this.dartboardMapColors[i][y];
			// if black red or green
			if(color.getRed()<140 && color.getGreen()>120 && color.getBlue()<80)
			{
				tripleOuterRadius = i;
				break;
			}
		}	
		
		// inner double radius		
		for (i = (int)(x + (this.boardRadius * 0.75)); i < (x + this.boardRadius + 10); i++) 
		{    	
			Color color = this.dartboardMapColors[i][y];
			// if black red or green
			if(color.getRed()<140 && color.getGreen()>120 && color.getBlue()<80)
			{
				doubleInnerRadius = i;
				break;
			}
		}	
		
		// outer outerbull radius 		
		for (i = (int)(x + (this.boardRadius * 0.25)); i > x; i--) 
		{    	
			Color color = this.dartboardMapColors[i][y];
			// if black red or green
			if(color.getRed()<140 && color.getGreen()>120 && color.getBlue()<80)
			{
				outerBullOuterRadius = i;
				break;
			}
		}	
		
		// inner outerbull radius		
		for (i = x; i < (int)(x + (this.bullRadius *3)); i++) 
		{    	
			Color color = this.dartboardMapColors[i][y];
			// if black red or green
			if(color.getRed()<140 && color.getGreen()>120 && color.getBlue()<80)
			{
				outerBullInnerRadius = i;
				break;
			}
		}	
		
		System.out.println("Inner triple radius : " + (tripleInnerRadius - this.center.x));
		System.out.println("outer triple radius : " + (tripleOuterRadius - this.center.x));
		System.out.println("Double inner radius : " + (doubleInnerRadius - this.center.x));
		System.out.println("Inner outerbull radius : " + (outerBullInnerRadius - this.center.x));
		System.out.println("Outer outerbull radius : " + (outerBullOuterRadius - this.center.x));
		
		this.doubleInnerRadius = (int) (doubleInnerRadius - this.center.x);
		this.innerTripleRadius = (int) (tripleInnerRadius - this.center.x);
		this.outerTripleRadius = (int) (tripleOuterRadius - this.center.x);
		this.outerBullInnerRadius = (int) (outerBullInnerRadius - this.center.x);
		this.outerBullOuterRadius = (int) (outerBullOuterRadius - this.center.x);
		
		this.exclusiveDartBoard();
		
	}
	
	
	public void exportRegion()
	{
		BufferedImage Image = new BufferedImage(this.dartboardWidth,this.dartboardHeight,BufferedImage.TYPE_INT_RGB);
		
		for (int i = 0; i < this.dartboardHeight; i++) 
	    {    	
		   for (int j = 0; j < this.dartboardWidth; j++) 
		   {
			   Color color = new Color(this.dartboardMapRegions[j][i]);	
			   Image.setRGB(j, i, color.getRGB());
		   }
	    }	
		String imageDifferenceLocation = "region.bmp";	

	    File imageDifferenceFile = new File(imageDifferenceLocation);
	    try
	    {
	    	ImageIO.write(Image, "bmp", imageDifferenceFile);
	    	System.out.println("written");
	    }
	    catch(Exception e)
	    {
	    	System.out.println("failed");
	    }
	}
	
	
	public void exclusiveDartBoard()
	{
		int diameter = (int) ((this.boardRadius * 2)  + (this.bullRadius * 30));
		this.dartBoard = new BufferedImage(diameter,diameter,BufferedImage.TYPE_INT_RGB);
		int x = 0, y = 0;
		
		System.out.println("diameter = " + diameter);
		System.out.println("diameter = " + ((this.center.x - (diameter / 2)) - (this.center.x + (diameter / 2))));
		
			for (int i = (int)(this.center.x - (diameter / 2)); i < (int)(this.center.x + (diameter / 2)); i++) 
			{    	
				y = 0;
		  		for (int j = (int)(this.center.y - (diameter / 2)); j < (int)(this.center.y + (diameter / 2)); j++) 
		  		 {
		  			 int color = this.dartboardImage.getRGB(i, j);	
		  			 this.dartBoard.setRGB(x, y, color);
		  			 y++;
		  		 }
		  		 x++;
			}	
	}
	
	
	public Point2D.Double getCenter()
	{
		return this.center;
	}
	
	public int getBullRadius()
	{
		return this.bullRadius;
	}
	
	public int getBoardRadius()
	{
		return this.boardRadius;
	}
	
	public int getDoubleInnerRadius()
	{
		return this.doubleInnerRadius;
	}
		
	public int getTripleOuterRadius()
	{
		return this.outerTripleRadius;
	}
	
	public int getTripleInnerRadius()
	{
		return this.innerTripleRadius;
	}
	
	public int getOuterBullOuterRadius()
	{
		return this.outerBullOuterRadius;
	}
	
	public int getOuterBullInnerRadius()
	{
		return this.outerBullInnerRadius;
	}
	
	public BufferedImage getDartBoard()
	{
		return this.dartBoard;
	}
	
}
