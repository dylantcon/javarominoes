/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx;

import javarominoes.model.gfx.staging.RenderPhase;
import javarominoes.model.gfx.staging.AbstractRenderPhase;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
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
   * A dash is this many pixels long. The period is that length times the number
   * of outlines presently on the grid, so that each takes its own slot and none
   * is hidden by another. A lone outline needs no slot, and is drawn solid.
   */
  private final static float DASH_ON = 3f;
  private final static Stroke SOLID = new BasicStroke(1f);

  /**
   * How long a debug outline lingers after the phase which raised it has drawn,
   * fading the while. Animated phases outlive this and use their own duration.
   */
  public final static int DEBUG_GHOST_MS = 450;

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
   * @return 
   */
  public static ArrayList<GridZone> drainDirtyStaticZones() {
    ArrayList<GridZone> drained = new ArrayList<>(dirtyStaticZones);
    dirtyStaticZones.clear();
    return drained;
  }

  /**
   * The pending static zones, without consuming them. Only the debug overlay
   * of FixedBlocksRenderPhase wants to look without taking.
   * @return 
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
    public static Rectangle clippedCells(Graphics g, int bPx) {
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
    public static Color debugColorFor(int phaseId) {
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
     * The human name of a phase, for the legend.
     *
     * @author dylan
     * @param phaseId the phase's bit
     * @return its simple class name, or "?" for a bit no phase claims
     */
    public static String debugNameFor(int phaseId) {
      switch (phaseId) {
        case RenderPhase.Factory.ID_BRRP:
          return "BoardRegionRenderPhase";
        case RenderPhase.Factory.ID_FBRP:
          return "FixedBlocksRenderPhase";
        case RenderPhase.Factory.ID_SPRP:
          return "SilhouettePieceRenderPhase";
        case RenderPhase.Factory.ID_APRP:
          return "AirbornePieceRenderPhase";
        case RenderPhase.Factory.ID_PPRP:
          return "PiecePlacementRenderPhase";
        case RenderPhase.Factory.ID_LCRP:
          return "LineClearRenderPhase";
        default:
          return "?";
      }
    }

    /**
     * A dashed stroke, one phase to each slot of a shared cycle.
     *
     * <p>
     * The zones overlap constantly: the silhouette sits beneath the airborne
     * piece, a placement pulse beneath both. Were every outline solid, the last
     * one drawn would be the only one seen. They instead share a period equal to
     * the dash length times their number, each taking a different offset into
     * it, so that coincident borders lay their dashes in disjoint runs of pixels
     * and every one remains legible.</p>
     *
     * <p>
     * The period shrinks with the count. Two outlines dash every other three
     * pixels; a lone one is solid, there being nothing for it to hide from.</p>
     *
     * @author dylan
     * @param slot this outline's index among those presently drawn
     * @param visibleCount how many are presently drawn
     * @return its stroke
     */
    private static Stroke debugStrokeFor(int slot, int visibleCount) {
      if (visibleCount <= 1) {
        return SOLID; // nothing to interleave with, and a solid box reads best
      }
      float period = DASH_ON * visibleCount;
      return new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
              10f, new float[]{DASH_ON, period - DASH_ON}, DASH_ON * slot);
    }

    /**
     * Outlines a zone in its phase's colour and dash.
     *
     * <p>
     * The line sits on the innermost ring of pixels the zone owns, its outer
     * edge tracing the boundary the zone traces. A zone must own every pixel it
     * paints: an outline drawn one pixel outside would be clipped away on the
     * two leading edges, and left behind, unerasable, on the two trailing ones,
     * since no later zone need contain the zone which preceded it.</p>
     *
     * @author dylan
     * @param g the surface to outline upon
     * @param bPx block size in pixels
     * @param phaseId the phase which claimed the zone, for its colour and dash
     * @param z the zone, or null to draw nothing
     * @param fade 1 when the phase has just drawn, falling to 0 as it ages out
     * @param slot this outline's index among those presently drawn
     * @param visibleCount how many are presently drawn
     * @return the rectangle outlined, in pixels, or null when nothing was drawn
     */
    public static Rectangle outlineZone__Debug(Graphics g, int bPx, int phaseId,
            GridZone z, float fade, int slot, int visibleCount) {
      if (!DEBUG_RENDER_PHASES || g == null || z == null || bPx <= 0) {
        return null;
      }
      if (z.w <= 0 || z.h <= 0 || fade <= 0f) {
        return null;
      }
      Rectangle r = new Rectangle(z.x * bPx, z.y * bPx, z.w * bPx, z.h * bPx);

      Color base = debugColorFor(phaseId);
      int alpha = Math.max(1, Math.min(255, Math.round(255f * fade)));
      g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));

      if (g instanceof Graphics2D) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke prior = g2d.getStroke();
        try {
          g2d.setStroke(debugStrokeFor(slot, visibleCount));
          g2d.drawRect(r.x, r.y, r.width - 1, r.height - 1);
        } finally {
          g2d.setStroke(prior);
        }
      } else {
        g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
      }
      return r;
    }
  }
}
