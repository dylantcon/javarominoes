/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx;

import java.awt.Color;
import javarominoes.model.Board;
import javarominoes.model.GameState;
import javarominoes.model.GridZone;
import javarominoes.model.Pieces;
import javarominoes.model.TetrominoState;

/**
 * Short white pulse over the cells of a just-landed piece, fading out over the
 * animation's duration. Holds its own snapshot of the landed state because the
 * game state's active piece is replaced by the next spawn immediately after
 * placement.
 *
 * @author dylan
 */
public class PiecePlacementRenderPhase extends AbstractAnimatedRenderPhase {

  private static final int DURATION_MS = 100;
  private static final int PEAK_ALPHA = 80;

  private final TetrominoState landed;

  public PiecePlacementRenderPhase(GameState gs, TetrominoState landed) {
    super(gs, DURATION_MS);
    this.landed = landed;
  }

  @Override
  public void draw() {
    if (graphics == null || landed == null || bckPix <= 0) {
      return;
    }
    float fade = 1f - progress();
    graphics.setColor(new Color(255, 255, 255, (int) (PEAK_ALPHA * fade)));

    for (int x = 0; x < Board.PIECE_BLOCKS; ++x) {
      for (int y = 0; y < Board.PIECE_BLOCKS; ++y) {
        if (Pieces.getBlockType(landed.tyRot.f, landed.tyRot.s, x, y) != Board.POS_FREE) {
          graphics.fillRect((x + landed.xy.f) * bckPix, (y + landed.xy.s) * bckPix,
                  bckPix, bckPix);
        }
      }
    }
  }

  @Override
  public GridZone getZone() {
    return GridZone.boundingBox(Pieces.MATRIX[landed.tyRot.f][landed.tyRot.s],
            landed.xy);
  }

  @Override
  public int getRenderPhaseId() {
    return RenderPhase.Factory.ID_PPRP;
  }
}
