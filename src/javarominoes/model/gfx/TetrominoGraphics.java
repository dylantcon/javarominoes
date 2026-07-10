/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
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
  /**
   * One colour per render phase, so that a screenshot says which phase laid
   * claim to which region. The two originals are kept for the silhouette and
   * the airborne piece.
   */
  private final static Color FOOTPRINT_SPRP = new Color(0x2B00B5);
  private final static Color FOOTPRINT_APRP = new Color(0xC0FFEE);
  private final static Color FOOTPRINT_BRRP = new Color(0x8A2BE2);
  private final static Color FOOTPRINT_FBRP = new Color(0x00B5FF);
  private final static Color FOOTPRINT_PPRP = new Color(0xFFAA00);
  private final static Color FOOTPRINT_LCRP = new Color(0xFF0055);

  /**
   * Outlines every render phase's GridZone. Costs nothing when false: each
   * phase asks for its zone only after this has been consulted.
   */
  public final static boolean DEBUG_RENDER_PHASES = false;

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

  /**
   * The pending static zones, without consuming them. Only the debug overlay
   * of FixedBlocksRenderPhase wants to look without taking.
   */
  public static ArrayList<GridZone> peekDirtyStaticZones() {
    return new ArrayList<>(dirtyStaticZones);
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
      Rectangle cells = clippedCells(g, bPx);
      for (int column = cells.x; column < cells.x + cells.width; ++column) {
        for (int row = cells.y; row < cells.y + cells.height; ++row) {
          if (!b.isFreeBlock(column, row)) {
            g.setColor(getBlockColor(b.getBlockType(column, row)));
            g.fill3DRect(column * bPx, row * bPx, bPx, bPx, true);
          }
        }
      }
    }

    /**
     * The board cells which the context's clip is able to show, as a rectangle
     * in cell coordinates rather than pixels.
     *
     * <p>
     * Setting a clip and then drawing the whole board is not free. Java2D
     * discards the pixels which fall outside the clip, but only once every
     * fill3DRect has been issued, and each of those is five primitives. Under
     * CheerpJ, where AWT is backed by an HTML canvas, an issued primitive costs
     * a crossing out of WebAssembly whether or not it lands anywhere.</p>
     *
     * <p>
     * Bounding the loop rather than the rasterizer makes a landing's bake
     * proportional to the piece's footprint instead of to the whole board.</p>
     *
     * @author dylan
     * @param g a context whose clip is either the zone being baked, or the
     * whole surface
     * @param bPx block size in pixels
     * @return the columns and rows worth visiting; the full board when there is
     * no clip to speak of
     */
    static Rectangle clippedCells(Graphics g, int bPx) {
      Rectangle clip = (g == null || bPx <= 0) ? null : g.getClipBounds();
      if (clip == null) {
        return new Rectangle(0, 0, Board.WIDTH, Board.HEIGHT);
      }
      int c0 = Math.max(0, Math.floorDiv(clip.x, bPx));
      int r0 = Math.max(0, Math.floorDiv(clip.y, bPx));
      int c1 = Math.min(Board.WIDTH - 1, Math.floorDiv(clip.x + clip.width - 1, bPx));
      int r1 = Math.min(Board.HEIGHT - 1, Math.floorDiv(clip.y + clip.height - 1, bPx));

      return new Rectangle(c0, r0, Math.max(0, c1 - c0 + 1), Math.max(0, r1 - r0 + 1));
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
    private static Color debugColorFor(int phaseId) {
      switch (phaseId) {
        case RenderPhase.Factory.ID_BRRP:
          return FOOTPRINT_BRRP;
        case RenderPhase.Factory.ID_FBRP:
          return FOOTPRINT_FBRP;
        case RenderPhase.Factory.ID_SPRP:
          return FOOTPRINT_SPRP;
        case RenderPhase.Factory.ID_APRP:
          return FOOTPRINT_APRP;
        case RenderPhase.Factory.ID_PPRP:
          return FOOTPRINT_PPRP;
        case RenderPhase.Factory.ID_LCRP:
          return FOOTPRINT_LCRP;
        default:
          return Color.WHITE;
      }
    }

    /**
     * Outlines a zone in its phase's colour, one pixel outside the blocks it
     * covers, so that the outline never hides the thing it describes.
     *
     * @author dylan
     * @param g the surface the phase drew onto
     * @param bPx block size in pixels
     * @param phaseId the phase which claimed the zone, for its colour
     * @param z the zone, or null to draw nothing
     */
    public static void outlineZone__Debug(Graphics g, int bPx, int phaseId, GridZone z) {
      if (!DEBUG_RENDER_PHASES || g == null || z == null || bPx <= 0) {
        return;
      }
      // one pixel outside the blocks, then pulled back onto the surface: a zone
      // flush with the board's edge would otherwise outline itself off it
      int x0 = (z.x * bPx) - 1;
      int y0 = (z.y * bPx) - 1;
      int x1 = x0 + (z.w * bPx);
      int y1 = y0 + (z.h * bPx);

      Rectangle clip = g.getClipBounds();
      if (clip != null) {
        x0 = Math.max(x0, clip.x);
        y0 = Math.max(y0, clip.y);
        x1 = Math.min(x1, clip.x + clip.width - 1);
        y1 = Math.min(y1, clip.y + clip.height - 1);
      }
      if (x1 <= x0 || y1 <= y0) {
        return;
      }
      g.setColor(debugColorFor(phaseId));
      g.drawRect(x0, y0, x1 - x0, y1 - y0);
    }

    /**
     * The form every phase calls at the end of its own draw(). The zone is
     * asked for only once the flag is known to be set, so a release build pays
     * a branch and nothing more.
     *
     * @author dylan
     * @param g the surface the phase drew onto
     * @param bPx block size in pixels
     * @param phase the phase which has just drawn itself
     */
    public static void outlinePhase__Debug(Graphics g, int bPx, AbstractRenderPhase phase) {
      if (!DEBUG_RENDER_PHASES || phase == null) {
        return;
      }
      outlineZone__Debug(g, bPx, phase.getRenderPhaseId(), phase.debugZone());
    }
  }
}
