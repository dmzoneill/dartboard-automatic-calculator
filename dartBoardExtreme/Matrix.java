package dartBoardExtreme;

/**
 * Immutable M-by-N matrix of doubles.
 *
 * <p>Compilation: javac Matrix.java. Execution: java Matrix.
 */
public final class Matrix {
  /** Order of the random demo matrix. */
  private static final int DEMO_ORDER = 5;

  /** Demo matrix entry three. */
  private static final int DEMO_THREE = 3;

  /** Demo matrix entry four. */
  private static final int DEMO_FOUR = 4;

  /** Demo matrix entry six. */
  private static final int DEMO_SIX = 6;

  /** Demo matrix entry nine. */
  private static final int DEMO_NINE = 9;

  /** Pivot value that marks a singular matrix. */
  private static final double SINGULAR_PIVOT = 0.0d;

  /** Number of rows. */
  private final int rowCount;

  /** Number of columns. */
  private final int columnCount;

  /** Row-major entries. */
  private final double[][] data;

  /**
   * Creates a zero matrix.
   *
   * @param rows number of rows
   * @param columns number of columns
   */
  public Matrix(final int rows, final int columns) {
    this.rowCount = rows;
    this.columnCount = columns;
    data = new double[rows][columns];
  }

  /**
   * Copies a rectangular array.
   *
   * @param source source rows
   */
  public Matrix(final double[][] source) {
    rowCount = source.length;
    columnCount = source[0].length;
    this.data = new double[rowCount][columnCount];
    for (int row = 0; row < rowCount; row++) {
      for (int column = 0; column < columnCount; column++) {
        this.data[row][column] = source[row][column];
      }
    }
  }

  /**
   * Copies another matrix.
   *
   * @param source matrix to copy
   */
  private Matrix(final Matrix source) {
    this(source.data);
  }

