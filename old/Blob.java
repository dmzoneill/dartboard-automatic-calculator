package old;

/** One detected blob and the lines that outline it. */
public class Blob {
  /** Maximum lines stored on one blob. */
  public static final int MAX_NBLINE = 4000;

  /** Half of a span, used when recentring. */
  private static final float HALF = 0.5f;

  /** Detector that owns this blob. */
  private BlobDetection parent;

  /** Identifier. */
  private int id;

  /** Centre x. */
  private float x;

  /** Centre y. */
  private float y;

  /** Width. */
  private float w;

  /** Height. */
  private float h;

  /** Minimum x. */
  private float xMin;

  /** Maximum x. */
  private float xMax;

  /** Minimum y. */
  private float yMin;

  /** Maximum y. */
  private float yMax;

  /** Line endpoint indexes. */
  private int[] line;

  /** Number of stored line endpoints. */
  private int nbLine;

  /**
   * Creates a blob owned by a detector.
   *
   * @param owner detector
   */
  public Blob(final BlobDetection owner) {
    this.parent = owner;
    line = new int[MAX_NBLINE];
    nbLine = 0;
  }

  /**
   * Returns the first vertex of a line.
   *
   * @param iEdge line index
   * @return the vertex, or null when the index is past the end
   */
  public EdgeVertex getEdgeVertexA(final int iEdge) {
    if (iEdge * 2 < parent.lineCount() * 2) {
      return parent.getEdgeVertex(line[iEdge * 2]);
    }
    return null;
  }

  /**
   * Returns the second vertex of a line.
   *
   * @param iEdge line index
   * @return the vertex, or null when the index is past the end
   */
  public EdgeVertex getEdgeVertexB(final int iEdge) {
    if ((iEdge * 2 + 1) < parent.lineCount() * 2) {
      return parent.getEdgeVertex(line[iEdge * 2 + 1]);
    }
    return null;
  }

  /**
   * Returns how many lines were stored.
   *
   * @return the line count
   */
  public int getEdgeNb() {
    return nbLine;
  }

  /** Updates the centre and size from the recorded bounds. */
  public void update() {
    w = xMax - xMin;
    h = yMax - yMin;
    x = HALF * (xMax + xMin);
    y = HALF * (yMax + yMin);
    nbLine /= 2;
  }

  /**
   * Sets the identifier.
   *
   * @param identifier identifier
   */
  public void setId(final int identifier) {
    id = identifier;
  }

  /**
   * Returns the minimum x.
   *
   * @return the minimum x
   */
  public float getXMin() {
    return xMin;
  }

  /**
   * Sets the minimum x.
   *
   * @param value minimum x
   */
  public void setXMin(final float value) {
    xMin = value;
  }

  /**
   * Returns the maximum x.
   *
   * @return the maximum x
   */
  public float getXMax() {
    return xMax;
  }

  /**
   * Sets the maximum x.
   *
   * @param value maximum x
   */
  public void setXMax(final float value) {
    xMax = value;
  }

  /**
   * Returns the minimum y.
   *
   * @return the minimum y
   */
  public float getYMin() {
    return yMin;
  }

  /**
   * Sets the minimum y.
   *
   * @param value minimum y
   */
  public void setYMin(final float value) {
    yMin = value;
  }

  /**
   * Returns the maximum y.
   *
   * @return the maximum y
   */
  public float getYMax() {
    return yMax;
  }

  /**
   * Sets the maximum y.
   *
   * @param value maximum y
   */
  public void setYMax(final float value) {
    yMax = value;
  }

  /**
   * Returns the line buffer.
   *
   * @return the line buffer
   */
  public int[] lineBuffer() {
    return line;
  }

  /**
   * Returns how many line endpoints are stored.
   *
   * @return the endpoint count
   */
  public int getNbLine() {
    return nbLine;
  }

  /**
   * Sets how many line endpoints are stored.
   *
   * @param count endpoint count
   */
  public void setNbLine(final int count) {
    nbLine = count;
  }
}
