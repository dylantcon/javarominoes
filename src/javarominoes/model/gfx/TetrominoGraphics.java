/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import javarominoes.model.Board;
import javarominoes.model.GridZone;
import javarominoes.model.Pieces;
import javarominoes.model.TetrominoState;
import javarominoes.model.util.Pair;

/**
 *
 * @author dylan
 */
public class TetrominoGraphics {

  private static int OFFSET_X = 0;
  private static int OFFSET_Y = 0;

  private final static Color GOLD = new Color(255, 223, 0);
  private final static Color BRIGHT_ORANGE = new Color(255, 92, 0);
  private final static Color FOOTPRINT_1 = new Color(0x2B00B5);
  private final static Color FOOTPRINT_2 = new Color(0xC0FFEE);
  private final static boolean DEBUG_TETROMINO_GRAPHICS = false;

  /**
   * first item: in-air tetromino GridZone formed from relative union of last
   * and current TetrominoStates, representing the region on the grid that was
   * dirtied. second item: combined GridZone (bounding box) of the current and
   * previous tetromino silhouettes.
   */
  private static Pair<GridZone, GridZone> tetrominoFootprint = new Pair<>();

  // a shared buffer that is accessed by separate render phases
  private static final ArrayList<GridZone> dirtyStaticZones = new ArrayList<>();

  public static final GridZone getActivePieceZone() {
    return tetrominoFootprint.f;
  }

  public static final GridZone getSilhouettePieceZone() {
    return tetrominoFootprint.s;
  }

  public static void bankLastTetrominoFootprint(TetrominoState lastState) {
    tetrominoFootprint = GridZone.Factory.dirtiedByMovement(lastState);
  }

  public static void markLandingDirtyZone(TetrominoState landState) {
    ArrayList<GridZone> zones = GridZone.Factory.dirtiedByLanding(landState);
    if (zones != null) {
      dirtyStaticZones.addAll(zones);
    }
  }

  /**
   * Hands off every accumulated static dirty zone and empties the buffer, so
   * each zone is baked into the static layer exactly once.
   */
  public static ArrayList<GridZone> drainDirtyStaticZones() {
    ArrayList<GridZone> drained = new ArrayList<>(dirtyStaticZones);
    dirtyStaticZones.clear();
    return drained;
  }

  public static void markLineClearDirtyZone(TetrominoState clearState, int t, int b) {
    dirtyStaticZones.add(GridZone.Factory.dirtiedByLineClear(clearState, t, b));
  }

  public static RenderOffsetBuilder offsetNextRender() {
    return new RenderOffsetBuilder();
  }

  private static void clearRenderOffsets() {
    OFFSET_X = OFFSET_Y = 0;
  }

  public static class RenderOffsetBuilder {

    public RenderOffsetBuilder xBy(int px) {
      TetrominoGraphics.OFFSET_X = px;
      return this;
    }

    public RenderOffsetBuilder yBy(int px) {
      TetrominoGraphics.OFFSET_Y = px;
      return this;
    }
  }

  public static class Render {

    public static Color getBlockColor(int num) {
      switch (num) {
        case 1:
          return GOLD;
        case 2:
          return Color.RED;
        case 3:
          return BRIGHT_ORANGE;
        case 4:
          return Color.CYAN;
        case 5:
          return Color.GREEN;
        case 6:
          return Color.MAGENTA;
        case 7:
          return Color.PINK;
        default:
          return Color.WHITE;
      }
    }

    /**
     * @param rp
     */
    public static void drawRenderPhase(RenderPhase rp) {
      rp.draw();
      TetrominoGraphics.clearRenderOffsets();
    }

    /**
     *
     * @param g
     * @param b
     * @param bPx
     */
    public static void drawStaticBoardBlocks(Graphics g, Board b, int bPx) {
      for (int column = 0; column < Board.WIDTH; ++column) {
        for (int row = 0; row < Board.HEIGHT; ++row) {
          if (!b.isFreeBlock(column, row)) {
            g.setColor(getBlockColor(b.getBlockType(column, row)));
            g.fill3DRect(column * bPx, row * bPx, bPx, bPx, true);
          }
        }
      }
    }

    /**
     * 
     * @param g
     * @param b
     * @param bPx
     * @param depthFactor 
     */
    public static void drawTower(Graphics g, Board b, int bPx, float depthFactor) {
      int width = b.getWidth();
      int height = b.getHeight();

      for (int col = 0; col < width; ++col) {
        for (int row = 0; row < height; ++row) {
          if (!b.isFreeBlock(col, row)) {
            int blockType = b.getBlockType(col, row);
            Color baseColor = getBlockColor(blockType);
            Color depthColor = darkenForDepth(baseColor, depthFactor);

            g.setColor(depthColor);
            int x = TetrominoGraphics.OFFSET_X + (col * bPx);
            int y = TetrominoGraphics.OFFSET_Y + (row * bPx);
            g.fill3DRect(x, y, bPx, bPx, true);
          }
        }
      }
      // padding set only affects next render, reset it
      TetrominoGraphics.clearRenderOffsets();
    }

