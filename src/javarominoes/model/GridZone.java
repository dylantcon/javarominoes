/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import javarominoes.GameController;
import javarominoes.model.util.Pair;

/**
 *
 * @author dylan
 */
public class GridZone {

  public int x, y, w, h;

  protected GridZone(int x, int y, int w, int h) {
    setGeometry(x, y, w, h);
  }

  protected final GridZone setX(int x) {
    this.x = x;
    return this;
  }

  protected final GridZone setY(int y) {
    this.y = y;
    return this;
  }

  protected final GridZone setW(int w) {
    this.w = w;
    return this;
  }

  protected final GridZone setH(int h) {
    this.h = h;
    return this;
  }

  protected final GridZone setGeometry(int x, int y, int w, int h) {
    return setX(x).setY(y).setW(w).setH(h);
  }

  protected final GridZone setGeometry(Pair<Integer, Integer> c,
          Pair<Integer, Integer> d) {
    return setCoordinates(c).setDimensions(d);

  }

  public GridZone translateX(int dx) {
    this.x += dx;
    return this;
  }

  public GridZone translateY(int dy) {
    this.y += dy;
    return this;
  }

  public GridZone scaleW(int dW) {
    this.w += dW;
    return this;
  }

  public GridZone scaleH(int dH) {
    this.h += dH;
    return this;
  }

  public GridZone translateCoordinates(Pair<Integer, Integer> coordDuple) {
    return this.translateX(coordDuple.f).translateY(coordDuple.s);
  }

  public GridZone translateDimensions(Pair<Integer, Integer> dimDuple) {
    return this.scaleW(dimDuple.f).scaleH(dimDuple.s);
  }

  public GridZone setCoordinates(Pair<Integer, Integer> coordDuple) {
    return this.setX(coordDuple.f).setY(coordDuple.s);
  }

  public GridZone setDimensions(Pair<Integer, Integer> dimDuple) {
    return this.setW(dimDuple.f).setH(dimDuple.s);
  }

  /**
   * scaleToContain mutates its receiver, so anything unioning a zone it does
   * not own must union a copy of it instead.
   *
   * @param gz a zone, or null
   * @return an independent zone with the same geometry, or null
   */
  public static GridZone copyOf(GridZone gz) {
    return gz == null ? null : new GridZone(gz.x, gz.y, gz.w, gz.h);
  }

  public GridZone scaleToContain(GridZone gz) {
    int x1 = Math.min(x, gz.x);
    int y1 = Math.min(y, gz.y);

    int x2 = Math.max(x + w, gz.x + gz.w);
    int y2 = Math.max(y + h, gz.y + gz.h);

    x = x1;
    y = y1;
    w = x2 - x1;
    h = y2 - y1;

    return this;
  }
  
  public static Integer[] coordinateTransform(GridZone dom, GridZone codom) {
    // find the bounding box intersection

    int x1 = Math.min(dom.x, codom.x);
    int y1 = Math.min(dom.y, codom.y);
    int x2 = Math.max(dom.x + dom.w, codom.x + codom.w);
    int y2 = Math.max(dom.y + dom.h, codom.y + codom.h);

    int boundW = Math.max(0, x2 - x1 + 1);
    int boundH = Math.max(0, y2 - y1 + 1);

    // calculate shrinkage
    int dX = x1 - dom.x;
    int dY = y1 - dom.y;
    int dW = dom.w - boundW;
    int dH = dom.h - boundH;

    return new Integer[] {dX, dY, dW, dH};
  }

  public static GridZone boundingBox(int[][] pmxs, int x, int y) {
    if (pmxs == null || pmxs.length == 0 || pmxs[0].length == 0) {
      return null;
    }

    int x1 = Integer.MAX_VALUE, y1 = Integer.MAX_VALUE;
    int x2 = Integer.MIN_VALUE, y2 = Integer.MIN_VALUE;
    boolean nonEmpty = false;

    for (int j = 0; j < pmxs.length; ++j) {
      for (int i = 0; i < pmxs[j].length; ++i) {
        if (pmxs[j][i] != Board.POS_FREE) {
          nonEmpty = true;
          x1 = Math.min(i + x, x1);
          x2 = Math.max(i + x, x2);
          y1 = Math.min(j + y, y1);
          y2 = Math.max(j + y, y2);
        }
      }
    }
    int boundW = x2 - x1 + 1;
    int boundH = y2 - y1 + 1;

    return !nonEmpty ? null : new GridZone(x1, y1, boundW, boundH);
  }

  public static GridZone boundingBox(int[][] pmxs, Pair<Integer, Integer> offset) {
    return boundingBox(pmxs, offset.f, offset.s);
  }

  /**
   *
   *
   * @param pmxs a grid containing piece matrix data, to be wrapped in a bbox
   *
   * @return a GridZone object that wraps the matrix data provided. must use a
   * translation, as this assumes that [0][0] is the spatial origin of the
   */
  public static GridZone boundingBox(int[][] pmxs) {
    return boundingBox(pmxs, 0, 0);
  }

  public static GridZone pieceMatrixGrid() {
    return new GridZone(0, 0, Board.PIECE_BLOCKS, Board.PIECE_BLOCKS);
  }

  @Override
  public String toString() {
    return String.format("x=%d,y=%d,w=%d,h=%d", x, y, w, h);
  }

  /**
   * For determining the render-able areas dirtied by a piece, including
   * movement while airborne and placement onto the game grid.
   *
   * @author dylan
   */
  public static class Factory {

    private static GameController c;

