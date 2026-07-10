/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.view;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javarominoes.GameController;
import javarominoes.model.GameState;
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
