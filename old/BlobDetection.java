package old;

import java.lang.reflect.Method;

/** Finds blobs by walking the marching-squares grid. */
public final class BlobDetection extends EdgeDetection {
  /** Default cap on how many blobs are stored. */
  private static final int DEFAULT_BLOB_MAX = 1000;

  /** Bound used when a blob has not yet recorded that side. */
  private static final float SENTINEL = 1000.0f;

  /** Square index that means every corner is inside. */
  private static final int FULL_SQUARE = 15;

  /** Horizontal edge bit. */
  private static final int EDGE_X = 1;

  /** Vertical edge bit. */
  private static final int EDGE_Y = 2;

  /** Neighbour bit for X plus one. */
  private static final int BIT_X_PLUS = 1;

  /** Neighbour bit for X minus one. */
  private static final int BIT_X_MINUS = 2;

  /** Neighbour bit for Y plus one. */
  private static final int BIT_Y_PLUS = 4;

  /** Neighbour bit for Y minus one. */
  private static final int BIT_Y_MINUS = 8;

  /** Maximum number of blobs. */
  private static int blobMaxNumber = DEFAULT_BLOB_MAX;

  /** Listener that receives new blobs. */
  private Object listener;

  /** Optional filter invoked for each blob. */
  private Method filterBlobMethod;

  /** Number of blobs found. */
  private int blobNumber;

  /** Stored blobs. */
  private Blob[] blob;

  /** Squares already visited. */
  private boolean[] gridVisited;

  /** Minimum blob width accepted by the unused filter. */
  private int blobWidthMin;

  /** Minimum blob height accepted by the unused filter. */
  private int blobHeightMin;

  /**
   * Creates a detector for an image size.
   *
   * @param width image width
   * @param height image height
   */
  public BlobDetection(final int width, final int height) {
    super(width, height);
    gridVisited = new boolean[gridCount()];
    blob = new Blob[blobMaxNumber];
    blobNumber = 0;
    for (int i = 0; i < blobMaxNumber; i++) {
      blob[i] = new Blob(this);
    }
    blobWidthMin = 0;
    blobHeightMin = 0;
    filterBlobMethod = null;
  }

  /**
   * Sets the maximum number of blobs.
   *
   * @param count maximum blob count
   */
  public void setBlobMaxNumber(final int count) {
    blobMaxNumber = count;
  }

  /**
   * Returns one stored blob.
   *
   * @param index blob index
   * @return the blob, or null when the index is past the end
   */
  public Blob getBlob(final int index) {
    if (index < blobNumber) {
      return blob[index];
    }
    return null;
  }

  /**
   * Returns how many blobs were found.
   *
   * @return the blob count
   */
  public int getBlobNb() {
    return blobNumber;
  }

  /**
   * Finds blobs in a packed image.
   *
   * @param source packed pixels
   */
  public void computeBlobs(final int[] source) {
    setImage(source);
    for (int i = 0; i < gridCount(); i++) {
      gridVisited[i] = false;
    }
    computeIsovalue();
    setLineCount(0);
    blobNumber = 0;
    float vx = 0.0f;
    for (int x = 0; x < resolutionX() - 1; x++) {
      float vy = 0.0f;
      for (int y = 0; y < resolutionY() - 1; y++) {
        final int offset = x + resolutionX() * y;
        if (!gridVisited[offset]) {
          final int squareIndex = getSquareIndex(x, y);
          if (squareIndex > 0 && squareIndex < FULL_SQUARE) {
            if (blobNumber < blobMaxNumber) {
              findBlob(blobNumber, x, y);
              blobNumber++;
            }
          }
        }
        vy += stepY();
      }
      vx += stepX();
    }
    setLineCount(lineCount() / 2);
  }

  /**
   * Traces one blob and drops it when it sits on the sentinel bounds.
   *
   * @param iBlob blob index
   * @param x start x
   * @param y start y
   */
  public void findBlob(final int iBlob, final int x, final int y) {
    final Blob current = blob[iBlob];
    current.setId(iBlob);
    current.setXMin(SENTINEL);
    current.setXMax(-SENTINEL);
    current.setYMin(SENTINEL);
    current.setYMax(-SENTINEL);
    current.setNbLine(0);

    computeEdgeVertex(iBlob, x, y);

    // Sometimes large blobs are detected on the grid edges.
    final boolean lowX = current.getXMin() >= SENTINEL;
    final boolean highX = current.getXMax() <= -SENTINEL;
    final boolean missingX = lowX || highX;
    final boolean lowY = current.getYMin() >= SENTINEL;
    final boolean highY = current.getYMax() <= -SENTINEL;
    final boolean missingY = lowY || highY;
    if (missingX || missingY) {
      blobNumber--;
    } else {
      current.update();
      applyFilter(current);
    }
  }

