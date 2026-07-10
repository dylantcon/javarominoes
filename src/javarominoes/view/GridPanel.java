/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javarominoes.model.Board;
import javarominoes.model.GridZone;
import javarominoes.model.gfx.staging.AbstractAnimatedRenderPhase;
import javarominoes.model.gfx.staging.AbstractRenderPhase;
import javarominoes.model.gfx.staging.LineClearRenderPhase;
import javarominoes.model.gfx.staging.RenderPhase;
import javarominoes.model.gfx.staging.SilhouettePieceRenderPhase;
import javarominoes.model.gfx.TetrominoGraphics;
import javarominoes.model.util.SortedInserter;
import javax.swing.JPanel;
import javax.swing.Timer;


/**
 * The traditional game grid that is known, loved, and admired by many. Always
 * kept at a size that is within one block of the exact 1:2 (x:y) aspect ratio 
 * required for perfectly square grid tiles. A white floor fills in what space
 * remains between y=20x-current-block-size and the parent Component's height.
 * 
 * @author dylan
 */
class GridPanel extends JPanel {

  private static final int ANIMATION_TICK_MS = 33;
  final List<AbstractRenderPhase> queuedRenderPhases = new ArrayList<>();
  /**
   * Phases that are redrawn on every paint, in render phase ID order.
   * Swing gives no guarantee that pixels survive between paints, so
   * paintComponent must reproduce everything inside the clip each call;
   * the silhouette and airborne piece stay resident (replaced when a newer
   * phase of the same ID is consumed), and animated phases stay resident
   * until finished.
   */
  private final List<AbstractRenderPhase> residentRenderPhases = new ArrayList<>();
  /**
   * Board region background plus all landed blocks, baked once per game
   * event instead of redrawn per frame. Painted unconditionally as the
   * bottom layer of every repaint.
   */
  private BufferedImage staticLayer;
  /**
   * Grey blocks, drawn over the field while the pause menu is up.
   */
  private BufferedImage pauseCurtain;
  private static final Color CURTAIN_BLOCK = new Color(0x9A9A9A);

  /**
   * The zones the static phases last claimed. They never draw onto the screen,
   * so the overlay draws for them, and redraws every paint rather than baking a
   * box into the layer where no later rebake could reach it.
   */
  private final Map<Integer, GridZone> debugStaticZones = new LinkedHashMap<>();

  /**
   * The phase IDs whose zones were outlined by the last paint, as a bitmask, and
   * the pixels those outlines occupied.
   */
  private int debugVisibleMask = 0;
  private Rectangle debugOutlineBoundsPx;
  private final Timer animationTimer = new Timer(ANIMATION_TICK_MS, e -> stepAnimations());
  private final BoardPanel out;

  protected GridPanel(final BoardPanel outer) {
    this.out = outer;
  }

  protected void addRenderPhase(AbstractRenderPhase rp) {
    SortedInserter.insertInOrder(queuedRenderPhases, rp);
  }

  protected AbstractRenderPhase peekDequeueableRenderPhase() {
    if (queuedRenderPhases.isEmpty()) {
      return null;
    }
    return queuedRenderPhases.get(0);
  }

  /**
   * identify the render phase currently being consumed in order
   */
  protected void consumeRenderPhase() {
    // get first-in-line renderPhase
    AbstractRenderPhase arp = peekDequeueableRenderPhase();
    if (arp == null) {
      return;
    }
    GridZone gz;
    int bPx = getBlockSize();
    arp.withBlockPixels(bPx);
    queuedRenderPhases.remove(arp);
    switch (arp.getRenderPhaseId()) {
      case RenderPhase.Factory.ID_BRRP:
        noteDebugStaticZone(arp);
        staticLayer = null; // force a full rebuild on next paint
        repaint();
        break;
      case RenderPhase.Factory.ID_FBRP:
        // asked before the drain, since the drain is what empties it
        noteDebugStaticZone(arp);
        // bake newly landed blocks into the static layer, zone by zone
        for (GridZone zone : TetrominoGraphics.drainDirtyStaticZones()) {
          bakeStaticZone(zone, bPx);
          repaintZone(zone);
        }
        break;
      case RenderPhase.Factory.ID_SPRP:
        gz = TetrominoGraphics.getSilhouettePieceZone();
        if (gz == null) {
          // no movement footprint recorded (e.g. spawn); use current spot
          gz = ((SilhouettePieceRenderPhase) arp).currentZone();
        }
        makeSoleResident(arp);
        repaintZone(gz);
        break;
      case RenderPhase.Factory.ID_APRP:
        gz = TetrominoGraphics.getActivePieceZone();
        makeSoleResident(arp);
        repaintZone(gz);
        break;
      case RenderPhase.Factory.ID_PPRP:
      case RenderPhase.Factory.ID_LCRP:
        beginAnimation((AbstractAnimatedRenderPhase) arp);
        break;
      default:
        break;
    }
  }

