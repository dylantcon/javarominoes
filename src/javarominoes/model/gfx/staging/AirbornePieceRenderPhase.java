/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx.staging;

import java.awt.Graphics;
import javarominoes.model.GameState;
import javarominoes.model.GridZone;
import javarominoes.model.gfx.TetrominoGraphics;

/**
 *
 * @author dylan
 */
public class AirbornePieceRenderPhase extends AbstractRenderPhase {

  public AirbornePieceRenderPhase(Graphics g, GameState gs, int bPx) {
    super(g, gs, bPx);
  }

  public AirbornePieceRenderPhase(GameState gs, int bPx) {
    super(gs, bPx);
  }

  public AirbornePieceRenderPhase(GameState gs) {
    super(gs);
  }

  @Override
  public void draw() {
    if (gameState == null || gameState.active() == null) {
      return;
    }
    // the override variant positions the piece by its grid coordinates; the
    // 3-arg variant is for positionless previews and draws at the origin
    TetrominoGraphics.Render.drawPiece(graphics, bckPix, gameState.active(), null);
  }

  /**
   * The union of the piece's last and current positions, as banked by
   * TetrominoGraphics.bankLastTetrominoFootprint.
   */
  @Override
  public GridZone debugZone() {
    return TetrominoGraphics.getActivePieceZone();
  }

  @Override
  public int getRenderPhaseId() {
    return RenderPhase.Factory.ID_APRP;
  }

}
