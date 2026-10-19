package dartboardextreme;

import dartboardextreme.edgeDetection.HoughLine;
import dartboardextreme.edgeDetection.SobelEdgeDetector;
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

/** Window that draws the detected board and a sample angle. */
public class CalibratorUI extends JFrame {
  /** Stable serialization id from the original class. */
  private static final long serialVersionUID = -408995032593017165L;

  /** Opaque white. */
  private static final int OPAQUE_WHITE = 255;

  /** Pixels of bull radius used when choosing the angle sample. */
  private static final int SAMPLE_BULL_SCALE = 5;

  /** Vertical nudge applied to the sample point. */
  private static final int SAMPLE_Y_NUDGE = 2;

  /** Hough angle passed to the sample line. */
  private static final int SAMPLE_ANGLE = 45;

  /** Hough radius passed to the sample line. */
  private static final int SAMPLE_RADIUS = 500;

  /** Source image. */
  private BufferedImage fractal;

  /** Edge image drawn over the source. */
  private BufferedImage outline;

  /** Bull centre. */
  private Point2D.Double center;

  /** Inner bull radius. */
  private int bullradius;

  /** Board radius. */
  private int boardradius;

  /** Double-ring radius. */
  private int doubleradius;

  /** Outer triple radius. */
  private int outertripleradius;

  /** Inner triple radius. */
  private int innertripleradius;

  /** Outer radius of the outer bull. */
  private int outerbullouterradius;

  /** Inner radius of the outer bull. */
  private int outerbullinnerradius;

  /** Content panel. */
  private JPanel panel;

  /** Content pane. */
  private Container container;

  /** Unused button kept from the original layout. */
  private JButton updateButton;

  /** Board radii shown by the calibrator. */
  public static final class BoardGeometry {
    /** Inner bull radius. */
    private final int bullRadius;

    /** Board radius. */
    private final int boardRadius;

    /** Double-ring radius. */
    private final int doubleRadius;

    /** Outer triple radius. */
    private final int tripleOuter;

    /** Inner triple radius. */
    private final int tripleInner;

    /** Outer radius of the outer bull. */
    private final int outerBull;

    /** Inner radius of the outer bull. */
    private final int innerBull;

    /**
     * Stores the seven radii.
     *
     * @param bull inner bull radius
     * @param board board radius
     * @param dbl double-ring radius
     * @param outerTriple outer triple radius
     * @param innerTriple inner triple radius
     * @param outer outer-bull outer radius
     * @param inner outer-bull inner radius
     */
    public BoardGeometry(
        final int bull,
        final int board,
        final int dbl,
        final int outerTriple,
        final int innerTriple,
        final int outer,
        final int inner) {
      this.bullRadius = bull;
      this.boardRadius = board;
      this.doubleRadius = dbl;
      this.tripleOuter = outerTriple;
      this.tripleInner = innerTriple;
      this.outerBull = outer;
      this.innerBull = inner;
    }
  }

  /**
   * Opens the calibrator for one image and one set of radii.
   *
   * @param sourceImage source image
   * @param boardCenter bull centre
   * @param geometry radii measured on the board
   */
  public CalibratorUI(
      final BufferedImage sourceImage,
      final Point2D.Double boardCenter,
      final BoardGeometry geometry) {
    this.fractal = sourceImage;
    this.center = boardCenter;
    this.bullradius = geometry.bullRadius;
    this.boardradius = geometry.boardRadius;
    this.doubleradius = geometry.doubleRadius;
    this.outertripleradius = geometry.tripleOuter;
    this.innertripleradius = geometry.tripleInner;
    this.outerbullouterradius = geometry.outerBull;
    this.outerbullinnerradius = geometry.innerBull;

    this.container = getContentPane();
    this.panel = new JPanel();
    this.panel.setLocation(0, 0);
    this.panel.setSize(sourceImage.getWidth(), sourceImage.getHeight());
    this.container.add(this.panel);

    final MediaTracker mediaTracker = new MediaTracker(this.panel);
    mediaTracker.addImage(this.fractal, 0);
    try {
      mediaTracker.waitForID(0);
    } catch (final InterruptedException ex) {
      System.err.println(ex);
      System.exit(1);
    }
    addWindowListener(
        new WindowAdapter() {
          /**
           * Ends the program when the window closes.
           *
           * @param event the window event
           */
          @Override
          public void windowClosing(final WindowEvent event) {
            System.exit(0);
          }
        });
    setSize(this.fractal.getWidth(null), this.fractal.getHeight(null));
    this.setVisible(true);
  }

  /**
   * Paints the board, a sample angle, and the edge overlay.
   *
   * @param graphics destination
   */
  @Override
  public void paint(final Graphics graphics) {
    final Graphics2D g2d = (Graphics2D) graphics;
    graphics.drawImage(this.fractal, 0, 0, null);

    final SobelEdgeDetector sobel = new SobelEdgeDetector(this.fractal);
    final BufferedImage edges = sobel.edges();
    final int markX = (int) this.center.getX();
    final int markY = (int) this.center.getY();
    final int white = OPAQUE_WHITE;
    final Color mark = new Color(white, white, white, white);
    edges.setRGB(markX, markY, mark.getRGB());
    this.outline = edges;

    final ArrayList<Object> area = new ArrayList<Object>();
    area.size();
    final int sampleX = (int) this.center.x + this.outertripleradius;
    int x = sampleX + (SAMPLE_BULL_SCALE * this.bullradius);
    int y = (int) this.center.y + SAMPLE_Y_NUDGE;
    int pixel = this.outline.getRGB(x, y);
    while (pixel != Color.cyan.getRGB()) {
      pixel = this.outline.getRGB(x, y);
      y--;
    }
    y = y - 1;

    final double opposite = this.center.y + SAMPLE_Y_NUDGE - y;
    final double adjacent = x - this.center.x;
    final double tan = opposite / adjacent;
    final double angle = Math.atan(tan);
    final String point = x + "," + y;
    final String header = " Center : " + this.center;
    System.out.println(header + " point on angle :" + point);
    System.out.println(" opposite : " + opposite);
    System.out.println(" angle : " + Math.ceil(Math.toDegrees(angle)));

    g2d.setColor(Color.yellow);
    final int baseY = (int) this.center.y + SAMPLE_Y_NUDGE;
    g2d.drawLine(x, baseY, (int) x, (int) y);
    g2d.drawLine((int) this.center.x, baseY, (int) x, baseY);
    g2d.setColor(Color.yellow);
    g2d.drawLine((int) this.center.x, baseY, (int) x, (int) y);

    final HoughLine sample = new HoughLine(SAMPLE_ANGLE, SAMPLE_RADIUS);
    this.outline = sample.draw(this.outline, Color.magenta.getRGB());
    graphics.drawImage(this.outline, 0, 0, null);
  }

  /**
   * Copies an image into a width-by-height array.
   *
   * @param image source image
   * @return packed pixels
   */
  public int[][] createPixelArray(final BufferedImage image) {
    final int[][] newImage = new int[image.getWidth()][image.getHeight()];
    for (int m = 0; m < image.getWidth(); m++) {
      for (int c = 0; c < image.getHeight(); c++) {
        newImage[m][c] = image.getRGB(m, c);
      }
    }
    return newImage;
  }
}
