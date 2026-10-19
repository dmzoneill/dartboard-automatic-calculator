package dartBoardExtreme.edgeDetection;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * Configurable Canny edge detector.
 *
 * <p>This software has been released into the public domain. Please read the
 *
 * <p>notes in this source file for additional information.
 *
 * <p>This class provides a configurable implementation of the Canny edge
 *
 * <p>detection algorithm. This classic algorithm has a number of shortcomings,
 *
 * <p>but remains an effective tool
 *
 * <p>in many scenarios. This class is designed for single threaded use only.
 *
 * <p>Sample usage:
 *
 * <pre>
 * CannyEdgeDetector detector = new CannyEdgeDetector();
 * detector.setLowThreshold(0.5f);
 * detector.setHighThreshold(1f);
 * detector.setSourceImage(frame);
 * detector.process();
 * BufferedImage edges = detector.getEdgesImage();
 * </pre>
 *
 * <p>For a more complete understanding of this edge detector's parameters
 *
 * <p>consult an explanation of the algorithm.
 *
 * @author Tom Gibara
 */
public class CannyEdgeDetector {

  /** Gaussian weight treated as zero. */
  private static final float GAUSSIAN_CUT_OFF = 0.005f;

  /** Scale applied to gradient magnitudes before they are stored. */
  private static final float MAGNITUDE_SCALE = 100f;

  /** Gradient magnitude that saturates the stored value. */
  private static final float MAGNITUDE_LIMIT = 1000f;

  /** Stored magnitude used at and above the limit. */
  private static final float SCALED_LIMIT = MAGNITUDE_SCALE * MAGNITUDE_LIMIT;

  /** Stored magnitude used at and above the limit. */
  private static final int MAGNITUDE_MAX = (int) SCALED_LIMIT;

  /** Default low hysteresis threshold. */
  private static final float DEFAULT_LOW_THRESHOLD = 2.5f;

  /** Default high hysteresis threshold. */
  private static final float DEFAULT_HIGH_THRESHOLD = 7.5f;

  /** Default Gaussian radius. */
  private static final float DEFAULT_KERNEL_RADIUS = 2f;

  /** Default Gaussian width in pixels. */
  private static final int DEFAULT_KERNEL_WIDTH = 16;

  /** Smallest accepted Gaussian width. */
  private static final int MIN_KERNEL_WIDTH = 2;

  /** Smallest accepted Gaussian radius. */
  private static final float MIN_KERNEL_RADIUS = 0.1f;

  /** Samples averaged into each Gaussian kernel weight. */
  private static final float KERNEL_SAMPLE_COUNT = 3f;

  /** Factor in the Gaussian normalisation. */
  private static final float KERNEL_TWO = 2f;

  /** Offset used when sampling the Gaussian on either side of a pixel. */
  private static final float HALF_PIXEL = 0.5f;

  /** Red weight in the luminance conversion. */
  private static final float LUMINANCE_RED = 0.299f;

  /** Green weight in the luminance conversion. */
  private static final float LUMINANCE_GREEN = 0.587f;

  /** Blue weight in the luminance conversion. */
  private static final float LUMINANCE_BLUE = 0.114f;

  /** Histogram bins, one per byte value. */
  private static final int HISTOGRAM_SIZE = 256;

  /** Largest byte value. */
  private static final int BYTE_MAX = 255;

  /** Mask for one byte. */
  private static final int BYTE_MASK = 0xff;

  /** Mask for one unsigned short. */
  private static final int SHORT_MASK = 0xffff;

  /** Divisor that maps an unsigned short onto a byte. */
  private static final int SHORT_TO_BYTE = 256;

  /** Bit shift of the red byte in a packed pixel. */
  private static final int RED_SHIFT = 16;

  /** Bit shift of the green byte in a packed pixel. */
  private static final int GREEN_SHIFT = 8;

  /** Mask of the red byte in a packed pixel. */
  private static final int RED_MASK = 0xff0000;

