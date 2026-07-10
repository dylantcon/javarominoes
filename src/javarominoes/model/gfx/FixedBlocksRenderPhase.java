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
public class FixedBlocksRenderPhase extends AbstractRenderPhase {

  public FixedBlocksRenderPhase(Graphics g, GameState gs, int bPx) {
    super(g, gs, bPx);
  }

  public FixedBlocksRenderPhase(GameState gs, int bPx) {
    super(gs, bPx);
  }

  public FixedBlocksRenderPhase(GameState gs) {
    super(gs);
  }

  @Override
  public void draw() {
    TetrominoGraphics.Render.drawStaticBoardBlocks(graphics,
            gameState.getBoardState(), bckPix);
    TetrominoGraphics.Render.outlinePhase__Debug(graphics, bckPix, this);
  }

  /**
   * The union of every static zone awaiting a bake. Peeked rather than drained:
   * GridPanel is about to drain them itself, and asks for this beforehand.
   *
   * @return the region the next bake will touch, or null when none is queued
   */
  @Override
  public GridZone debugZone() {
    GridZone union = null;
    for (GridZone z : TetrominoGraphics.peekDirtyStaticZones()) {
      if (union == null) {
        union = GridZone.copyOf(z); // scaleToContain mutates; never the original
      } else {
        union.scaleToContain(z);
      }
    }
    return union;
  }

  @Override
  public int getRenderPhaseId() {
    return RenderPhase.Factory.ID_FBRP;
  }
  
}