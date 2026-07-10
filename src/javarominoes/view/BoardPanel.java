/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.view;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javarominoes.GameController;
import javarominoes.model.GameState;
import javarominoes.model.gfx.TetrominoGraphics;
import javarominoes.model.gfx.staging.AbstractRenderPhase;
import javarominoes.model.gfx.staging.RenderPhase;

/**
 *
 * @author dylan
 */
public class BoardPanel extends JPanel implements ComponentListener {


  private class PaddingPanel extends JPanel {

    protected GridPanel gP;

    private GradientPaint mgGrad;
    private int gradW = -1, gradH = -1;

    protected PaddingPanel(GridPanel g) {
      gP = g;
    }

    /**
     * The gradient spans the panel, so its endpoints are only knowable once
     * the panel has been laid out. Cached until the panel is resized, rather
     * than rebuilt on every paint.
     */
    private GradientPaint gradientFor(int w, int h) {
      if (mgGrad == null || gradW != w || gradH != h) {
        mgGrad = new GradientPaint(0, 0, new Color(17, 66, 50, 0),
                w, h, new Color(17, 66, 50, 200)); // moss green
        gradW = w;
        gradH = h;
      }
      return mgGrad;
    }

    @Override
    public void paintComponent(Graphics g) {
      int w = getWidth();
      int h = getHeight();
      if (w <= 0 || h <= 0) {
        return; // not laid out yet, and a degenerate gradient has no endpoints
      }
      g.setColor(Color.YELLOW);
      g.fill3DRect(0, 0, w, h, true);

      // apply a gradient on the borders, use graphics2d
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(gradientFor(w, h));
      g2d.fillRect(0, 0, w, h);
    }

    @Override
    public Dimension getPreferredSize() {
      int availW = (BoardPanel.this.getWidth() - gP.getPreferredSize().width) / 2;
      return new Dimension(Math.max(availW, 0), BoardPanel.this.getHeight());
    }
  } // end inner class 2 PaddingPanel

  private final GridPanel gridPanel;
  private final PaddingPanel[] pads;
  final GameController controller;
  final GameState gameState;

  // ---- the debug legend, drawn only when TetrominoGraphics has it enabled
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
    gridPanel = new GridPanel(this); // create new JPanel overriding paintComponent
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
   * The legend sits above the padding panel and the grid alike, so it is drawn
   * after the children rather than beneath them.
   */
  @Override
  protected void paintChildren(Graphics g) {
    super.paintChildren(g);
    drawDebugLegend(g);
  }

  /**
   * Dirties the corner the legend occupies. GridPanel calls this when the set of
   * outlined phases changes: a clipped repaint of the grid never reaches this
   * component, so the caption would otherwise stand stale.
   */
  void repaintDebugLegend() {
    if (TetrominoGraphics.DEBUG_RENDER_PHASES) {
      repaint(0, 0, LEGEND_MAX_W, LEGEND_MAX_H);
    }
  }

  /**
   * A key to the outlines currently on the grid: one dot per phase, in its own
   * colour, against its name. Only the phases whose zones the last paint
   * actually outlined appear, so the list grows and shrinks with the board --
   * the line clear's entry, say, comes and goes with the animation.
   */
  private void drawDebugLegend(Graphics g) {
    if (!TetrominoGraphics.DEBUG_RENDER_PHASES) {
      return;
    }
    int mask = gridPanel.debugVisiblePhaseMask();
    if (mask == 0) {
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
        if ((mask & id) != 0) {
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
        if ((mask & id) == 0) {
          continue;
        }
        int cy = y + (rowH - LEGEND_DOT_PX) / 2;
        g2d.setColor(TetrominoGraphics.Render.debugColorFor(id));
        g2d.fillOval(LEGEND_PAD * 2, cy, LEGEND_DOT_PX, LEGEND_DOT_PX);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(LEGEND_PAD * 2, cy, LEGEND_DOT_PX, LEGEND_DOT_PX);

        g2d.setColor(Color.WHITE);
        g2d.drawString(TetrominoGraphics.Render.debugNameFor(id),
                LEGEND_PAD * 2 + LEGEND_DOT_PX + LEGEND_PAD,
                y + fm.getAscent());
        y += rowH;
      }
    } finally {
      g2d.dispose();
    }
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
