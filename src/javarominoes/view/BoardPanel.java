/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.view;

import javarominoes.model.gfx.TetrominoGraphics;
import javarominoes.model.Board;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javarominoes.GameController;
import javarominoes.model.GameState;
import javarominoes.model.GridZone;
import javarominoes.model.gfx.AbstractAnimatedRenderPhase;
import javarominoes.model.gfx.AbstractRenderPhase;
import javarominoes.model.gfx.LineClearRenderPhase;
import javarominoes.model.gfx.RenderPhase;
import javarominoes.model.gfx.SilhouettePieceRenderPhase;
import javarominoes.model.util.SortedInserter;

/**
 *
 * @author dylan
 */
public class BoardPanel extends JPanel implements ComponentListener {

  // inner class for the grid itself
  private class GridPanel extends JPanel {

    private static final int ANIMATION_TICK_MS = 33;

    private final List<AbstractRenderPhase> queuedRenderPhases = new ArrayList<>();

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

    private final Timer animationTimer
            = new Timer(ANIMATION_TICK_MS, e -> stepAnimations());

    protected GridPanel() {}

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

      // gravity freezes while animations play; the block timer's pause
      // accounting keeps the interrupted TTD interval's duration intact
      controller.holdForAnimation();
      if (!animationTimer.isRunning()) {
        animationTimer.start();
      }
    }

    /**
     * Timer-driven animation pump: repaints live animation zones, retires
     * finished ones, and rebakes/reveals the shifted rows once a line clear
     * finishes. Stops itself (and thaws gravity) when no animations remain.
     */
    private void stepAnimations() {
      boolean anyLive = false;
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
          repaintZone(anim.getZone());
        }
      }

      if (!anyLive) {
        animationTimer.stop();
        controller.releaseAnimationHold();
      }
    }

    private void repaintZone(GridZone gz) {
      if (gz == null) {
        return;
      }
      int bPx = getBlockSize();
      repaint(gz.x * bPx, gz.y * bPx, gz.w * bPx, gz.h * bPx);
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
     * Redraws board background and landed blocks into the static layer,
     * clipped to the given zone (null bakes everything). The full-board draw
     * calls stay cheap because the clip discards work outside the zone, and
     * baking only happens on game events, never per frame.
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
        RenderPhase.Factory.boardRegionRenderPhase(big, gameState, bPx).draw();
        TetrominoGraphics.Render.drawStaticBoardBlocks(big,
                gameState.getBoardState(), bPx);
      } finally {
        big.dispose();
      }
    }

    private int getBlockSize() {
      return Math.max(BoardPanel.this.getHeight() / Board.HEIGHT, 1);
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(getBlockSize() * Board.WIDTH,
              getBlockSize() * Board.HEIGHT);
    }

    /**
     * Bottom-up: static layer (background + landed blocks) first, then the
     * resident phases in ID order (silhouette, airborne piece, animations).
     */
    @Override
    protected void paintComponent(Graphics g) {
      // no super call: the static layer covers every pixel of the panel
      if (staticLayer == null || staticLayer.getWidth() != getWidth()
              || staticLayer.getHeight() != getHeight()) {
        rebuildStaticLayer();
      }
      if (staticLayer != null) {
        g.drawImage(staticLayer, 0, 0, null);
      }

      int bPx = getBlockSize();
      for (AbstractRenderPhase arp : residentRenderPhases) {
        arp.withGraphics(g).withBlockPixels(bPx).draw();
      }
    }
  } // end inner class 1 GridPanel

  private class PaddingPanel extends JPanel {

    protected GridPanel gP;
    GradientPaint mgGrad = new GradientPaint(0, 0,
            new Color(17, 66, 50, 0),
            this.getWidth(),
            this.getHeight(),
            new Color(17, 66, 50, 200)); // moss green

    protected PaddingPanel(GridPanel g) {
      gP = g;
    }

    @Override
    public void paintComponent(Graphics g) {
      g.setColor(Color.YELLOW);
      g.fill3DRect(0, 0, this.getWidth(), this.getHeight(), true);

      // apply a gradient on the borders, use graphics2d
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(mgGrad);
      g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    @Override
    public Dimension getPreferredSize() {
      int availW = (BoardPanel.this.getWidth() - gP.getPreferredSize().width) / 2;
      return new Dimension(Math.max(availW, 0), BoardPanel.this.getHeight());
    }
  } // end inner class 2 PaddingPanel

  private final GridPanel gridPanel;
  private final PaddingPanel[] pads;
  private final GameController controller;
  private final GameState gameState;

  /**
   * Constructor initializes the board and layout.
   *
   * @param controller game controller, source of game state and the descent
   * timer's animation hold
   */
  public BoardPanel(GameController controller) {

    this.controller = controller;
    this.gameState = controller.getGameState();

    this.setLayout(new BorderLayout()); // specify the border layout for padding
    gridPanel = new GridPanel(); // create new JPanel overriding paintComponent
    pads = new PaddingPanel[]{new PaddingPanel(gridPanel), new PaddingPanel(gridPanel)};

    this.add(pads[0], BorderLayout.EAST);
    this.add(gridPanel, BorderLayout.CENTER); // add the grid panel to center
    this.add(pads[1], BorderLayout.WEST);
    addComponentListener(BoardPanel.this);

    gridPanel.addRenderPhase(RenderPhase.Factory.boardRegionRenderPhase(gameState));
  }

  @Override
  public void paintComponent(Graphics g) {
  }

  /**
   * This is used by the GamePanel manager class when a new Piece is created,
   * moved, rotated, placed, or a line is cleared.
   *
   * @param arp
   */
  public void bankRenderPhase(AbstractRenderPhase arp) {
    // update the GridPanel inner class so that the rendering
    // reflects the changes to the block
    gridPanel.addRenderPhase(arp);
  }

  public void dispatchGridPanelRerender() {
    if (gridPanel.queuedRenderPhases.isEmpty()) {
      return;
    }
    gridPanel.consumeRenderPhases();
  }

  @Override
  public void componentResized(ComponentEvent e) {
    // the size-mismatch check in paintComponent rebuilds the static layer
    gridPanel.repaint();
  }

  @Override
  public void componentMoved(ComponentEvent e) {}

  @Override
  public void componentShown(ComponentEvent e) {}

  @Override
  public void componentHidden(ComponentEvent e) {}
}
