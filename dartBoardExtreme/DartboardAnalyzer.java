package dartBoardExtreme;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** Loads the sample board and opens the calibrator. */
public final class DartboardAnalyzer {
  /** Sample image name. */
  private static final String CALIBRATED_IMAGE = "calibrated.bmp";

  /** Prevents instantiation. */
  private DartboardAnalyzer() {
    // utility
  }

  /**
   * Maps the calibrated board and shows the calibrator.
   *
   * @param args unused
   * @throws IOException when image setup fails
   */
  public static void main(final String[] args) throws IOException {
    final File imageCalibrationFile = new File(CALIBRATED_IMAGE);
    final BoardMapper mapper = new BoardMapper(imageCalibrationFile);
    mapper.mapDartboardColors();
    mapper.mapDartboardRegions();
    mapper.exportRegion();

    final BufferedImage dick = mapper.getDartBoard();
    final BoardMapper mapper2 = new BoardMapper(dick);
    mapper2.mapDartboardColors();
    mapper2.mapDartboardRegions();
    mapper2.exportRegion();

    final BufferedImage dick2 = mapper.getDartBoard();
    new CalibratorUI(
        dick2,
        mapper2.getCenter(),
        mapper2.getBullRadius(),
        mapper2.getBoardRadius(),
        mapper2.getDoubleInnerRadius(),
        mapper2.getTripleOuterRadius(),
        mapper2.getTripleInnerRadius(),
        mapper2.getOuterBullOuterRadius(),
        mapper2.getOuterBullInnerRadius());
  }
}
