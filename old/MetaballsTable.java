package old;

/** Marching-squares tables shared by the metaball and blob detectors. */
public final class MetaballsTable {
  /** Sentinel stored where an edge is absent. */
  private static final int NO_EDGE = -1;

  /** Number of square configurations. */
  private static final int SQUARE_COUNT = 16;

  /** Slots stored for each square. */
  private static final int EDGE_SLOTS = 5;

  /** Third edge index. */
  private static final int EDGE_TWO = 2;

  /** Fourth edge index. */
  private static final int EDGE_THREE = 3;

  /** Neighbour bit for Y plus one. */
  private static final int BIT_Y_PLUS = 2;

  /** Neighbour bit for X minus one. */
  private static final int BIT_X_MINUS = 1;

  /** Neighbour bit for Y minus one. */
  private static final int BIT_Y_MINUS = 3;

  /** Edges cut by each square configuration. */
  private static final int[][] EDGE_CUT = {
    {NO_EDGE, NO_EDGE, NO_EDGE, NO_EDGE, NO_EDGE},
    {0, EDGE_THREE, NO_EDGE, NO_EDGE, NO_EDGE},
    {0, 1, NO_EDGE, NO_EDGE, NO_EDGE},
    {EDGE_THREE, 1, NO_EDGE, NO_EDGE, NO_EDGE},
    {1, EDGE_TWO, NO_EDGE, NO_EDGE, NO_EDGE},
    {1, EDGE_TWO, 0, EDGE_THREE, NO_EDGE},
    {0, EDGE_TWO, NO_EDGE, NO_EDGE, NO_EDGE},
    {EDGE_THREE, EDGE_TWO, NO_EDGE, NO_EDGE, NO_EDGE},
    {EDGE_THREE, EDGE_TWO, NO_EDGE, NO_EDGE, NO_EDGE},
    {0, EDGE_TWO, NO_EDGE, NO_EDGE, NO_EDGE},
    {1, EDGE_TWO, 0, EDGE_THREE, NO_EDGE},
    {1, EDGE_TWO, NO_EDGE, NO_EDGE, NO_EDGE},
    {EDGE_THREE, 1, NO_EDGE, NO_EDGE, NO_EDGE},
    {0, 1, NO_EDGE, NO_EDGE, NO_EDGE},
    {0, EDGE_THREE, NO_EDGE, NO_EDGE, NO_EDGE},
    {NO_EDGE, NO_EDGE, NO_EDGE, NO_EDGE, NO_EDGE}
  };

  /** X, Y, and endpoint offsets for each edge. */
  private static final int[][] EDGE_OFFSET_INFO = {
    {0, 0, 0},
    {1, 0, 1},
    {0, 1, 0},
    {0, 0, 1}
  };

  /** Offset component count. */
  private static final int OFFSET_X = 0;

  /** Offset component count. */
  private static final int OFFSET_Y = 1;

  /** Offset component count. */
  private static final int OFFSET_AB = 2;

  /** Edges that still need interpolation for each square. */
  private static final int[] EDGE_TO_COMPUTE = {
    0,
    EDGE_THREE,
    1,
    EDGE_TWO,
    0,
    EDGE_THREE,
    1,
    EDGE_TWO,
    EDGE_TWO,
    1,
    EDGE_THREE,
    0,
    EDGE_TWO,
    1,
    EDGE_THREE,
    0
  };

  /**
   * Neighbour bits for each square.
   *
   * <p>Bit 0 is X plus one, bit 1 is X minus one, bit 2 is Y plus one, and bit
   *
   * <p>3 is Y minus one.
   */
  private static final byte[] NEIGHBOR_VOXEL = {
    0, 10, 9, EDGE_THREE, EDGE_SLOTS, 15, 12, 6,
    6, 12, 12, EDGE_SLOTS, EDGE_THREE, 9, 10, 0
  };

  /** Prevents instantiation of the table holder. */
  private MetaballsTable() {
    // utility
  }

  /**
   * Returns one edge index from a square configuration.
   *
   * @param squareIndex square configuration
   * @param slot position in that row
   * @return the edge index, or -1 when the row is finished
   */
  public static int edgeCutAt(final int squareIndex, final int slot) {
    return EDGE_CUT[squareIndex][slot];
  }

  /**
   * Returns the edges that must be interpolated for a square.
   *
   * @param squareIndex square configuration
   * @return the bit mask of edges
   */
  public static int edgeToComputeAt(final int squareIndex) {
    return EDGE_TO_COMPUTE[squareIndex];
  }

  /**
   * Returns the neighbour mask for a square.
   *
   * @param squareIndex square configuration
   * @return the neighbour bits
   */
  public static byte neighborVoxelAt(final int squareIndex) {
    return NEIGHBOR_VOXEL[squareIndex];
  }

  /**
   * Returns the voxel index for one marching-squares edge.
   *
   * @param v voxel table
   * @param w horizontal resolution
   * @param x square x
   * @param y square y
   * @param e edge index
   * @return the voxel index
   */
  public static int voxelIndexForEdge(
      final int[] v, final int w, final int x, final int y, final int e) {
    final int[] info = EDGE_OFFSET_INFO[e];
    final int offx = info[OFFSET_X];
    final int offy = info[OFFSET_Y];
    final int offAb = info[OFFSET_AB];
    final int column = x + offx;
    final int row = y + offy;
    return v[column + w * row] + offAb;
  }

  /** Rebuilds {@link #NEIGHBOR_VOXEL} from the edge-cut table. */
  public static void computeNeighborTable() {
    for (int square = 0; square < SQUARE_COUNT; square++) {
      NEIGHBOR_VOXEL[square] = 0;
      int slot = 0;
      int edge = EDGE_CUT[square][slot];
      while (edge != NO_EDGE) {
        if (edge == 0) {
          NEIGHBOR_VOXEL[square] |= (byte) (1 << BIT_Y_MINUS);
        } else if (edge == 1) {
          NEIGHBOR_VOXEL[square] |= (byte) (1 << 0);
        } else if (edge == EDGE_TWO) {
          NEIGHBOR_VOXEL[square] |= (byte) (1 << BIT_Y_PLUS);
        } else if (edge == EDGE_THREE) {
          NEIGHBOR_VOXEL[square] |= (byte) (1 << BIT_X_MINUS);
        }
        slot++;
        edge = EDGE_CUT[square][slot];
      }
    }
  }
}