  protected void consumeRenderPhases() {
    while (!queuedRenderPhases.isEmpty()) {
      consumeRenderPhase();
    }
  }

  /**
   * Only one silhouette/airborne phase is meaningful at a time; the newest
   * consumed phase of an ID replaces any resident predecessor.
   */
  private void makeSoleResident(AbstractRenderPhase arp) {
    residentRenderPhases.removeIf(r -> r.getRenderPhaseId() == arp.getRenderPhaseId());
    SortedInserter.insertInOrder(residentRenderPhases, arp);
  }

  private void beginAnimation(AbstractAnimatedRenderPhase anim) {
    anim.begin();
    SortedInserter.insertInOrder(residentRenderPhases, anim);
    repaintZone(anim.getZone());
    // only an animation which says so freezes gravity; the block timer's pause
    // accounting keeps the interrupted TTD interval's duration intact. the
    // placement pulse does not, and plays over a game which carries on
    if (anim.haltsGameplay()) {
      out.controller.holdForAnimation();
    }
    if (!animationTimer.isRunning()) {
      animationTimer.start();
    }
  }

  /**
   * Timer-driven animation pump: repaints live animation zones, retires
   * finished ones, and rebakes/reveals the shifted rows once a line clear
   * finishes.
   *
   * <p>
   * Gravity is thawed the moment no <em>halting</em> animation remains, which
   * is not the same moment the timer stops. A placement pulse may still be
   * fading while the next piece is already descending through it.</p>
   */
  private void stepAnimations() {
    boolean anyLive = false;
    boolean anyHalting = false;

    Iterator<AbstractRenderPhase> it = residentRenderPhases.iterator();
    while (it.hasNext()) {
      AbstractRenderPhase arp = it.next();
      if (!(arp instanceof AbstractAnimatedRenderPhase)) {
        continue;
      }
      AbstractAnimatedRenderPhase anim = (AbstractAnimatedRenderPhase) arp;
      if (anim.isFinished()) {
        it.remove();
        if (anim instanceof LineClearRenderPhase) {
          GridZone rebake = ((LineClearRenderPhase) anim).getRebakeZone();
          bakeStaticZone(rebake, getBlockSize());
          repaintZone(rebake);
        } else {
          repaintZone(anim.getZone());
        }
      } else {
        anyLive = true;
        anyHalting |= anim.haltsGameplay();
        repaintZone(anim.getZone());
      }
    }

    if (!anyHalting) {
      out.controller.releaseAnimationHold(); // a no-op unless a hold is standing
    }
    if (!anyLive) {
      animationTimer.stop();
    }
  }

  /**
   * Dirties the pixels a zone covers, and nothing besides.
   *
   * <p>
   * With the debug overlay up, also the pixels the last paint's outlines
   * occupied. A zone need not contain the zone which preceded it -- a movement
   * footprint is the union of the piece's last and current positions, so a piece
   * travelling steadily leaves each footprint's trailing edge outside the next
   * one -- and an outline drawn on that trailing edge is beyond the reach of any
   * repaint clipped to the newer zone. Sweeping the previous outline's bounds in
   * alongside the new zone erases them, and costs a rectangle union.</p>
   */
  private void repaintZone(GridZone gz) {
    if (gz == null) {
      return;
    }
    int bPx = getBlockSize();
    Rectangle dirty = new Rectangle(gz.x * bPx, gz.y * bPx, gz.w * bPx, gz.h * bPx);
    if (TetrominoGraphics.DEBUG_RENDER_PHASES && debugOutlineBoundsPx != null) {
      dirty = dirty.union(debugOutlineBoundsPx);
    }
    repaint(dirty.x, dirty.y, dirty.width, dirty.height);
  }

  private void rebuildStaticLayer() {
    int w = getWidth();
    int h = getHeight();
    if (w <= 0 || h <= 0) {
      staticLayer = null; // not laid out yet
      return;
    }
    staticLayer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    bakeStaticZone(null, getBlockSize());
  }

  /**
   * Redraws board background and landed blocks into the static layer, clipped
   * to the given zone (null bakes everything). Baking happens only on game
   * events, never per frame, and the clip now bounds the loops rather than only
   * the rasterizer, so a landing's bake is the size of the piece.
   *
   */
  private void bakeStaticZone(GridZone gz, int bPx) {
    if (staticLayer == null) {
      return;
    }
    Graphics2D big = staticLayer.createGraphics();
    try {
      if (gz != null) {
        big.setClip(gz.x * bPx, gz.y * bPx, gz.w * bPx, gz.h * bPx);
      }
      RenderPhase.Factory.boardRegionRenderPhase(big, out.gameState, bPx).draw();
      TetrominoGraphics.Render.drawStaticBoardBlocks(big, out.gameState.getBoardState(), bPx);
    } finally {
      big.dispose();
    }
  }

