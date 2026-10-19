package dartboardextreme;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.File;
import javax.imageio.ImageIO;

/** Maps colours on a dartboard image to scoring regions. */
public class BoardMapper {
  /** Minimum red channel for a red bull pixel. */
  private static final int RED_BULL_MIN_RED = 120;

  /** Maximum green channel for a red bull pixel. */
  private static final int RED_BULL_MAX_GREEN = 80;

  /** Maximum blue channel for a red bull pixel. */
  private static final int RED_BULL_MAX_BLUE = 60;

  /** Maximum red channel for a green ring pixel. */
  private static final int GREEN_RING_MAX_RED = 140;

  /** Minimum green channel for a green ring pixel. */
  private static final int GREEN_RING_MIN_GREEN = 120;

  /** Maximum blue channel for a green ring pixel. */
  private static final int GREEN_RING_MAX_BLUE = 80;

  /** Divisor that turns the board width into the bull search band. */
  private static final int ASPECT_DIVISOR = 10;

  /** Pixels stepped inward or outward from a detected edge. */
  private static final int SIDE_OFFSET = 1;

  /** Fraction of the board radius used for the outer triple and double. */
  private static final double OUTER_FRACTION = 0.75d;

  /** Fraction of the board radius used for the outer bull. */
  private static final double OUTER_BULL_FRACTION = 0.25d;

  /** Extra pixels searched past the board when finding the double ring. */
  private static final int DOUBLE_MARGIN = 10;

  /** Divisor for the inner-triple search start. */
  private static final int TRIPLE_DIVISOR = 3;

  /** Scale applied to the bull radius when searching its inner edge. */
  private static final int BULL_RADIUS_SCALE = 3;

  /** Scale that turns a radius into a diameter. */
  private static final int DIAMETER_SCALE = 2;

  /** Extra bull radii included in the cropped board diameter. */
  private static final int BULL_DIAMETER_SCALE = 30;

  /** Source file, when the board was loaded from disk. */
  private File dartboardFile;

  /** Colour of each pixel. */
  private Color[][] dartboardMapColors;

  /** Region colour copied from the source. */
  private int[][] dartboardMapRegions;

  /** Image width in pixels. */
  private int dartboardWidth;

  /** Image height in pixels. */
  private int dartboardHeight;

  /** Loaded board image. */
  private BufferedImage dartboardImage;

  /** Cropped board image. */
  private BufferedImage dartBoard;

  /** Raster of the loaded image. */
  private Raster raster;

  /** Data buffer of the loaded image. */
  private DataBuffer dataBuffer;

  /** Bull centre. */
  private Point2D.Double center;

  /** Radius of the inner bull. */
  private int bullRadius;

  /** Radius of the whole board. */
  private int boardRadius;

  /** Inner radius of the double ring. */
  private int doubleInnerRadius;

  /** Inner radius of the triple ring. */
  private int innerTripleRadius;

  /** Outer radius of the triple ring. */
  private int outerTripleRadius;

  /** Inner radius of the outer bull. */
  private int outerBullInnerRadius;

  /** Outer radius of the outer bull. */
  private int outerBullOuterRadius;

  /** Creates a mapper with no image loaded. */
  public BoardMapper() {
    // no fields to initialize
  }

  /**
   * Loads a board from a file.
   *
   * @param board image file
   */
  public BoardMapper(final File board) {
    this.dartboardFile = board;
    try {
      this.dartboardImage = ImageIO.read(this.dartboardFile);
    } catch (final Exception ex) {
      System.out.println("Error buffering image file");
      return;
    }
    initFromImage();
  }

  /**
   * Uses an image that is already in memory.
   *
   * @param board board image
   */
  public BoardMapper(final BufferedImage board) {
    this.dartboardImage = board;
    initFromImage();
  }

  /** Allocates the colour and region maps from the current image. */
  private void initFromImage() {
    try {
      this.dataBuffer = this.dartboardImage.getRaster().getDataBuffer();
      this.dartboardHeight = this.dartboardImage.getHeight();
      this.dartboardWidth = this.dartboardImage.getWidth();
      final int width = this.dartboardWidth;
      final int height = this.dartboardHeight;
      this.dartboardMapColors = new Color[width][height];
      this.dartboardMapRegions = new int[width][height];
      this.raster = this.dartboardImage.getData();
    } catch (final Exception ex) {
      System.out.println("Error buffering image file");
    }
  }