  /** Mask of the green byte in a packed pixel. */
  private static final int GREEN_MASK = 0xff00;

  /** Pixel stored when a point is not an edge. */
  private static final int BACKGROUND = 0xff000000;

  /** Image height in pixels. */
  private int height;

  /** Image width in pixels. */
  private int width;

  /** Pixel count. */
  private int picsize;

  /** Luminance, later reused as edge intensity. */
  private int[] data;

  /** Gradient magnitude at each pixel. */
  private int[] magnitude;

  /** Source image. */
  private BufferedImage sourceImage;

  /** Image of detected edges. */
  private BufferedImage edgesImage;

  /** Gaussian radius. */
  private float gaussianKernelRadius;

  /** Low hysteresis threshold. */
  private float lowThreshold;

  /** High hysteresis threshold. */
  private float highThreshold;

  /** Gaussian width in pixels. */
  private int gaussianKernelWidth;

  /** Whether luminance is histogram-equalised first. */
  private boolean contrastNormalized;

  /** Horizontal convolution. */
  private float[] xConv;

  /** Vertical convolution. */
  private float[] yConv;

  /** Horizontal gradient. */
  private float[] xGradient;

  /** Vertical gradient. */
  private float[] yGradient;

  /** Constructs a detector with default parameters. */
  public CannyEdgeDetector() {
    lowThreshold = DEFAULT_LOW_THRESHOLD;
    highThreshold = DEFAULT_HIGH_THRESHOLD;
    gaussianKernelRadius = DEFAULT_KERNEL_RADIUS;
    gaussianKernelWidth = DEFAULT_KERNEL_WIDTH;
    contrastNormalized = false;
  }

  /**
   * Returns the source image.
   *
   * @return the source image, or null
   */
  public BufferedImage getSourceImage() {
    return sourceImage;
  }

  /**
   * Sets the image that supplies luminance data.
   *
   * <p>A source image must be set before {@link #process()} is called.
   *
   * @param image a source of luminance data
   */
  public void setSourceImage(final BufferedImage image) {
    sourceImage = image;
  }

  /**
   * Returns the edge image from the last call to {@link #process()}.
   *
   * <p>Edge pixels are white and all other pixels are black. The image type is
   *
   * <p>TYPE_INT_ARGB.
   *
   * @return the edge image, or null if process has not been called
   */
  public BufferedImage getEdgesImage() {
    return edgesImage;
  }

  /**
   * Replaces the edge image without changing detection.
   *
   * @param image expected, though not required, to be null
   */
  public void setEdgesImage(final BufferedImage image) {
    this.edgesImage = image;
  }

  /**
   * Returns the low hysteresis threshold.
   *
   * @return the low hysteresis threshold
   */
  public float getLowThreshold() {
    return lowThreshold;
  }

  /**
   * Sets the low hysteresis threshold.
   *
   * <p>Suitable values must be determined experimentally. It is nonsensical,
   *
   * <p>though not prohibited, for this value to exceed the high threshold.
   *
   * @param threshold a low hysteresis threshold
   */
  public void setLowThreshold(final float threshold) {
    lowThreshold = requireNonNegative(threshold);
  }

  /**
   * Returns the high hysteresis threshold.
   *
   * @return the high hysteresis threshold
   */
  public float getHighThreshold() {
    return highThreshold;
  }

  /**
   * Sets the high hysteresis threshold.
   *
   * <p>Suitable values must be determined experimentally. It is nonsensical,
   *
   * <p>though not prohibited, for this value to be less than the low threshold.
   *
   * @param threshold a high hysteresis threshold
   */
  public void setHighThreshold(final float threshold) {
    highThreshold = requireNonNegative(threshold);
  }

  /**
   * Rejects a negative hysteresis threshold.
   *
   * @param threshold candidate threshold
   * @return the same threshold when it is zero or positive
   */
  private static float requireNonNegative(final float threshold) {
    if (threshold < 0) {
      throw new IllegalArgumentException();
    }
    return threshold;
  }

