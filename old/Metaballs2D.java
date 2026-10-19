package old;

/** Two-dimensional metaball mesh. */
public class Metaballs2D {
  /** Bit for the first square corner. */
  private static final int CORNER_A = 1;

  /** Bit for the second square corner. */
  private static final int CORNER_B = 2;

  /** Bit for the third square corner. */
  private static final int CORNER_C = 4;

  /** Bit for the fourth square corner. */
  private static final int CORNER_D = 8;

  /** Vertices stored for each voxel. */
  private static final int VERTICES_PER_VOXEL = 2;

  /** Iso value. */
  private float isoValue;

  /** Horizontal resolution. */
  private int resolutionX;

  /** Vertical resolution. */
  private int resolutionY;

  /** Horizontal step. */
  private float stepX;

  /** Vertical step. */
  private float stepY;

  /** Scalar field. */
  private float[] gridValue;

  /** Number of grid samples. */
  private int gridCount;

  /** Voxel table. */
  private int[] voxel;

  /** Number of voxels. */
  private int voxelCount;

  /** Edge vertices. */
  private EdgeVertex[] edgeVertices;

  /** Number of edge vertices. */
  private int edgeVertexCount;

  /** Lines passed to the renderer. */
  private int[] lineToDraw;

  /** Number of line endpoints stored. */
  private int lineCount;

  /** Creates an uninitialised mesh. */
  public Metaballs2D() {
    // fields are filled by init
  }

