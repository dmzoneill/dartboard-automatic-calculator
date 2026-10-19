package dartboardextreme.edgeDetection;

import java.awt.image.BufferedImage;

/** One line in Hough space. */
public class HoughLine {
  /** Quarter turn, in radians as a fraction of pi. */
  private static final double QUARTER_TURN = 0.25d;

  /** Three-quarter turn, in radians as a fraction of pi. */
  private static final double THREE_QUARTER_TURN = 0.75d;

  /** Angle of the line. */
  private double theta;

  /** Distance of the line from the origin. */
  private double distance;

  /**
   * Creates a line from its Hough parameters.
   *
   * @param angle angle of the line
   * @param radius distance from the origin
   */
  public HoughLine(final double angle, final double radius) {
    this.theta = angle;
    this.distance = radius;
  }

  /**
   * Draws the line on an image.
   *
   * @param image image to draw on
   * @param color packed RGB colour
   * @return the same image
   */
  public BufferedImage draw(final BufferedImage image, final int color) {
    final int height = image.getHeight();
    final int width = image.getWidth();

    // During processing h_h is doubled so that negative r values fit.
    final double diagonal = Math.sqrt(2) * Math.max(height, width);
    final int houghHeight = (int) diagonal / 2;

    final float centerX = width / 2;
    final float centerY = height / 2;
    final double tsin = Math.sin(theta);
    final double tcos = Math.cos(theta);
    final double shallow = Math.PI * QUARTER_TURN;
    final double steep = Math.PI * THREE_QUARTER_TURN;

    if (theta < shallow || theta > steep) {
      for (int y = 0; y < height; y++) {
        final double dy = (y - centerY) * tsin;
        final double raw = ((distance - houghHeight) - dy) / tcos;
        final int x = (int) (raw + centerX);
        if (x < width && x >= 0) {
          image.setRGB(x, y, color);
        }
      }
    } else {
      for (int x = 0; x < width; x++) {
        final double dx = (x - centerX) * tcos;
        final double raw = ((distance - houghHeight) - dx) / tsin;
        final int y = (int) (raw + centerY);
        if (y < height && y >= 0) {
          image.setRGB(x, y, color);
        }
      }
    }
    return image;
  }
}
