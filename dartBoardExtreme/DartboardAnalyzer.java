package dartBoardExtreme;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DartboardAnalyzer 
{	
	public static void main(String[] args) throws IOException
	{				  

		File imageCalibrationFile;
		String imagecalibrationLocation;
	
		imagecalibrationLocation = "calibrated.bmp";	
		imageCalibrationFile = new File(imagecalibrationLocation);
    
		BoardMapper mapper = new BoardMapper(imageCalibrationFile);
		mapper.MapDartboardColors();
		mapper.MapDartboardRegions();
		mapper.exportRegion();
    
		BufferedImage dick = mapper.getDartBoard();
		
		
		BoardMapper mapper2 = new BoardMapper(dick);
		mapper2.MapDartboardColors();
		mapper2.MapDartboardRegions();
		mapper2.exportRegion();
		
		BufferedImage dick2 = mapper.getDartBoard();
		
		//BufferedImage dick = calibrationImage;
		
		CalibratorUI fail = new CalibratorUI(dick2,mapper2.getCenter(),mapper2.getBullRadius(),mapper2.getBoardRadius(),mapper2.getDoubleInnerRadius(),mapper2.getTripleOuterRadius(),mapper2.getTripleInnerRadius(),mapper2.getOuterBullOuterRadius(),mapper2.getOuterBullInnerRadius());
	}
}


