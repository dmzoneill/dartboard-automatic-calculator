package old;

/** Edge detection over a metaball grid. */
public class EdgeDetection extends Metaballs2D {
  /** Red channel flag. */
  public static final byte C_R = 0x01;

  /** Green channel flag. */
  public static final byte C_G = 0x02;

  /** Blue channel flag. */
  public static final byte C_B = 0x04;

  /** Channels added into the scalar field. */
  private static final float CHANNEL_COUNT = 3.0f;

  /** Maximum byte value. */
  private static final float BYTE_MAX = 255.0f;

  /** Mask of the red byte. */
  private static final int RED_MASK = 0x00FF0000;

  /** Mask of the green byte. */
  private static final int GREEN_MASK = 0x0000FF00;

  /** Mask of the blue byte. */
  private static final int BLUE_MASK = 0x000000FF;

  /** Shift of the red byte. */
  private static final int RED_SHIFT = 16;

  /** Shift of the green byte. */
  private static final int GREEN_SHIFT = 8;

  /** Bit for the first square corner. */
  private static final int CORNER_A = 1;

  /** Bit for the second square corner. */
  private static final int CORNER_B = 2;

  /** Bit for the third square corner. */
  private static final int CORNER_C = 4;

  /** Bit for the fourth square corner. */
  private static final int CORNER_D = 8;

  /** Lowest accepted threshold. */
  private static final float MIN_THRESHOLD = 0.0f;

  /** Highest accepted threshold. */
  private static final float MAX_THRESHOLD = 1.0f;

  /** Image width. */
  private int imgWidth;

  /** Image height. */
  private int imgHeight;

  /** Packed pixels. */
  private int[] pixels;

  /** Whether values above the iso value count as inside. */
  private boolean posDiscrimination;

  /** Scale from a unit threshold to an iso value. */
  private float coefficient = CHANNEL_COUNT * BYTE_MAX;

  /**
   * Creates a detector for an image size.
   *
   * @param width image width
   * @param height image height
   */
  public EdgeDetection(final int width, final int height) {
    this.imgWidth = width;
    this.imgHeight = height;
    super.init(width, height);
    posDiscrimination = false;
  }

  /**
   * Chooses which side of the iso value is inside.
   *
   * @param positive true to treat larger values as inside
   */
  public void setPosDiscrimination(final boolean positive) {
    posDiscrimination = positive;
  }

  /**
   * Sets the threshold as a fraction of the coefficient.
   *
   * @param value threshold from 0 to 1
   */
  public void setThreshold(final float value) {
    float clamped = value;
    if (clamped < MIN_THRESHOLD) {
      clamped = MIN_THRESHOLD;
    }
    if (clamped > MAX_THRESHOLD) {
      clamped = MAX_THRESHOLD;
    }
    setIsovalue(clamped * coefficient);
  }

  /**
   * Stores the image pixels.
   *
   * @param source packed pixels
   */
  public void setImage(final int[] source) {
    this.pixels = source;
  }

  /**
   * Detects edges in an image.
   *
   * @param source packed pixels
   */
  public void computeEdges(final int[] source) {
    setImage(source);
    computeMesh();
  }

  /** Fills the scalar field with the sum of the colour channels. */
  @Override
  public void computeIsovalue() {
    for (int y = 0; y < imgHeight; y++) {
      for (int x = 0; x < imgWidth; x++) {
        final int offset = x + imgWidth * y;
        final int pixel = pixels[offset];
        final int red = (pixel & RED_MASK) >> RED_SHIFT;
        final int green = (pixel & GREEN_MASK) >> GREEN_SHIFT;
        final int blue = pixel & BLUE_MASK;
        gridValues()[offset] = red + green + blue;
      }
    }
  }

  /**
   * Classifies one square, optionally inverting the comparison.
   *
   * @param x square x
   * @param y square y
   * @return the square index
   */
  @Override
  protected int getSquareIndex(final int x, final int y) {
    int squareIndex = 0;
    final int offy = resolutionX() * y;
    final int offy1 = resolutionX() * (y + 1);
    final float[] field = gridValues();
    final float iso = isoValue();
    if (posDiscrimination) {
      if (field[x + offy] > iso) {
        squareIndex |= CORNER_A;
      }
      if (field[x + 1 + offy] > iso) {
        squareIndex |= CORNER_B;
      }
      if (field[x + 1 + offy1] > iso) {
        squareIndex |= CORNER_C;
      }
      if (field[x + offy1] > iso) {
        squareIndex |= CORNER_D;
      }
    } else {
      if (field[x + offy] < iso) {
        squareIndex |= CORNER_A;
      }
      if (field[x + 1 + offy] < iso) {
        squareIndex |= CORNER_B;
      }
      if (field[x + 1 + offy1] < iso) {
        squareIndex |= CORNER_C;
      }
      if (field[x + offy1] < iso) {
        squareIndex |= CORNER_D;
      }
    }
    return squareIndex;
  }

  /**
   * Returns one edge vertex.
   *
   * @param index vertex index
   * @return the vertex
   */
  public EdgeVertex getEdgeVertex(final int index) {
    return edgeVertices()[index];
  }
}