  /**
   * Creates a random matrix with entries in the unit interval.
   *
   * @param rows number of rows
   * @param columns number of columns
   * @return a random matrix
   */
  public static Matrix random(final int rows, final int columns) {
    final Matrix result = new Matrix(rows, columns);
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        result.data[row][column] = Math.random();
      }
    }
    return result;
  }

  /**
   * Creates an identity matrix.
   *
   * @param size number of rows and columns
   * @return the identity
   */
  public static Matrix identity(final int size) {
    final Matrix result = new Matrix(size, size);
    for (int index = 0; index < size; index++) {
      result.data[index][index] = 1;
    }
    return result;
  }

  /**
   * Swaps two rows.
   *
   * @param first first row
   * @param second second row
   */
  private void swap(final int first, final int second) {
    final double[] temp = data[first];
    data[first] = data[second];
    data[second] = temp;
  }

  /**
   * Returns the transpose.
   *
   * @return the transpose
   */
  public Matrix transpose() {
    final Matrix result = new Matrix(columnCount, rowCount);
    for (int row = 0; row < rowCount; row++) {
      for (int column = 0; column < columnCount; column++) {
        result.data[column][row] = this.data[row][column];
      }
    }
    return result;
  }

  /**
   * Returns the sum of this matrix and {@code other}.
   *
   * @param other matrix to add
   * @return the sum
   */
  public Matrix plus(final Matrix other) {
    return elementWise(other, false);
  }

  /**
   * Returns this matrix minus {@code other}.
   *
   * @param other matrix to subtract
   * @return the difference
   */
  public Matrix minus(final Matrix other) {
    return elementWise(other, true);
  }

  /**
   * Adds or subtracts element by element.
   *
   * @param other the other matrix
   * @param subtract true for subtraction
   * @return the result
   */
  private Matrix elementWise(final Matrix other, final boolean subtract) {
    if (other.rowCount != rowCount || other.columnCount != columnCount) {
      throw new RuntimeException("Illegal matrix dimensions.");
    }
    final Matrix result = new Matrix(rowCount, columnCount);
    for (int row = 0; row < rowCount; row++) {
      for (int column = 0; column < columnCount; column++) {
        final double left = data[row][column];
        final double right = other.data[row][column];
        if (subtract) {
          result.data[row][column] = left - right;
        } else {
          result.data[row][column] = left + right;
        }
      }
    }
    return result;
  }

  /**
   * Reports whether every entry matches {@code other}.
   *
   * @param other matrix to compare
   * @return true when the matrices are identical
   */
  public boolean eq(final Matrix other) {
    if (other.rowCount != rowCount || other.columnCount != columnCount) {
      throw new RuntimeException("Illegal matrix dimensions.");
    }
    for (int row = 0; row < rowCount; row++) {
      for (int column = 0; column < columnCount; column++) {
        if (data[row][column] != other.data[row][column]) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns the product of this matrix and {@code other}.
   *
   * @param other matrix on the right
   * @return the product
   */
  public Matrix times(final Matrix other) {
    if (columnCount != other.rowCount) {
      throw new RuntimeException("Illegal matrix dimensions.");
    }
    final Matrix result = new Matrix(rowCount, other.columnCount);
    for (int row = 0; row < result.rowCount; row++) {
      for (int column = 0; column < result.columnCount; column++) {
        for (int index = 0; index < columnCount; index++) {
          final double product = data[row][index] * other.data[index][column];
          result.data[row][column] += product;
        }
      }
    }
    return result;
  }

  /**
   * Solves this square system for a single right-hand side.
   *
   * @param rhs column vector
   * @return the solution vector
   */
  public Matrix solve(final Matrix rhs) {
    final boolean square = rowCount == columnCount;
    final boolean rowsMatch = rhs.rowCount == columnCount;
    final boolean singleColumn = rhs.columnCount == 1;
    if (!square || !rowsMatch || !singleColumn) {
      throw new RuntimeException("Illegal matrix dimensions.");
    }
    final Matrix factor = new Matrix(this);
    final Matrix vector = new Matrix(rhs);
    for (int pivot = 0; pivot < columnCount; pivot++) {
      int max = pivot;
      for (int row = pivot + 1; row < columnCount; row++) {
        final double candidate = Math.abs(factor.data[row][pivot]);
        final double best = Math.abs(factor.data[max][pivot]);
        if (candidate > best) {
          max = row;
        }
      }
      factor.swap(pivot, max);
      vector.swap(pivot, max);
      if (factor.data[pivot][pivot] == SINGULAR_PIVOT) {
        throw new RuntimeException("Matrix is singular.");
      }
      for (int row = pivot + 1; row < columnCount; row++) {
        final double diagonal = factor.data[pivot][pivot];
        final double entry = factor.data[row][pivot];
        vector.data[row][0] -= vector.data[pivot][0] * entry / diagonal;
      }
      for (int row = pivot + 1; row < columnCount; row++) {
        final double pivotValue = factor.data[pivot][pivot];
        final double scale = factor.data[row][pivot] / pivotValue;
        for (int column = pivot + 1; column < columnCount; column++) {
          factor.data[row][column] -= factor.data[pivot][column] * scale;
        }
        factor.data[row][pivot] = SINGULAR_PIVOT;
      }
    }
    final Matrix solution = new Matrix(columnCount, 1);
    for (int row = columnCount - 1; row >= 0; row--) {
      double total = 0.0d;
      for (int column = row + 1; column < columnCount; column++) {
        total += factor.data[row][column] * solution.data[column][0];
      }
      final double numerator = vector.data[row][0] - total;
      solution.data[row][0] = numerator / factor.data[row][row];
    }
    return solution;
  }

  /** Prints the matrix to standard output. */
  public void show() {
    for (int row = 0; row < rowCount; row++) {
      for (int column = 0; column < columnCount; column++) {
        System.out.printf("%9.4f ", data[row][column]);
      }
      System.out.println();
    }
  }

  /**
   * Exercises the matrix operations.
   *
   * @param args unused
   */
  public static void main(final String[] args) {
    final double[][] seed = {
      {1, 2, DEMO_THREE},
      {DEMO_FOUR, DEMO_ORDER, DEMO_SIX},
      {DEMO_NINE, 1, DEMO_THREE}
    };
    final Matrix seeded = new Matrix(seed);
    seeded.show();
    System.out.println();

    final Matrix randomSquare = Matrix.random(DEMO_ORDER, DEMO_ORDER);
    randomSquare.show();
    System.out.println();

    randomSquare.swap(1, 2);
    randomSquare.show();
    System.out.println();

    final Matrix transposed = randomSquare.transpose();
    transposed.show();
    System.out.println();

    final Matrix identity = Matrix.identity(DEMO_ORDER);
    identity.show();
    System.out.println();

    randomSquare.plus(transposed).show();
    System.out.println();

    transposed.times(randomSquare).show();
    System.out.println();

    final Matrix product = randomSquare.times(transposed);
    final Matrix reverse = transposed.times(randomSquare);
    System.out.println(product.eq(reverse));
    System.out.println();

    final Matrix vector = Matrix.random(DEMO_ORDER, 1);
    vector.show();
    System.out.println();

    final Matrix solution = randomSquare.solve(vector);
    solution.show();
    System.out.println();

    randomSquare.times(solution).show();
  }
}