    /**
     * 
     * @param color
     * @param depthFactor
     * @return 
     */
    private static Color darkenForDepth(Color color, float depthFactor) {
      // depthFactor 0 = front (no darkening), 1 = far back (max darkening)
      float brightness = 1.0f - (depthFactor * 0.6f);
      return new Color(
              (int) (color.getRed() * brightness),
              (int) (color.getGreen() * brightness),
              (int) (color.getBlue() * brightness)
      );
    }

   /**
    * gX , gY are block coordinates of the top left of the piece matrix, and it
    * is assumed that the origin of the coordinate system corresponds to 0px,0px
    * on the targeted JComponent's graphics context. Hence, targeted JComponent 
    * is effectively subdivided into contiguous squares, that may or may not be
    * part of a game board. To render a piece without the grid position offset,
    * simply pass gX and gY as zero. pc is the index of the specific piece matrix
    * 
     * @param g
     * @param bPx
     * @param ts
     * @param override
    */
    public static void drawPiece(Graphics g, int bPx, TetrominoState ts, Color override) {
      // must have graphics. assume parameters passed correctly
      if (g == null) {
        return;
      }

      // piece color is deterministic, get it now to prevent excessive lookups
      Color c = (override == null) ? getBlockColor(ts.tyRot.f + 1) : override;

      int xPx, yPx;

      // loop through piece matrix to draw requested piece pc with rotation rot
      for (int x = 0; x < Board.PIECE_BLOCKS; ++x) {
        for (int y = 0; y < Board.PIECE_BLOCKS; ++y) {
          // if the block is not a free position...
          if (Pieces.getBlockType(ts.tyRot.f, ts.tyRot.s, x, y) != Board.POS_FREE) {
            xPx = ((x + ts.xy.f) * bPx) + TetrominoGraphics.OFFSET_X;
            yPx = ((y + ts.xy.s) * bPx) + TetrominoGraphics.OFFSET_Y;
            g.setColor(c);
            g.fill3DRect(xPx, yPx, bPx, bPx, true);
          }
        }
      }
      // padding set only affects the next render. reset it to 0.
      TetrominoGraphics.clearRenderOffsets();

      drawFootprintGridZones__Debug(g, bPx);
    }

    /**
     * Specifically for pieces which do not have a grid position yet, which, at
     * the moment, only describes the preview shown in {@link InfoPanel}
     *
     * @param g
     * @param bPx
     * @param ts
     */
    public static void drawPiece(Graphics g, int bPx, TetrominoState ts) {
      // must have graphics. assume parameters passed correctly
      if (g == null) {
        return;
      }

      int xPx, yPx;
      g.setColor(getBlockColor(ts.tyRot.f + 1));

      // loop through piece matrix to draw requested piece pc with rotation rot
      for (int x = 0; x < Board.PIECE_BLOCKS; ++x) {
        for (int y = 0; y < Board.PIECE_BLOCKS; ++y) {
          // if the block is not a free position...
          if (Pieces.getBlockType(ts.tyRot.f, ts.tyRot.s, x, y) != Board.POS_FREE) {
            xPx = (x * bPx) + TetrominoGraphics.OFFSET_X;
            yPx = (y * bPx) + TetrominoGraphics.OFFSET_Y;
            g.fill3DRect(xPx, yPx, bPx, bPx, true);
          }
        }
      }
      // padding set only affects the next render. reset it to 0.
      TetrominoGraphics.clearRenderOffsets();
    }

    /**
     * 
     * @param g
     * @param bPx 
     */
    public static void drawFootprintGridZones__Debug(Graphics g, int bPx) {
      if (!DEBUG_TETROMINO_GRAPHICS || g == null) {
        return;
      }
      if (tetrominoFootprint == null) {
        return;
      }
      /* by this point, it is guaranteed that all data is present. as a result,
          we may confidently instantiate statically allocated local variables */
      int xPx, yPx, wPx, hPx;
      TetrominoGraphics.offsetNextRender().xBy(-1).yBy(-1);

      if (tetrominoFootprint.s != null) {
        // draw the silhouette's associated grid zone. must do geometry
        xPx = tetrominoFootprint.s.x * bPx + TetrominoGraphics.OFFSET_X;
        yPx = tetrominoFootprint.s.y * bPx + TetrominoGraphics.OFFSET_Y;
        wPx = tetrominoFootprint.s.w * bPx;
        hPx = tetrominoFootprint.s.h * bPx;
        g.setColor(FOOTPRINT_1);
        g.drawRect(xPx, yPx, wPx, hPx);
      }

      if (tetrominoFootprint.f != null) {
        // second, draw the active piece's associated grid zone. same as above
        xPx = tetrominoFootprint.f.x * bPx + TetrominoGraphics.OFFSET_X;
        yPx = tetrominoFootprint.f.y * bPx + TetrominoGraphics.OFFSET_Y;
        wPx = tetrominoFootprint.f.w * bPx;
        hPx = tetrominoFootprint.f.h * bPx;
        g.setColor(FOOTPRINT_2);
        g.drawRect(xPx, yPx, wPx, hPx);
      }

      TetrominoGraphics.clearRenderOffsets();
    }
  }
}