  /**
   * Reports whether the colour is the red bull.
   *
   * @param color pixel colour
   * @return true for a red bull pixel
   */
  private static boolean isRedBull(final Color color) {
    return color.getRed() > RED_BULL_MIN_RED
        && color.getGreen() < RED_BULL_MAX_GREEN
        && color.getBlue() < RED_BULL_MAX_BLUE;
  }

  /**
   * Reports whether the colour is the green ring.
   *
   * @param color pixel colour
   * @return true for a green ring pixel
   */
  private static boolean isGreenRing(final Color color) {
    return color.getRed() < GREEN_RING_MAX_RED
        && color.getGreen() > GREEN_RING_MIN_GREEN
        && color.getBlue() < GREEN_RING_MAX_BLUE;
  }

  /**
   * Returns the last red-bull hit on the first outer index that has one.
   *
   * <p>Ascending flags match the original loop directions, including ranges
   *
   * <p>that do not run. The point is {@code (0, 0)} when nothing matches.
   *
   * @param outerStart first outer index
   * @param outerEnd exclusive outer limit
   * @param outerAscending true when the outer index increases
   * @param innerStart first inner index
   * @param innerEnd exclusive inner limit
   * @param innerAscending true when the inner index increases
   * @param outerIsX true when the outer index is x
   * @return the matching point, or the origin
   */
  private Point2D.Double scanRedBull(
      final int outerStart,
      final int outerEnd,
      final boolean outerAscending,
      final int innerStart,
      final int innerEnd,
      final boolean innerAscending,
      final boolean outerIsX) {
    final int outerStep = outerAscending ? 1 : -1;
    final int innerStep = innerAscending ? 1 : -1;
    Point2D.Double found = null;
    for (int outer = outerStart;
        outerAscending ? outer < outerEnd : outer > outerEnd;
        outer += outerStep) {
      if (found != null) {
        break;
      }
      for (int inner = innerStart;
          innerAscending ? inner < innerEnd : inner > innerEnd;
          inner += innerStep) {
        final int xcoord = outerIsX ? outer : inner;
        final int ycoord = outerIsX ? inner : outer;
        if (isRedBull(this.dartboardMapColors[xcoord][ycoord])) {
          found = new Point2D.Double(xcoord, ycoord);
        }
      }
    }
    if (found == null) {
      return new Point2D.Double(0, 0);
    }
    return found;
  }

  /**
   * Returns the first green-ring x on a row.
   *
   * @param a first x
   * @param b exclusive x limit
   * @param row image row
   * @param up true when x increases
   * @return the x coordinate, or 0 when none is found
   */
  private int ring(final int a, final int b, final int row, final boolean up) {
    final int step = up ? 1 : -1;
    for (int i = a; up ? i < b : i > b; i += step) {
      if (isGreenRing(this.dartboardMapColors[i][row])) {
        return i;
      }
    }
    return 0;
  }

  /** Stores the colour of every pixel. */
  public void mapDartboardColors() {
    for (int i = 0; i < this.dartboardHeight; i++) {
      for (int j = 0; j < this.dartboardWidth; j++) {
        final int rgb = this.dartboardImage.getRGB(j, i);
        this.dartboardMapColors[j][i] = new Color(rgb);
      }
    }
  }

