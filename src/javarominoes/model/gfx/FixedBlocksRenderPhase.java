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
  }

  @Override
  public int getRenderPhaseId() {
    return RenderPhase.Factory.ID_FBRP;
  }
  
}