  /**
   * Returns the Gaussian kernel width in pixels.
   *
   * @return the radius of the convolution operation in pixels
   */
  public int getGaussianKernelWidth() {
    return gaussianKernelWidth;
  }

  /**
   * Sets the Gaussian kernel width in pixels.
   *
   * <p>The number of pixels across which the Gaussian kernel is applied. This
   *
   * <p>implementation will reduce the radius if the contribution of pixel
   *
   * <p>values is deemed negligible,
   *
   * <p>so this is actually a maximum radius.
   *
   * @param kernelWidth a radius for the convolution, at least 2
   */
  public void setGaussianKernelWidth(final int kernelWidth) {
    if (kernelWidth < MIN_KERNEL_WIDTH) {
      throw new IllegalArgumentException();
    }
    this.gaussianKernelWidth = kernelWidth;
  }

  /**
   * Returns the Gaussian kernel radius.
   *
   * @return the Gaussian kernel radius in pixels
   */
  public float getGaussianKernelRadius() {
    return gaussianKernelRadius;
  }

  /**
   * Sets the Gaussian kernel radius used before the gradient is calculated.
   *
   * @param radius a Gaussian kernel radius in pixels, must exceed 0.1
   */
  public void setGaussianKernelRadius(final float radius) {
    if (radius < MIN_KERNEL_RADIUS) {
      throw new IllegalArgumentException();
    }
    this.gaussianKernelRadius = radius;
  }

  /**
   * Reports whether contrast is normalised before edge extraction.
   *
   * @return whether the contrast is normalised
   */
  public boolean isContrastNormalized() {
    return contrastNormalized;
  }

  /**
   * Sets whether the contrast is normalised.
   *
   * @param normalized true if the contrast should be normalised
   */
  public void setContrastNormalized(final boolean normalized) {
    this.contrastNormalized = normalized;
  }

  /** Detects edges in the current source image. */
  public void process() {
    width = sourceImage.getWidth();
    height = sourceImage.getHeight();
    picsize = width * height;
    initArrays();
    readLuminance();
    if (contrastNormalized) {
      normalizeContrast();
    }
    computeGradients(gaussianKernelRadius, gaussianKernelWidth);
    final int low = Math.round(lowThreshold * MAGNITUDE_SCALE);
    final int high = Math.round(highThreshold * MAGNITUDE_SCALE);
    performHysteresis(low, high);
    thresholdEdges();
    writeEdges(data);
  }

  /** Allocates the working arrays when the image size changes. */
  private void initArrays() {
    if (data == null || picsize != data.length) {
      data = new int[picsize];
      magnitude = new int[picsize];
      xConv = new float[picsize];
      yConv = new float[picsize];
      xGradient = new float[picsize];
      yGradient = new float[picsize];
    }
  }

  /**
   * Computes gradients and non-maximal suppression.
   *
   * <p>The technique for non-maximal suppression and gradient computation is
   *
   * <p>derived from an implementation posted in the following forum, with the
   *
   * <p>clear intent of
   *
   * <p>others using the code:
   * http://forum.java.sun.com/thread.jspa?threadID=546211&amp;start=45&amp;
   *
   * <p>tstart=0
   *
   * <p>My code effectively mimics the algorithm exhibited above. Since I don't
   *
   * <p>know the
   *
   * <p>providence of the code that was posted it is a possibility (though I
   *
   * <p>think a very remote one) that
   *
   * <p>this code violates someone's intellectual property rights. If this
   *
   * <p>concerns you feel free to
   *
   * <p>contact me for an alternative, though less efficient, implementation.
   *
   * @param radius Gaussian radius
   * @param kernelWidth Gaussian width
   */
  private void computeGradients(final float radius, final int kernelWidth) {
    final float[] kernel = new float[kernelWidth];
    final float[] diffKernel = new float[kernelWidth];
    final int kwidth = fillKernels(kernel, diffKernel, radius);

    final int initX = kwidth - 1;
    final int maxX = width - (kwidth - 1);
    final int initY = width * (kwidth - 1);
    final int maxY = width * (height - (kwidth - 1));

    convolve(kernel, kwidth, initX, maxX, initY, maxY);
    horizontalGradient(diffKernel, kwidth, initX, maxX, initY, maxY);
    verticalGradient(diffKernel, kwidth, initY, maxY);
    suppressNonMaximum(kwidth);
  }

