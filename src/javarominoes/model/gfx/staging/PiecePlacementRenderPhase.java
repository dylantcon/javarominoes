/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx.staging;

import java.awt.Color;
import javarominoes.model.Board;
import javarominoes.model.GameState;
import javarominoes.model.GridZone;
import javarominoes.model.Pieces;
import javarominoes.model.TetrominoState;
import javarominoes.model.gfx.TetrominoGraphics;

/**
 * Short pulse over the cells of a just-landed piece, fading out over the
 * animation's duration. Holds its own snapshot of the landed state because the
 * game state's active piece is replaced by the next spawn immediately after
 * placement.
 *
 * @author dylan
 */
public class PiecePlacementRenderPhase extends AbstractAnimatedRenderPhase {

  private static final int DURATION_MS = 120;
  private static final int PEAK_ALPHA = 100;
  private static final int WHITE_CHANNEL = 0xFF;

  private final TetrominoState landed;

  /**
   * The pulse is the channel-wise mean of white and the landed piece's own
   * colour, so that it reads as that piece brightening rather than as a white
   * flash which happens to sit on top of it.
   */
  private final Color flash;

  public PiecePlacementRenderPhase(GameState gs, TetrominoState landed) {
    super(gs, DURATION_MS);
    this.landed = landed;
    this.flash = landed == null ? Color.WHITE
            : mixWithWhite(TetrominoGraphics.Render.getBlockColor(landed.tyRot.f + 1));
  }

  /**
   * @param c a piece colour, drawn from the 1-indexed table in
   * {@link TetrominoGraphics.Render#getBlockColor(int)}. A TetrominoState's
   * type is 0-indexed, hence the increment at the call site
   * @return the average of one part color and two parts white, opaque
   */
  private static Color mixWithWhite(Color c) {
    return new Color((c.getRed() + 2 * WHITE_CHANNEL) / 3,
            (c.getGreen() + 2 * WHITE_CHANNEL) / 3,
            (c.getBlue() + 2 * WHITE_CHANNEL) / 3);
  }

  @Override
  public void draw() {
    if (graphics == null || landed == null || bckPix <= 0) {
      return;
    }
    float fade = 1f - progress();
    graphics.setColor(new Color(flash.getRed(), flash.getGreen(), flash.getBlue(),
            (int) (PEAK_ALPHA * fade)));

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
  public GridZone debugZone() {
    return getZone();
  }

  /**
   * The pulse plays over blocks already fixed in the board and already baked
   * into the static layer. Nothing it draws can mislead the player as to where
   * a piece may fall, so gravity and the controls carry on beneath it.
   */
  @Override
  public boolean haltsGameplay() {
    return false;
  }

  @Override
  public int getRenderPhaseId() {
    return RenderPhase.Factory.ID_PPRP;
  }
}
