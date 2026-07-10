/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
   * Phases that are redrawn on every paint, in render phase ID order. Swing
   * gives no guarantee that pixels survive between paints, so paintComponent
   * must reproduce everything inside the clip each call; the silhouette and
   * airborne piece stay resident (replaced when a newer phase of the same ID is
   * consumed), and animated phases stay resident until finished.
   */
  private final List<AbstractRenderPhase> residentRenderPhases = new ArrayList<>();
  /**
   * Board region background plus all landed blocks, baked once per game event
   * instead of redrawn per frame. Painted unconditionally as the bottom layer
   * of every repaint.
   */
  private BufferedImage staticLayer;
  /**
   * Grey blocks, drawn over the field while the pause menu is up.
   */
  private BufferedImage pauseCurtain;
  private static final Color CURTAIN_BLOCK = new Color(0x9A9A9A);

  /**
   * An outline raised when a phase drew, fading out over its own span. The zone
   * is copied: the phase's own will have moved on by the time this expires.
   */
  private static final class DebugGhost {

    final int phaseId;
    final GridZone zone;
    final long startMillis;
    final int durationMs;

    DebugGhost(int phaseId, GridZone zone, int durationMs) {
      this.phaseId = phaseId;
      this.zone = GridZone.copyOf(zone);
      this.startMillis = System.currentTimeMillis();
      this.durationMs = Math.max(1, durationMs);
    }

    float fade() {
      float p = (System.currentTimeMillis() - startMillis) / (float) durationMs;
      return Math.max(0f, 1f - p);
    }

    boolean isFinished() {
      return fade() <= 0f;
    }

    Rectangle px(int bPx) {
      return new Rectangle(zone.x * bPx, zone.y * bPx, zone.w * bPx, zone.h * bPx);
    }
  }

  /**
   * Live outlines, kept in phase ID order so each keeps a stable dash slot. One
   * per phase: a phase which draws again refreshes its own, rather than piling a
   * second atop it.
   */
  private final List<DebugGhost> debugGhosts = new ArrayList<>();
  private static final int DEBUG_TICK_MS = 33;
  private final Timer debugTimer = new Timer(DEBUG_TICK_MS, e -> stepDebugGhosts());

  /**
   * The phase IDs whose zones were outlined by the last paint, as a bitmask,
   * and the pixels those outlines occupied.
   */
  private int debugVisibleMask = 0;
  private Rectangle debugOutlineBoundsPx;

  // ---- the debug legend, compiled away with the flag
  private static final Font LEGEND_FONT = new Font("Monospaced", Font.BOLD, 11);
  private static final Color LEGEND_BACKDROP = new Color(0, 0, 0, 190);
  private static final int LEGEND_PAD = 6;
  private static final int LEGEND_DOT_PX = 9;
  private static final int LEGEND_MAX_W = 320;
  private static final int LEGEND_MAX_H = 160;

  /**
   * In painter's depth order, which is ID order, so the legend reads bottom
   * layer first.
   */
  private static final int[] PHASE_IDS = {
    RenderPhase.Factory.ID_BRRP,
    RenderPhase.Factory.ID_FBRP,
    RenderPhase.Factory.ID_SPRP,
    RenderPhase.Factory.ID_APRP,
    RenderPhase.Factory.ID_PPRP,
    RenderPhase.Factory.ID_LCRP,};
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
        // its ghost is raised where it truly draws, inside bakeStaticZone
        staticLayer = null; // force a full rebuild on next paint
        repaint();
        break;
      case RenderPhase.Factory.ID_FBRP:
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
        spawnDebugGhost(arp.getRenderPhaseId(), gz, TetrominoGraphics.DEBUG_GHOST_MS);
        repaintZone(gz);
        break;
      case RenderPhase.Factory.ID_APRP:
        gz = TetrominoGraphics.getActivePieceZone();
        makeSoleResident(arp);
        spawnDebugGhost(arp.getRenderPhaseId(), gz, TetrominoGraphics.DEBUG_GHOST_MS);
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
    spawnDebugGhost(anim.getRenderPhaseId(), anim.getZone(), anim.durationMs());
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
   * footprint is the union of the piece's last and current positions, so a
   * piece travelling steadily leaves each footprint's trailing edge outside the
   * next one -- and an outline drawn on that trailing edge is beyond the reach
   * of any repaint clipped to the newer zone. Sweeping the previous outline's
   * bounds in alongside the new zone erases them, and costs a rectangle
   * union.</p>
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
    spawnBakeGhosts(gz);
  }

  /**
   * Raises an outline for a phase which has just drawn, replacing whatever that
   * phase last raised. A phase drawing repeatedly, as the airborne piece does,
   * keeps a single outline perpetually renewed rather than a trail of them.
   */
  private void spawnDebugGhost(int phaseId, GridZone z, int durationMs) {
    if (!TetrominoGraphics.DEBUG_RENDER_PHASES || z == null) {
      return;
    }
    debugGhosts.removeIf(gh -> gh.phaseId == phaseId);
    debugGhosts.add(new DebugGhost(phaseId, z, durationMs));
    debugGhosts.sort((p, q) -> p.phaseId - q.phaseId); // stable dash slots
    if (!debugTimer.isRunning()) {
      debugTimer.start();
    }
  }

  /**
   * The bake is where BoardRegionRenderPhase and FixedBlocksRenderPhase truly
   * run, and it is the only place they run: neither ever draws to the screen.
   * Once at the first paint, once per landing, once per resize, over the zone
   * being baked and no more of the board than that.
   */
  private void spawnBakeGhosts(GridZone gz) {
    if (!TetrominoGraphics.DEBUG_RENDER_PHASES) {
      return;
    }
    GridZone z = (gz != null) ? gz : GridZone.Factory.rowBand(0, Board.HEIGHT - 1);
    spawnDebugGhost(RenderPhase.Factory.ID_BRRP, z, TetrominoGraphics.DEBUG_GHOST_MS);
    spawnDebugGhost(RenderPhase.Factory.ID_FBRP, z, TetrominoGraphics.DEBUG_GHOST_MS);
  }

  /**
   * Ages the outlines, retires those spent, and dirties what they occupied so
   * the pixels they leave behind are painted over. Stops itself once none
   * remain, and so costs nothing while the board is still.
   */
  private void stepDebugGhosts() {
    Rectangle dirty = debugOutlineBoundsPx; // where they were last drawn
    boolean anyLive = false;

    Iterator<DebugGhost> it = debugGhosts.iterator();
    while (it.hasNext()) {
      if (it.next().isFinished()) {
        it.remove();
      } else {
        anyLive = true;
      }
    }

    int bPx = getBlockSize();
    for (DebugGhost gh : debugGhosts) {
      Rectangle r = gh.px(bPx);
      dirty = (dirty == null) ? r : dirty.union(r);
    }
    if (dirty != null) {
      repaint(dirty.x, dirty.y, dirty.width, dirty.height);
    }
    if (!anyLive) {
      debugTimer.stop();
    }
  }

  /**
   * Draws every live outline atop everything else, dimming each by its age, and
   * records which phases they were so the legend may caption them.
   *
   * <p>
   * Drawn here rather than from each phase's own draw(): the static phases do
   * not draw to the screen at all, the outlines belong above the pieces they
   * describe, and one place is the only place which can know the whole set --
   * which is what the dash period is reckoned from.</p>
   */
  private void drawDebugOverlay(Graphics g, int bPx) {
    if (!TetrominoGraphics.DEBUG_RENDER_PHASES) {
      return;
    }
    int mask = 0;
    int slot = 0;
    int live = debugGhosts.size();
    Rectangle bounds = null;

    for (DebugGhost gh : debugGhosts) {
      Rectangle r = TetrominoGraphics.Render.outlineZone__Debug(g, bPx, gh.phaseId,
              gh.zone, gh.fade(), slot++, live);
      if (r != null) {
        mask |= gh.phaseId;
        bounds = (bounds == null) ? r : bounds.union(r);
      }
    }

    debugOutlineBoundsPx = bounds;
    if (mask != debugVisibleMask) {
      debugVisibleMask = mask;
      repaintDebugLegend(); // the caption follows what is on the grid
    }
  }

  /**
   * Dirties the corner the legend occupies, so a change in the set of outlined
   * phases reaches the caption even when no zone happens to overlap it.
   */
  private void repaintDebugLegend() {
    if (TetrominoGraphics.DEBUG_RENDER_PHASES) {
      repaint(0, 0, LEGEND_MAX_W, LEGEND_MAX_H);
    }
  }

  /**
   * A key to the outlines on the grid: one dot per phase, in the phase's own
   * colour, against its name. Only the phases whose zones the paint just now
   * outlined appear, so the rows come and go with the board -- the line clear's
   * entry lasts exactly as long as its animation.
   *
   * <p>
   * Drawn by the grid rather than by BoardPanel, though the corner it occupies
   * is BoardPanel's too. GridPanel is opaque, so Swing takes it for the root of
   * any repaint issued against it, and paints it without ever descending through
   * its parent. A legend painted by the parent would be scrubbed away by the
   * next zone repaint which touched the pixels beneath it. A component must own
   * every pixel it paints, exactly as a zone must.</p>
   */
  private void drawDebugLegend(Graphics g) {
    if (!TetrominoGraphics.DEBUG_RENDER_PHASES || debugVisibleMask == 0) {
      return;
    }
    Graphics2D g2d = (Graphics2D) g.create();
    try {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setFont(LEGEND_FONT);
      FontMetrics fm = g2d.getFontMetrics();

      int rows = 0;
      int textW = 0;
      for (int id : PHASE_IDS) {
        if ((debugVisibleMask & id) != 0) {
          ++rows;
          textW = Math.max(textW, fm.stringWidth(TetrominoGraphics.Render.debugNameFor(id)));
        }
      }
      int rowH = Math.max(fm.getHeight(), LEGEND_DOT_PX + 2);
      int boxW = LEGEND_PAD * 3 + LEGEND_DOT_PX + textW;
      int boxH = LEGEND_PAD * 2 + rows * rowH;

      g2d.setColor(LEGEND_BACKDROP);
      g2d.fillRect(LEGEND_PAD, LEGEND_PAD, boxW, boxH);

      int y = LEGEND_PAD * 2;
      for (int id : PHASE_IDS) {
        if ((debugVisibleMask & id) == 0) {
          continue;
        }
        int cy = y + (rowH - LEGEND_DOT_PX) / 2;
        g2d.setColor(TetrominoGraphics.Render.debugColorFor(id));
        g2d.fillOval(LEGEND_PAD * 2, cy, LEGEND_DOT_PX, LEGEND_DOT_PX);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(LEGEND_PAD * 2, cy, LEGEND_DOT_PX, LEGEND_DOT_PX);

        g2d.setColor(Color.WHITE);
        g2d.drawString(TetrominoGraphics.Render.debugNameFor(id),
                LEGEND_PAD * 2 + LEGEND_DOT_PX + LEGEND_PAD, y + fm.getAscent());
        y += rowH;
      }
    } finally {
      g2d.dispose();
    }
  }

  /**
   * A curtain of featureless grey blocks, drawn in place of the board while the
   * pause menu is up. The original game hides the field for the same reason: a
   * paused board is a board the player may study at leisure.
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
        if (debugVisibleMask != 0 || !debugGhosts.isEmpty()) {
          debugVisibleMask = 0; // nothing of the field is on view to caption
          debugOutlineBoundsPx = null;
          debugGhosts.clear();
          debugTimer.stop();
          repaintDebugLegend();
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
    drawDebugLegend(g);
  }

} // end inner class 1 GridPanel
