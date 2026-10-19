package dartboardextreme;

import java.awt.geom.Point2D;

/** Placeholder for turning dart positions into a score. */
public class ScoreCalculator {
  /** Creates a calculator with no positions. */
  public ScoreCalculator() {
    // no positions yet
  }

  /**
   * Creates a calculator for the given dart positions.
   *
   * @param dartPositions positions of the darts
   */
  public ScoreCalculator(final Point2D.Double[] dartPositions) {
    // scoring is not implemented yet
    if (dartPositions != null && dartPositions.length < 0) {
      return;
    }
  }
}