  /**
   * Fills the Gaussian and difference kernels.
   *
   * @param g Gaussian weights
   * @param d difference weights
   * @param r Gaussian radius
   * @return the used kernel width
   */
  private int fillKernels(final float[] g, final float[] d, final float r) {
    int kwidth;
    for (kwidth = 0; kwidth < g.length; kwidth++) {
      final float center = gaussian(kwidth, r);
      if (center <= GAUSSIAN_CUT_OFF && kwidth >= MIN_KERNEL_WIDTH) {
        break;
      }
      final float before = gaussian(kwidth - HALF_PIXEL, r);
      final float after = gaussian(kwidth + HALF_PIXEL, r);
      final float sum = center + before + after;
      final float twoPi = KERNEL_TWO * (float) Math.PI;
      final float area = twoPi * r * r;
      g[kwidth] = sum / KERNEL_SAMPLE_COUNT / area;
      d[kwidth] = after - before;
    }
    return kwidth;
  }

  /**
   * Convolves luminance in x and y.
   *
   * @param kernel Gaussian weights
   * @param kwidth used kernel width
   * @param initX first x
   * @param maxX exclusive x limit
   * @param initY first y offset
   * @param maxY exclusive y limit
   */
  private void convolve(
      final float[] kernel,
      final int kwidth,
      final int initX,
      final int maxX,
      final int initY,
      final int maxY) {
    for (int x = initX; x < maxX; x++) {
      for (int y = initY; y < maxY; y += width) {
        final int index = x + y;
        float sumX = data[index] * kernel[0];
        float sumY = sumX;
        int xOffset = 1;
        int yOffset = width;
        while (xOffset < kwidth) {
          final float weight = kernel[xOffset];
          sumY += weight * (data[index - yOffset] + data[index + yOffset]);
          sumX += weight * (data[index - xOffset] + data[index + xOffset]);
          yOffset += width;
          xOffset++;
        }
        yConv[index] = sumY;
        xConv[index] = sumX;
      }
    }
  }

  /**
   * Computes the horizontal gradient from the vertical convolution.
   *
   * @param diffKernel difference weights
   * @param kwidth used kernel width
   * @param initX first x
   * @param maxX exclusive x limit
   * @param initY first y offset
   * @param maxY exclusive y limit
   */
  private void horizontalGradient(
      final float[] diffKernel,
      final int kwidth,
      final int initX,
      final int maxX,
      final int initY,
      final int maxY) {
    for (int x = initX; x < maxX; x++) {
      for (int y = initY; y < maxY; y += width) {
        float sum = 0f;
        final int index = x + y;
        for (int i = 1; i < kwidth; i++) {
          sum += diffKernel[i] * (yConv[index - i] - yConv[index + i]);
        }
        xGradient[index] = sum;
      }
    }
  }

  /**
   * Computes the vertical gradient from the horizontal convolution.
   *
   * @param diff difference weights
   * @param kwidth used kernel width
   * @param initY first y offset
   * @param maxY exclusive y limit
   */
  private void verticalGradient(
      final float[] diff, final int kwidth, final int initY, final int maxY) {
    for (int x = kwidth; x < width - kwidth; x++) {
      for (int y = initY; y < maxY; y += width) {
        float sum = 0.0f;
        final int index = x + y;
        int yOffset = width;
        for (int i = 1; i < kwidth; i++) {
          final float left = xConv[index - yOffset];
          final float right = xConv[index + yOffset];
          sum += diff[i] * (left - right);
          yOffset += width;
        }
        yGradient[index] = sum;
      }
    }
  }