  /**
   * Applies the optional listener filter.
   *
   * @param current blob that was just traced
   */
  private void applyFilter(final Blob current) {
    if (filterBlobMethod == null) {
      return;
    }
    try {
      final Object[] arguments = new Object[] {current};
      final Object invoked = filterBlobMethod.invoke(listener, arguments);
      final Boolean returned = (Boolean) invoked;
      if (!returned.booleanValue()) {
        blobNumber--;
      }
    } catch (final Exception ex) {
      System.out.println("Disabling filterBlobMethod() because of an error.");
      filterBlobMethod = null;
    }
  }

  /**
   * Walks the squares that belong to one blob.
   *
   * @param iBlob blob index
   * @param x square x
   * @param y square y
   */
  private void computeEdgeVertex(final int iBlob, final int x, final int y) {
    final int offset = x + resolutionX() * y;
    if (gridVisited[offset]) {
      return;
    }
    gridVisited[offset] = true;

    final int squareIndex = getSquareIndex(x, y);
    final float vx = x * stepX();
    final float vy = y * stepY();
    if (!cut(iBlob, x, y, squareIndex)) {
      return;
    }
    grow(iBlob, x, y);
    walk(iBlob, x, y, squareIndex);
  }

  /**
   * Records the cut edges of one square onto the blob.
   *
   * @param id blob index
   * @param x square x
   * @param y square y
   * @param sq square configuration
   * @return false when the blob line buffer is full and the walk must stop
   */
  private boolean cut(final int id, final int x, final int y, final int sq) {
    final Blob current = blob[id];
    int slot = 0;
    int edge = MetaballsTable.edgeCutAt(sq, slot);
    while (edge != -1) {
      if (current.getNbLine() >= Blob.MAX_NBLINE) {
        return false;
      }
      final int[] table = voxels();
      final int width = resolutionX();
      final int edgeIndex;
      edgeIndex = MetaballsTable.voxelIndexForEdge(table, width, x, y, edge);
      final int drawn = lineCount();
      linesToDraw()[drawn] = edgeIndex;
      setLineCount(drawn + 1);
      final int stored = current.getNbLine();
      current.lineBuffer()[stored] = edgeIndex;
      current.setNbLine(stored + 1);
      slot++;
      edge = MetaballsTable.edgeCutAt(sq, slot);
    }
    return true;
  }

  /**
   * Interpolates the blob edges and grows its bounds.
   *
   * @param id blob index
   * @param x square x
   * @param y square y
   */
  private void grow(final int id, final int x, final int y) {
    final int at = x + resolutionX() * y;
    final int s = getSquareIndex(x, y);
    final float vx = x * stepX();
    final float vy = y * stepY();
    final int toCompute = MetaballsTable.edgeToComputeAt(s);
    if (toCompute <= 0) {
      return;
    }
    final Blob current = blob[id];
    final float[] field = gridValues();
    if ((toCompute & EDGE_X) > 0) {
      final float span = field[at + 1] - field[at];
      final float t = (isoValue() - field[at]) / span;
      final float value = vx * (1.0f - t) + t * (vx + stepX());
      edgeVertices()[voxels()[at]].x = value;
      if (value < current.getXMin()) {
        current.setXMin(value);
      }
      if (value > current.getXMax()) {
        current.setXMax(value);
      }
    }
    if ((toCompute & EDGE_Y) > 0) {
      final float span = field[at + resolutionX()] - field[at];
      final float t = (isoValue() - field[at]) / span;
      final float value = vy * (1.0f - t) + t * (vy + stepY());
      edgeVertices()[voxels()[at] + 1].y = value;
      if (value < current.getYMin()) {
        current.setYMin(value);
      }
      if (value > current.getYMax()) {
        current.setYMax(value);
      }
    }
  }

  /**
   * Continues the walk into neighbouring squares.
   *
   * @param id blob index
   * @param x square x
   * @param y square y
   * @param sq square configuration
   */
  private void walk(final int id, final int x, final int y, final int sq) {
    final int neighbor = MetaballsTable.neighborVoxelAt(sq);
    if (x < resolutionX() - 2 && (neighbor & BIT_X_PLUS) > 0) {
      computeEdgeVertex(id, x + 1, y);
    }
    if (x > 0 && (neighbor & BIT_X_MINUS) > 0) {
      computeEdgeVertex(id, x - 1, y);
    }
    if (y < resolutionY() - 2 && (neighbor & BIT_Y_PLUS) > 0) {
      computeEdgeVertex(id, x, y + 1);
    }
    if (y > 0 && (neighbor & BIT_Y_MINUS) > 0) {
      computeEdgeVertex(id, x, y - 1);
    }
  }

  /**
   * Installs {@code newBlobDetectedEvent} from the listener, if it exists.
   *
   * @param owner listener
   */
  public void activeCustomFilter(final Object owner) {
    this.listener = owner;
    try {
      final Class<?>[] types = new Class<?>[] {Blob.class};
      final Class<?> type = owner.getClass();
      filterBlobMethod = type.getMethod("newBlobDetectedEvent", types);
    } catch (final Exception ex) {
      // No such method, or an error, which is fine to ignore.
      listener = owner;
    }
  }
}
