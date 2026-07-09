/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx;

import java.awt.Graphics;
import javarominoes.model.GameState;

/**
 *
 * @author dylan
 */
public abstract class AbstractRenderPhase implements RenderPhase, 
        Comparable<AbstractRenderPhase> {

  protected Graphics graphics;
  protected final GameState gameState;
  protected int bckPix;

  public AbstractRenderPhase(Graphics g, GameState gs, int bPx) {
    graphics = g;
    gameState = gs;
    bckPix = bPx;
  }

  public AbstractRenderPhase(GameState gs, int bPx) {
    graphics = null;
    gameState = gs;
    bckPix = bPx;
  }

  public AbstractRenderPhase(GameState gs) {
    graphics = null;
    gameState = gs;
    bckPix = -1;
  }

  public AbstractRenderPhase withGraphics(Graphics g) {
    graphics = g;
    return this;
  }
  
  public AbstractRenderPhase withBlockPixels(int bPx) {
    bckPix = bPx;
    return this;
  }

  public Graphics getGraphics() {
    return this.graphics;
  }

  public GameState getGameState() {
    return this.gameState;
  }

  public int getBlockPixels() {
    return this.bckPix;
  }

  @Override
  public abstract int getRenderPhaseId();

  @Override
  public int compareTo(AbstractRenderPhase o) {
    return compare(o);
  }

  protected int compare(AbstractRenderPhase o) {
    return getRenderPhaseId() - o.getRenderPhaseId();
  }

  @Override
  public abstract void draw();
}