  /**
   * Keeps only local maxima of the gradient magnitude.
   *
   * @param kwidth used kernel width
   */
  private void suppressNonMaximum(final int kwidth) {
    final int initX = kwidth;
    final int maxX = width - kwidth;
    final int initY = width * kwidth;
    final int maxY = width * (height - kwidth);
    for (int x = initX; x < maxX; x++) {
      for (int y = initY; y < maxY; y += width) {
        storeMagnitude(x + y);
      }
    }
  }

  /**
   * Stores the magnitude at one pixel when it is a local maximum.
   *
   * <p>This performs the non-maximal suppression phase of Canny edge detection.
   *
   * <p>The orientation of the edge is not employed by this implementation. It
   *
   * <p>is a simple matter to
   *
   * <p>compute it at this point as Math.atan2(yGrad, xGrad).
   *
   * @param index pixel index
   */
  private void storeMagnitude(final int index) {
    final float xGrad = xGradient[index];
    final float yGrad = yGradient[index];
    final float gradMag = hypot(xGrad, yGrad);
    final int indexNorth = index - width;
    final int indexSouth = index + width;
    final int indexWest = index - 1;
    final int indexEast = index + 1;
    final int indexNorthWest = indexNorth - 1;
    final int indexNorthEast = indexNorth + 1;
    final int indexSouthWest = indexSouth - 1;
    final int indexSouthEast = indexSouth + 1;

    final float north = hypot(xGradient[indexNorth], yGradient[indexNorth]);
    final float south = hypot(xGradient[indexSouth], yGradient[indexSouth]);
    final float west = hypot(xGradient[indexWest], yGradient[indexWest]);
    final float east = hypot(xGradient[indexEast], yGradient[indexEast]);
    final float northEastX = xGradient[indexNorthEast];
    final float northEastY = yGradient[indexNorthEast];
    final float northEast = hypot(northEastX, northEastY);
    final float southEastX = xGradient[indexSouthEast];
    final float southEastY = yGradient[indexSouthEast];
    final float southEast = hypot(southEastX, southEastY);
    final float southWestX = xGradient[indexSouthWest];
    final float southWestY = yGradient[indexSouthWest];
    final float southWest = hypot(southWestX, southWestY);
    final float northWestX = xGradient[indexNorthWest];
    final float northWestY = yGradient[indexNorthWest];
    final float northWest = hypot(northWestX, northWestY);

    final boolean sameSign = xGrad * yGrad <= 0f;
    final boolean xDominates = Math.abs(xGrad) >= Math.abs(yGrad);
    final float tmp;
    final boolean maximum;
    if (sameSign) {
      if (xDominates) {
        tmp = Math.abs(xGrad * gradMag);
        final float sum = xGrad + yGrad;
        final float forward = Math.abs(yGrad * northEast - sum * east);
        final float backward = Math.abs(yGrad * southWest - sum * west);
        maximum = tmp >= forward && tmp > backward;
      } else {
        tmp = Math.abs(yGrad * gradMag);
        final float sum = yGrad + xGrad;
        final float forward = Math.abs(xGrad * northEast - sum * north);
        final float backward = Math.abs(xGrad * southWest - sum * south);
        maximum = tmp >= forward && tmp > backward;
      }
    } else if (xDominates) {
      tmp = Math.abs(xGrad * gradMag);
      final float delta = xGrad - yGrad;
      final float forward = Math.abs(yGrad * southEast + delta * east);
      final float backward = Math.abs(yGrad * northWest + delta * west);
      maximum = tmp >= forward && tmp > backward;
    } else {
      tmp = Math.abs(yGrad * gradMag);
      final float delta = yGrad - xGrad;
      final float forward = Math.abs(xGrad * southEast + delta * south);
      final float backward = Math.abs(xGrad * northWest + delta * north);
      maximum = tmp >= forward && tmp > backward;
    }
    if (maximum) {
      if (gradMag >= MAGNITUDE_LIMIT) {
        magnitude[index] = MAGNITUDE_MAX;
      } else {
        magnitude[index] = (int) (MAGNITUDE_SCALE * gradMag);
      }
    } else {
      magnitude[index] = 0;
    }
  }

