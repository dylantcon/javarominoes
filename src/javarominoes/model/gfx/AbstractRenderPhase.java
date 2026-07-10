/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx;

import java.awt.Graphics;
import javarominoes.model.GameState;
import javarominoes.model.GridZone;

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

  /**
   * The region this phase lays claim to, for the debug overlay alone.
   *
   * <p>
   * Distinct from {@link AbstractAnimatedRenderPhase#getZone()}, which is the
   * region an animation dirties every frame and which the pump repaints. A
   * phase may claim a region without dirtying one each frame, as the board
   * region and the fixed blocks do.</p>
   *
   * @author dylan
   * @return the zone, or null where the phase has none to speak of at the
   * moment it is asked
   */
  public GridZone debugZone() {
    return null;
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
