package dartboardextreme.edgeDetection;

import java.awt.Color;
import java.awt.image.BufferedImage;

/** Sobel edge detector used by the calibrator. */
public class SobelEdgeDetector {
  /** Width and height of the convolution kernel. */
  private static final int KERNEL_SIZE = 3;

  /** Colour channels stored in the accumulator. */
  private static final int CHANNEL_COUNT = 3;

  /** Largest byte value. */
  private static final int BYTE_MAX = 255;

  /** Pixels darker than this stay transparent. */
  private static final int DARK_LIMIT = 100;

  /** Shift of the red byte. */
  private static final int RED_SHIFT = 16;

  /** Shift of one colour byte. */
  private static final int CHANNEL_SHIFT = 8;

  /** Mask of one byte. */
  private static final int BYTE_MASK = 0xff;

  /** Smaller Sobel weight. */
  private static final float WEIGHT_SMALL = 0.4f;

  /** Larger Sobel weight. */
  private static final float WEIGHT_LARGE = 0.8f;

  /** Edge image. */
  private BufferedImage sobel;

  /**
   * Detects edges in an image.
   *
   * @param img source image
   */
  public SobelEdgeDetector(final BufferedImage img) {
    this.sobelEdgeDetection(img);
  }

  /**
   * Runs the Sobel convolution.
   *
   * @param img source image
   */
  private void sobelEdgeDetection(final BufferedImage img) {
    final int imageType = BufferedImage.TYPE_INT_RGB;
    final int width = img.getWidth();
    final int height = img.getHeight();
    final BufferedImage edged = new BufferedImage(width, height, imageType);
    final float[] hx =
        new float[] {
          -WEIGHT_SMALL, 0, WEIGHT_SMALL,
          -WEIGHT_LARGE, 0, WEIGHT_LARGE,
          -WEIGHT_SMALL, 0, WEIGHT_SMALL
        };
    final float[] hy =
        new float[] {
          -WEIGHT_SMALL,
          -WEIGHT_LARGE,
          -WEIGHT_SMALL,
          0,
          0,
          0,
          WEIGHT_SMALL,
          WEIGHT_LARGE,
          WEIGHT_SMALL
        };
    final int[] rgbX = new int[CHANNEL_COUNT];
    final int[] rgbY = new int[CHANNEL_COUNT];

    // Ignore border pixels.
    for (int x = 1; x < img.getWidth() - 1; x++) {
      for (int y = 1; y < img.getHeight() - 1; y++) {
        convolvePixel(hx, KERNEL_SIZE, KERNEL_SIZE, img, x, y, rgbX);
        convolvePixel(hy, KERNEL_SIZE, KERNEL_SIZE, img, x, y, rgbY);
        int red = Math.abs(rgbX[0]) + Math.abs(rgbY[0]);
        int green = Math.abs(rgbX[1]) + Math.abs(rgbY[1]);
        int blue = Math.abs(rgbX[2]) + Math.abs(rgbY[2]);
        if (red > BYTE_MAX) {
          red = BYTE_MAX;
        }
        if (green > BYTE_MAX) {
          green = BYTE_MAX;
        }
        if (blue > BYTE_MAX) {
          blue = BYTE_MAX;
        }
        final int packed = (red << RED_SHIFT) | (green << CHANNEL_SHIFT) | blue;
        edged.setRGB(x, y, packed);
      }
    }
    this.sobel = edged;
  }

  /**
   * Applies one kernel at a pixel.
   *
   * @param kernel convolution weights
   * @param kernWidth kernel width
   * @param kernHeight kernel height
   * @param src source image
   * @param x pixel x
   * @param y pixel y
   * @param rgb channel accumulator; created when null
   * @return the accumulator
   */
  private int[] convolvePixel(
      final float[] kernel,
      final int kernWidth,
      final int kernHeight,
      final BufferedImage src,
      final int x,
      final int y,
      final int[] rgb) {
    final int[] channels;
    if (rgb == null) {
      channels = new int[CHANNEL_COUNT];
    } else {
      channels = rgb;
    }
    final int halfWidth = kernWidth / 2;
    final int halfHeight = kernHeight / 2;
    for (int component = 0; component < CHANNEL_COUNT; component++) {
      float sum = 0;
      for (int i = 0; i < kernel.length; i++) {
        final int row = (i / kernWidth) - halfWidth;
        final int column = (i - (kernWidth * row)) - halfHeight;
        if (x - row < 0 || x - row > src.getWidth()) {
          continue;
        }
        if (y - column < 0 || y - column > src.getHeight()) {
          continue;
        }
        final int srcRgb = src.getRGB(x - row, y - column);
        final int shift = RED_SHIFT - CHANNEL_SHIFT * component;
        sum = sum + kernel[i] * ((srcRgb >> shift) & BYTE_MASK);
      }
      channels[component] = (int) sum;
    }
    return channels;
  }

  /**
   * Returns the edges, with dark pixels transparent and the rest cyan.
   *
   * @return the coloured edge image
   */
  public BufferedImage edges() {
    final int argbType = BufferedImage.TYPE_INT_ARGB;
    final int width = this.sobel.getWidth();
    final int height = this.sobel.getHeight();
    final BufferedImage argb = new BufferedImage(width, height, argbType);
    for (int m = 0; m < this.sobel.getWidth(); m++) {
      for (int c = 0; c < this.sobel.getHeight(); c++) {
        final Color color = new Color(this.sobel.getRGB(m, c));
        final int alpha = 0;
        final Color transparent =
            new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        final boolean dark =
            color.getGreen() < DARK_LIMIT
                && color.getBlue() < DARK_LIMIT
                && color.getRed() < DARK_LIMIT;
        if (dark) {
          argb.setRGB(m, c, transparent.getRGB());
        } else {
          argb.setRGB(m, c, Color.cyan.getRGB());
        }
      }
    }
    return argb;
  }
}