    public static final Pair<Integer, Integer> PMATRIX_DIMS
            = new Pair<>(Board.PIECE_BLOCKS, Board.PIECE_BLOCKS);

    private static final int OFFSET = 1;

    private static boolean validController() {
      if (c == null || c.getBoard() == null) {
        return false;
      }
      return c.piece() != null;
    }

    public static void assignController(GameController ctrl) {
      c = ctrl;
    }

    /**
     * Handles piece placement scenarios by producing a list of the dirtied grid
     * zones; this case is specific to landings which do not clear any lines.
     *
     * <p>
     * Placements affect both the static board blocks, and the airborne
     * player-controlled tetromino. The area where a piece lands must be
     * dirtied, as well as the upper region where a new piece is spawning.
     * Finally, placements can cause line clears, which means the method must
     * identify the specific piece placed and the number of lines that it
     * cleared.</p>
     *
     * @author dylan
     * @param ts The current tetromino state
     * @return
     */
    public static ArrayList<GridZone> dirtiedByLanding(TetrominoState ts) {
      if (!validController()) {
        return null;
      }
      ArrayList<GridZone> affectedZones = new ArrayList<>();

      GridZone landing = boundingBox(Pieces.MATRIX[ts.tyRot.f][ts.tyRot.s], ts.xy);
      if (landing != null) {
        affectedZones.add(landing);
      }
      return affectedZones;
    }

    /**
     * @param top index of the topmost affected row
     * @param btm index of the bottommost affected row, inclusive
     * @return a full-width GridZone spanning rows top through btm
     */
    public static GridZone rowBand(int top, int btm) {
      return new GridZone(0, top, Board.WIDTH, btm - top + 1);
    }

    /**
     * Handles piece placement scenarios by producing a list of the dirtied grid
     * zones; this case is specific to landings which do clear lines.
     *
     * <p>
     * Placements affect both the static board blocks, and the airborne
     * player-controlled tetromino. The area where a piece lands must be
     * dirtied, as well as the upper region where a new piece is spawning.
     * Finally, placements can cause line clears, which means the method must
     * identify the specific piece placed and the number of lines that it
     * cleared.</p>
     *
     * @author dylan
     * @param ts The current tetromino state
     * @param t The index of the top of the row being cleared
     * @param b
     * @return
     */
    public static GridZone dirtiedByLineClear(TetrominoState ts, int t, int b) {
      return rowBand(t, b);
    }

    /**
     * Handles piece movement scenarios by producing a pair of GridZone objects
     * that represent the "in" tetromino, and its silhouette. This captures area
     * which has POS_FREE and can be occupied by a falling block or its shadow.
     *
     * <p>
     * Movement of a piece is consequential to two distinct regions of the
     * board: (1) Free blocks in the vicinity of the active (falling) piece's
     * 5x5 piece matrix, which are either partially or wholly contained by the
     * piece matrix prior to a render, and (2) the region that contains the
     * piece's projected silhouette, depicted at the y position that will result
     * in collision with other pieces, or the bottom of the game grid.</p>
     *
     * A new piece spawning at its specified position with offset applied does
     * not constitute valid movement in this case. It is considered as spurious,
     * which is why dirtying of the upper spawning region is left to a dedicated
     * method which is called elsewhere.
     *
     * @author
     * @param last
     * @return
     */
    public static Pair<GridZone, GridZone> dirtiedByMovement(TetrominoState last) {
      if (!validController()) {
        return null;
      }
      GridZone airZone, lastSilZone, silZone = null;

      int dX = last != null ? c.piece().xy.f - last.xy.f : 0;
      int dY = last != null ? c.piece().xy.s - last.xy.s : 0;

      int xOff = OFFSET + dX;
      int yOff = OFFSET + dY;

      // coincidentally, number of tetromino types is translation grid size
      int[][] g, lM, cM;
      g = new int[Pieces.N_TETROMINO][Pieces.N_TETROMINO];

      cM = Pieces.MATRIX[c.piece().tyRot.f][c.piece().tyRot.s];
      if (last != null)
        lM = Pieces.MATRIX[last.tyRot.f][last.tyRot.s];
      else
        lM = cM;

      for (int j = 0; j < Board.PIECE_BLOCKS; ++j) {
        for (int i = 0; i < Board.PIECE_BLOCKS; ++i) {
          g[j + yOff][i + xOff] |= lM[j][i];
          g[j + OFFSET][i + OFFSET] |= cM[j][i];
        }
      }
      // now, g contains the union of current and last piece states, with the 
      //  last piece offset from the origin relative to the current piece
      Pair<Integer, Integer> maskCoords;
      maskCoords = offsetCoordinatePair(c.piece().xy, -xOff, -yOff);

      airZone = boundingBox(g, maskCoords); // airZone is now correct

      if (last != null && (!Objects.equals(last.xy.f, c.piece().xy.f) 
              || !Objects.equals(last.tyRot.s, c.piece().tyRot.s))) {
        lastSilZone = boundingBox(lM, c.getBoard().getSilhouetteCoordinates(last));
        silZone = boundingBox(cM, c.getBoard().getSilhouetteCoordinates(c.piece()));

        silZone.scaleToContain(lastSilZone);
      }
      
      return new Pair<>(airZone, silZone);
    }
  }

  private static Pair<Integer, Integer> offsetCoordinatePair(
          Pair<Integer, Integer> coordinates, int dX, int dY) {
    return new Pair<>()
            .withFirst(coordinates.f + dX)
            .withSecond(coordinates.s + dY);
  }
}