  /** Finds the bull, the board edge, and the ring radii. */
  public void findOutline() {
    boolean leftMost = false;
    int leftSide = 0;
    int rightSide = 0;
    int ylevel = 0;

    for (int i = 0; i < this.dartboardWidth; i++) {
      for (int j = 0; j < this.dartboardHeight; j++) {
        final Color color = this.dartboardMapColors[i][j];
        if (isGreenRing(color)) {
          if (!leftMost) {
            ylevel = j;
            leftSide = i - SIDE_OFFSET;
            leftMost = true;
          }
          rightSide = i + SIDE_OFFSET;
        }
      }
    }

    final int middle = (rightSide + leftSide) / 2;
    final int bullAspectRatio = (rightSide - leftSide) / ASPECT_DIVISOR;

    System.out.println("looking for bull");
    System.out.println("");
    System.out.println("looking in sector");
    final int leftLimit = middle - bullAspectRatio;
    final int rightLimit = middle + bullAspectRatio;
    System.out.println("Left - > right " + leftLimit + " to " + rightLimit);
    final int topLimit = ylevel - bullAspectRatio;
    final int bottomLimit = ylevel + bullAspectRatio;
    System.out.println("Top - > bottom " + topLimit + " to " + bottomLimit);

    final int band = bullAspectRatio;
    final int lowX = middle - band;
    final int highX = middle + band;
    final int lowY = ylevel - band;
    final int highY = ylevel + band;
    final Point2D.Double bullLeftPoint;
    bullLeftPoint = scanRedBull(lowX, highX, true, lowY, highY, true, true);
    final Point2D.Double bullRightPoint;
    bullRightPoint = scanRedBull(highX, lowX, false, lowY, highY, true, true);
    final Point2D.Double bullTopPoint;
    bullTopPoint = scanRedBull(lowY, highY, true, lowX, highX, true, false);
    final Point2D.Double bottom;
    bottom = scanRedBull(highY, lowY, false, highX, lowX, false, false);

    final int centerX = (int) (bullLeftPoint.x + bullRightPoint.x) / 2;
    final int centerY = (int) (bullTopPoint.y + bottom.y) / 2;
    this.center = new Point2D.Double(centerX, centerY);
    this.boardRadius = (int) ((leftSide + rightSide) / 2) - leftSide;
    this.bullRadius = (int) (bullRightPoint.x - bullLeftPoint.x) / 2;

    final String topText = "Top Bull " + bullTopPoint;
    System.out.println(topText + " Bottom " + bottom);
    final String leftText = "Left Bull " + bullLeftPoint;
    System.out.println(leftText + " Right " + bullRightPoint);
    System.out.println("Center " + center);
    System.out.println("Board Radius " + this.boardRadius);
    System.out.println("Left " + leftSide + " Right " + rightSide);
    findRadii((int) center.x, (int) center.y);
  }

  /** Copies green and red pixels into the region map. */
  public void mapDartboardRegions() {
    int y = 0;
    findOutline();
    for (int i = 0; i < this.dartboardHeight; i++) {
      for (int j = 0; j < this.dartboardWidth; j++) {
        final Color color = this.dartboardMapColors[j][i];
        if (isGreenRing(color)) {
          y++;
          final int rgb = this.dartboardMapColors[j][i].getRGB();
          this.dartboardMapRegions[j][i] = rgb;
        }
        if (isRedBull(color)) {
          y++;
          final int rgb = this.dartboardMapColors[j][i].getRGB();
          this.dartboardMapRegions[j][i] = rgb;
        }
      }
    }
  }

  /**
   * Measures the triple, double, and outer-bull radii from the centre.
   *
   * @param x centre x
   * @param y centre y
   */
  public void findRadii(final int x, final int y) {
    final int tripleStart = (int) (x + (this.boardRadius / TRIPLE_DIVISOR));
    final int width = this.dartboardWidth;
    final int foundInnerTriple = ring(tripleStart, width, y, true);
    final int outerStart = (int) (x + (this.boardRadius * OUTER_FRACTION));
    final int foundOuterTriple = ring(outerStart, x, y, false);
    final int doubleEnd = x + this.boardRadius + DOUBLE_MARGIN;
    final int foundDouble = ring(outerStart, doubleEnd, y, true);
    final double bullFraction = this.boardRadius * OUTER_BULL_FRACTION;
    final int bullOuterStart = (int) (x + bullFraction);
    final int foundOuterBull = ring(bullOuterStart, x, y, false);
    final int bullInnerEnd = (int) (x + (this.bullRadius * BULL_RADIUS_SCALE));
    final int foundInnerBull = ring(x, bullInnerEnd, y, true);

    final double centerX = this.center.x;
    System.out.println("Inner triple radius : " + (foundInnerTriple - centerX));
    System.out.println("outer triple radius : " + (foundOuterTriple - centerX));
    System.out.println("Double inner radius : " + (foundDouble - centerX));
    final double innerGap = foundInnerBull - centerX;
    final double outerGap = foundOuterBull - centerX;
    System.out.println("Inner outerbull radius : " + innerGap);
    System.out.println("Outer outerbull radius : " + outerGap);

    this.doubleInnerRadius = (int) (foundDouble - centerX);
    this.innerTripleRadius = (int) (foundInnerTriple - centerX);
    this.outerTripleRadius = (int) (foundOuterTriple - centerX);
    this.outerBullInnerRadius = (int) (foundInnerBull - centerX);
    this.outerBullOuterRadius = (int) (foundOuterBull - centerX);

    this.exclusiveDartBoard();
  }