  /**
   * Approximates the hypotenuse.
   *
   * <p>It is quite feasible to replace this method with one which only loosely
   *
   * <p>approximates the hypot function. Simple approximations such as
   *
   * <p>Math.abs(x) + Math.abs(y)
   *
   * <p>work fine.
   *
   * @param x horizontal component
   * @param y vertical component
   * @return the hypotenuse
   */
  private float hypot(final float x, final float y) {
    if (x == 0f) {
      return y;
    }
    if (y == 0f) {
      return x;
    }
    return (float) Math.sqrt(x * x + y * y);
  }

  /**
   * Evaluates an unnormalised Gaussian.
   *
   * @param x distance
   * @param sigma standard deviation
   * @return the Gaussian value
   */
  private float gaussian(final float x, final float sigma) {
    return (float) Math.exp(-(x * x) / (KERNEL_TWO * sigma * sigma));
  }

  /**
   * Traces edges from pixels above the high threshold.
   *
   * <p>This implementation reuses the data array to store both luminance data
   *
   * <p>from the image and edge intensity from the processing. This is done for
   *
   * <p>memory efficiency.
   *
   * @param low low threshold
   * @param high high threshold
   */
  private void performHysteresis(final int low, final int high) {
    Arrays.fill(data, 0);
    int offset = 0;
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (data[offset] == 0 && magnitude[offset] >= high) {
          follow(x, y, offset, low);
        }
        offset++;
      }
    }
  }

  /**
   * Follows one edge through neighbouring pixels.
   *
   * @param x1 centre x
   * @param y1 centre y
   * @param at centre index
   * @param low low threshold
   */
  private void follow(final int x1, final int y1, final int at, final int low) {
    final int x0 = x1 == 0 ? x1 : x1 - 1;
    final int x2 = x1 == width - 1 ? x1 : x1 + 1;
    final int y0 = y1 == 0 ? y1 : y1 - 1;
    final int y2 = y1 == height - 1 ? y1 : y1 + 1;

    data[at] = magnitude[at];
    for (int x = x0; x <= x2; x++) {
      for (int y = y0; y <= y2; y++) {
        final int i2 = x + y * width;
        final boolean other = y != y1 || x != x1;
        if (other && data[i2] == 0 && magnitude[i2] >= low) {
          follow(x, y, i2, low);
          return;
        }
      }
    }
  }

  /** Converts traced edges to black and white pixels. */
  private void thresholdEdges() {
    for (int i = 0; i < picsize; i++) {
      if (data[i] > 0) {
        data[i] = -1;
      } else {
        data[i] = BACKGROUND;
      }
    }
  }

  /**
   * Converts one colour to luminance.
   *
   * @param red red channel
   * @param green green channel
   * @param blue blue channel
   * @return the luminance
   */
  private int luminance(final float red, final float green, final float blue) {
    final float redPart = LUMINANCE_RED * red;
    final float greenPart = LUMINANCE_GREEN * green;
    final float bluePart = LUMINANCE_BLUE * blue;
    return Math.round(redPart + greenPart + bluePart);
  }

  /**
   * Returns the source pixels.
   *
   * @return the pixel buffer
   */
  private Object rawPixels() {
    final java.awt.image.Raster raster = sourceImage.getData();
    return raster.getDataElements(0, 0, width, height, null);
  }

  /** Reads luminance from the source image into {@code data}. */
  private void readLuminance() {
    final int type = sourceImage.getType();
    final int packedRgb = BufferedImage.TYPE_INT_RGB;
    final int packedArgb = BufferedImage.TYPE_INT_ARGB;
    if (type == packedRgb || type == packedArgb) {
      readPacked(type);
    } else if (type == BufferedImage.TYPE_BYTE_GRAY) {
      readByteGray();
    } else if (type == BufferedImage.TYPE_USHORT_GRAY) {
      readUshortGray();
    } else if (type == BufferedImage.TYPE_3BYTE_BGR) {
      readBgr();
    } else {
      throw new IllegalArgumentException("Unsupported image type: " + type);
    }
  }

  /**
   * Reads a packed RGB or ARGB image.
   *
   * @param type image type, accepted only to keep the call shape
   */
  private void readPacked(final int type) {
    final int packedRgb = BufferedImage.TYPE_INT_RGB;
    final int packedArgb = BufferedImage.TYPE_INT_ARGB;
    if (type != packedRgb && type != packedArgb) {
      throw new IllegalArgumentException("Unsupported image type: " + type);
    }
    final Object pixels = rawPixels();
    final int[] packed = (int[]) pixels;
    for (int i = 0; i < picsize; i++) {
      final int pixel = packed[i];
      final int red = (pixel & RED_MASK) >> RED_SHIFT;
      final int green = (pixel & GREEN_MASK) >> GREEN_SHIFT;
      final int blue = pixel & BYTE_MASK;
      data[i] = luminance(red, green, blue);
    }
  }

  /** Reads a byte gray image. */
  private void readByteGray() {
    final Object pixels = rawPixels();
    final byte[] gray = (byte[]) pixels;
    for (int i = 0; i < picsize; i++) {
      data[i] = gray[i] & BYTE_MASK;
    }
  }

  /** Reads an unsigned short gray image. */
  private void readUshortGray() {
    final Object pixels = rawPixels();
    final short[] gray = (short[]) pixels;
    for (int i = 0; i < picsize; i++) {
      data[i] = (gray[i] & SHORT_MASK) / SHORT_TO_BYTE;
    }
  }

  /** Reads a 3-byte BGR image. */
  private void readBgr() {
    final Object pixels = rawPixels();
    final byte[] bgr = (byte[]) pixels;
    int offset = 0;
    for (int i = 0; i < picsize; i++) {
      final int blue = bgr[offset] & BYTE_MASK;
      offset++;
      final int green = bgr[offset] & BYTE_MASK;
      offset++;
      final int red = bgr[offset] & BYTE_MASK;
      offset++;
      data[i] = luminance(red, green, blue);
    }
  }

  /** Equalises the luminance histogram. */
  private void normalizeContrast() {
    final int[] histogram = new int[HISTOGRAM_SIZE];
    for (int i = 0; i < data.length; i++) {
      histogram[data[i]]++;
    }
    final int[] remap = new int[HISTOGRAM_SIZE];
    int sum = 0;
    int j = 0;
    for (int i = 0; i < histogram.length; i++) {
      sum += histogram[i];
      final int target = sum * BYTE_MAX / picsize;
      for (int k = j + 1; k <= target; k++) {
        remap[k] = i;
      }
      j = target;
    }
    for (int i = 0; i < data.length; i++) {
      data[i] = remap[data[i]];
    }
  }

  /**
   * Writes edge pixels into an ARGB image.
   *
   * <p>There is currently no mechanism for obtaining the edge data in any
   *
   * <p>format other than an INT_ARGB BufferedImage. This may be remedied by
   *
   * <p>providing alternative
   *
   * <p>accessors.
   *
   * @param pixels edge pixels
   */
  private void writeEdges(final int[] pixels) {
    if (edgesImage == null) {
      final int argb = BufferedImage.TYPE_INT_ARGB;
      edgesImage = new BufferedImage(width, height, argb);
    }
    final WritableRaster tile = edgesImage.getWritableTile(0, 0);
    tile.setDataElements(0, 0, width, height, pixels);
  }
}