  /**
   * Allocates the grid for a resolution.
   *
   * @param width horizontal samples
   * @param height vertical samples
   */
  public void init(final int width, final int height) {
    this.resolutionX = width;
    this.resolutionY = height;
    this.stepX = 1.0f / (float) (width - 1);
    this.stepY = 1.0f / (float) (height - 1);

    gridCount = width * height;
    gridValue = new float[gridCount];
    voxelCount = gridCount;
    voxel = new int[voxelCount];
    edgeVertices = new EdgeVertex[VERTICES_PER_VOXEL * voxelCount];
    edgeVertexCount = VERTICES_PER_VOXEL * voxelCount;
    lineToDraw = new int[VERTICES_PER_VOXEL * voxelCount];
    lineCount = 0;

    int index = 0;
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        final int vertex = VERTICES_PER_VOXEL * index;
        voxel[x + width * y] = vertex;
        edgeVertices[vertex] = new EdgeVertex(x * stepX, y * stepY);
        edgeVertices[vertex + 1] = new EdgeVertex(x * stepX, y * stepY);
        index++;
      }
    }
  }

  /**
   * Fills the scalar field.
   *
   * <p>The original sample placed one metaball at the centre of the screen.
   *
   * <p>That body is intentionally empty so subclasses can supply the field.
   */
  public void computeIsovalue() {
    // subclasses supply the scalar field
  }

  /** Builds the line mesh from the current scalar field. */
  public void computeMesh() {
    computeIsovalue();
    lineCount = 0;
    float vx = 0.0f;
    for (int x = 0; x < resolutionX - 1; x++) {
      float vy = 0.0f;
      for (int y = 0; y < resolutionY - 1; y++) {
        final int offset = x + resolutionX * y;
        final int squareIndex = getSquareIndex(x, y);
        appendEdges(squareIndex, x, y);
        final int toCompute = MetaballsTable.edgeToComputeAt(squareIndex);
        if (toCompute > 0) {
          mix(toCompute, offset, vx, vy);
        }
        vy += stepY;
      }
      vx += stepX;
    }
    lineCount /= VERTICES_PER_VOXEL;
  }

  /**
   * Appends the cut edges of one square.
   *
   * @param squareIndex square configuration
   * @param x square x
   * @param y square y
   */
  private void appendEdges(final int squareIndex, final int x, final int y) {
    int slot = 0;
    int edge = MetaballsTable.edgeCutAt(squareIndex, slot);
    while (edge != -1) {
      final int[] table = voxel;
      final int vertex;
      vertex = MetaballsTable.voxelIndexForEdge(table, resolutionX, x, y, edge);
      lineToDraw[lineCount] = vertex;
      lineCount++;
      slot++;
      edge = MetaballsTable.edgeCutAt(squareIndex, slot);
    }
  }

  /**
   * Interpolates the edges that this square cuts.
   *
   * @param m edge mask
   * @param at grid offset
   * @param vx horizontal position
   * @param vy vertical position
   */
  private void mix(final int m, final int at, final float vx, final float vy) {
    if ((m & CORNER_A) > 0) {
      final float span = gridValue[at + 1] - gridValue[at];
      final float t = (isoValue - gridValue[at]) / span;
      edgeVertices[voxel[at]].x = vx * (1.0f - t) + t * (vx + stepX);
    }
    if ((m & CORNER_B) > 0) {
      final float span = gridValue[at + resolutionX] - gridValue[at];
      final float t = (isoValue - gridValue[at]) / span;
      edgeVertices[voxel[at] + 1].y = vy * (1.0f - t) + t * (vy + stepY);
    }
  }

  /**
   * Classifies one square against the iso value.
   *
   * @param x square x
   * @param y square y
   * @return the square index
   */
  protected int getSquareIndex(final int x, final int y) {
    int squareIndex = 0;
    final int offy = resolutionX * y;
    final int offy1 = resolutionX * (y + 1);
    if (gridValue[x + offy] < isoValue) {
      squareIndex |= CORNER_A;
    }
    if (gridValue[x + 1 + offy] < isoValue) {
      squareIndex |= CORNER_B;
    }
    if (gridValue[x + 1 + offy1] < isoValue) {
      squareIndex |= CORNER_C;
    }
    if (gridValue[x + offy1] < isoValue) {
      squareIndex |= CORNER_D;
    }
    return squareIndex;
  }

  /**
   * Sets the iso value.
   *
   * @param iso iso value
   */
  public void setIsovalue(final float iso) {
    this.isoValue = iso;
  }

  /**
   * Returns the horizontal resolution.
   *
   * @return the horizontal resolution
   */
  protected final int resolutionX() {
    return resolutionX;
  }

  /**
   * Returns the vertical resolution.
   *
   * @return the vertical resolution
   */
  protected final int resolutionY() {
    return resolutionY;
  }

  /**
   * Returns the horizontal step.
   *
   * @return the horizontal step
   */
  protected final float stepX() {
    return stepX;
  }

  /**
   * Returns the vertical step.
   *
   * @return the vertical step
   */
  protected final float stepY() {
    return stepY;
  }

  /**
   * Returns the scalar field.
   *
   * @return the scalar field
   */
  protected final float[] gridValues() {
    return gridValue;
  }

  /**
   * Returns the number of grid samples.
   *
   * @return the sample count
   */
  protected final int gridCount() {
    return gridCount;
  }

  /**
   * Returns the voxel table.
   *
   * @return the voxel table
   */
  protected final int[] voxels() {
    return voxel;
  }

  /**
   * Returns the edge vertices.
   *
   * @return the edge vertices
   */
  protected final EdgeVertex[] edgeVertices() {
    return edgeVertices;
  }

  /**
   * Returns the iso value.
   *
   * @return the iso value
   */
  protected final float isoValue() {
    return isoValue;
  }

  /**
   * Returns the line table.
   *
   * @return the line endpoints
   */
  protected final int[] linesToDraw() {
    return lineToDraw;
  }

  /**
   * Returns how many line endpoints are stored.
   *
   * @return the endpoint count
   */
  protected final int lineCount() {
    return lineCount;
  }

  /**
   * Replaces the line endpoint count.
   *
   * @param count new count
   */
  protected final void setLineCount(final int count) {
    lineCount = count;
  }
}