  /**
   * Remembers what a static phase laid claim to, since the overlay draws for it.
   * BoardRegionRenderPhase and FixedBlocksRenderPhase never reach the screen:
   * they bake into the layer, and a box baked with them could never be erased by
   * a later rebake clipped elsewhere.
   */
  private void noteDebugStaticZone(AbstractRenderPhase arp) {
    if (!TetrominoGraphics.DEBUG_RENDER_PHASES) {
      return;
    }
    GridZone gz = arp.debugZone();
    if (gz != null) {
      debugStaticZones.put(arp.getRenderPhaseId(), gz);
    }
  }

  /**
   * Outlines every zone currently claimed, atop everything else, and records
   * which phases those were so BoardPanel may caption them.
   *
   * <p>
   * Drawn here rather than from each phase's own draw(): the static phases do
   * not draw to the screen at all, the outlines belong above the pieces they
   * describe, and one place is the only place that can know the whole set.</p>
   */
  private void drawDebugOverlay(Graphics g, int bPx) {
    if (!TetrominoGraphics.DEBUG_RENDER_PHASES) {
      return;
    }
    int mask = 0;
    Rectangle bounds = null;

    for (Map.Entry<Integer, GridZone> e : debugStaticZones.entrySet()) {
      Rectangle r = TetrominoGraphics.Render.outlineZone__Debug(g, bPx, e.getKey(), e.getValue());
      if (r != null) {
        mask |= e.getKey();
        bounds = (bounds == null) ? r : bounds.union(r);
      }
    }
    for (AbstractRenderPhase arp : residentRenderPhases) {
      Rectangle r = TetrominoGraphics.Render.outlineZone__Debug(g, bPx,
              arp.getRenderPhaseId(), arp.debugZone());
      if (r != null) {
        mask |= arp.getRenderPhaseId();
        bounds = (bounds == null) ? r : bounds.union(r);
      }
    }

    debugOutlineBoundsPx = bounds;
    if (mask != debugVisibleMask) {
      debugVisibleMask = mask;
      out.repaintDebugLegend(); // the caption follows what is on the grid
    }
  }

  /**
   * @return the bits of the phases whose zones the last paint outlined
   */
  int debugVisiblePhaseMask() {
    return debugVisibleMask;
  }

  /**
   * A curtain of featureless grey blocks, drawn in place of the board while the
   * pause menu is up. The original game hides the field for the same reason:
   * a paused board is a board the player may study at leisure.
   *
   * <p>
   * Baked once per size. It never changes, having nothing to say about the
   * board it conceals.</p>
   */
  private void rebuildPauseCurtain() {
    int w = getWidth();
    int h = getHeight();
    if (w <= 0 || h <= 0) {
      pauseCurtain = null;
      return;
    }
    int bPx = getBlockSize();
    pauseCurtain = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D cg = pauseCurtain.createGraphics();
    try {
      RenderPhase.Factory.boardRegionRenderPhase(cg, out.gameState, bPx).draw();
      cg.setColor(CURTAIN_BLOCK);
      for (int col = 0; col < Board.WIDTH; ++col) {
        for (int row = 0; row < Board.HEIGHT; ++row) {
          cg.fill3DRect(col * bPx, row * bPx, bPx, bPx, true);
        }
      }
    } finally {
      cg.dispose();
    }
  }

  private boolean curtainIsStale() {
    return pauseCurtain == null
            || pauseCurtain.getWidth() != getWidth()
            || pauseCurtain.getHeight() != getHeight();
  }

  private int getBlockSize() {
    return Math.max(out.getHeight() / Board.HEIGHT, 1);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(getBlockSize() * Board.WIDTH, getBlockSize() * Board.HEIGHT);
  }

  /**
   * Bottom-up: static layer (background + landed blocks) first, then the
   * resident phases in ID order (silhouette, airborne piece, animations), then
   * the debug overlay atop the lot.
   */
  @Override
  protected void paintComponent(Graphics g) {
    // a paused board is concealed outright: neither the static layer nor the
    // resident phases are drawn, so nothing of the field survives the curtain
    if (out.controller.isPaused()) {
      if (curtainIsStale()) {
        rebuildPauseCurtain();
      }
      if (pauseCurtain != null) {
        g.drawImage(pauseCurtain, 0, 0, null);
        if (debugVisibleMask != 0) {
          debugVisibleMask = 0; // nothing of the field is on view to caption
          debugOutlineBoundsPx = null;
          out.repaintDebugLegend();
        }
        return;
      }
    }

    // no super call: the static layer covers every pixel of the panel
    if (staticLayer == null || staticLayer.getWidth() != getWidth() || staticLayer.getHeight() != getHeight()) {
      rebuildStaticLayer();
    }
    if (staticLayer != null) {
      g.drawImage(staticLayer, 0, 0, null);
    }
    int bPx = getBlockSize();
    for (AbstractRenderPhase arp : residentRenderPhases) {
      arp.withGraphics(g).withBlockPixels(bPx).draw();
    }
    drawDebugOverlay(g, bPx);
  }
  
} // end inner class 1 GridPanel