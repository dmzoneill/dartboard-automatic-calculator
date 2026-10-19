package dartboardextreme;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** Window that shows an image and lets the user drag it. */
public class BufferedImageMouseDrag extends JFrame {
  /** Window width in pixels. */
  private static final int WINDOW_WIDTH = 1000;

  /** Window height in pixels. */
  private static final int WINDOW_HEIGHT = 800;

  /** Canvas that owns the image. */
  private DisplayCanvas canvas;

  /** Opens the drag window. */
  public BufferedImageMouseDrag() {
    super();
    final Container container = getContentPane();
    canvas = new DisplayCanvas();
    container.add(canvas);
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
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    setVisible(true);
  }

  /**
   * Starts the drag window.
   *
   * @param args unused
   */
  public static void main(final String[] args) {
    new BufferedImageMouseDrag();
  }
}

/** Panel that loads a photograph and follows the mouse. */
class DisplayCanvas extends JPanel {
  /** Sample photograph. */
  private static final String SAMPLE_IMAGE = "DSCN0504.JPG";

  /** Image x offset. */
  private int x;

  /** Image y offset. */
  private int y;

  /** Image width. */
  private int width;

  /** Image height. */
  private int height;

  /** Buffered copy of the photograph. */
  private BufferedImage bi;

  /** Loads the sample image and prepares the buffer. */
  DisplayCanvas() {
    final Image image = getToolkit().getImage(SAMPLE_IMAGE);
    final MediaTracker tracker = new MediaTracker(this);
    tracker.addImage(image, 1);
    try {
      tracker.waitForAll();
    } catch (final Exception ex) {
      System.out.println("Exception while loading image.");
    }

    if (image.getWidth(this) == -1) {
      System.out.println("no gif file");
      System.exit(0);
    }

    width = image.getWidth(this);
    height = image.getHeight(this);

    setBackground(Color.white);
    setSize(width, height);
    addMouseMotionListener(new MouseMotionHandler());

    final int imageType = BufferedImage.TYPE_INT_ARGB;
    final int imageWidth = image.getWidth(this);
    final int imageHeight = image.getHeight(this);
    bi = new BufferedImage(imageWidth, imageHeight, imageType);
    final Graphics2D big = bi.createGraphics();
    big.drawImage(image, 0, 0, this);
  }

  /**
   * Paints the buffered image at the current drag offset.
   *
   * @param graphics destination
   */
  @Override
  public void paintComponent(final Graphics graphics) {
    super.paintComponent(graphics);
    final Graphics2D g2d = (Graphics2D) graphics;
    g2d.drawImage(bi, x + width, y + height, this);
  }

  /** Updates the image offset while the mouse is dragged. */
  class MouseMotionHandler extends MouseMotionAdapter {
    /**
     * Moves the image with the pointer.
     *
     * @param event the drag event
     */
    @Override
    public void mouseDragged(final MouseEvent event) {
      x = event.getX() - width;
      y = event.getY() - height;
      repaint();
    }

    /**
     * Present for the original listener shape. Does nothing.
     *
     * @param event the click event
     */
    public void mouseClick(final MouseEvent event) {
      // no click behaviour
    }
  }
}