  /** Writes the region map to region.bmp. */
  public void exportRegion() {
    final int imageType = BufferedImage.TYPE_INT_RGB;
    final BufferedImage image =
        new BufferedImage(this.dartboardWidth, this.dartboardHeight, imageType);
    for (int i = 0; i < this.dartboardHeight; i++) {
      for (int j = 0; j < this.dartboardWidth; j++) {
        final Color color = new Color(this.dartboardMapRegions[j][i]);
        image.setRGB(j, i, color.getRGB());
      }
    }
    final String imageDifferenceLocation = "region.bmp";
    final File imageDifferenceFile = new File(imageDifferenceLocation);
    try {
      ImageIO.write(image, "bmp", imageDifferenceFile);
      System.out.println("written");
    } catch (final Exception ex) {
      System.out.println("failed");
    }
  }

  /** Crops a square around the detected board. */
  public void exclusiveDartBoard() {
    final int boardSpan = this.boardRadius * DIAMETER_SCALE;
    final int bullSpan = this.bullRadius * BULL_DIAMETER_SCALE;
    final int diameter = (int) (boardSpan + bullSpan);
    final int rgbType = BufferedImage.TYPE_INT_RGB;
    this.dartBoard = new BufferedImage(diameter, diameter, rgbType);
    int x = 0;

    System.out.println("diameter = " + diameter);
    final double leftEdge = this.center.x - (diameter / 2);
    final double rightEdge = this.center.x + (diameter / 2);
    System.out.println("diameter = " + (leftEdge - rightEdge));

    final int xStart = (int) (this.center.x - (diameter / 2));
    final int xEnd = (int) (this.center.x + (diameter / 2));
    for (int i = xStart; i < xEnd; i++) {
      int y = 0;
      final int yStart = (int) (this.center.y - (diameter / 2));
      final int yEnd = (int) (this.center.y + (diameter / 2));
      for (int j = yStart; j < yEnd; j++) {
        final int color = this.dartboardImage.getRGB(i, j);
        this.dartBoard.setRGB(x, y, color);
        y++;
      }
      x++;
    }
  }

  /**
   * Returns the bull centre.
   *
   * @return the centre
   */
  public Point2D.Double getCenter() {
    return this.center;
  }

  /**
   * Returns the inner bull radius.
   *
   * @return the radius
   */
  public int getBullRadius() {
    return this.bullRadius;
  }

  /**
   * Returns the board radius.
   *
   * @return the radius
   */
  public int getBoardRadius() {
    return this.boardRadius;
  }

  /**
   * Returns the inner double radius.
   *
   * @return the radius
   */
  public int getDoubleInnerRadius() {
    return this.doubleInnerRadius;
  }

  /**
   * Returns the outer triple radius.
   *
   * @return the radius
   */
  public int getTripleOuterRadius() {
    return this.outerTripleRadius;
  }

  /**
   * Returns the inner triple radius.
   *
   * @return the radius
   */
  public int getTripleInnerRadius() {
    return this.innerTripleRadius;
  }

  /**
   * Returns the outer radius of the outer bull.
   *
   * @return the radius
   */
  public int getOuterBullOuterRadius() {
    return this.outerBullOuterRadius;
  }

  /**
   * Returns the inner radius of the outer bull.
   *
   * @return the radius
   */
  public int getOuterBullInnerRadius() {
    return this.outerBullInnerRadius;
  }

  /**
   * Returns the cropped board image.
   *
   * @return the cropped image
   */
  public BufferedImage getDartBoard() {
    return this.dartBoard;
  }
}
