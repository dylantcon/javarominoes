/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx;

import java.awt.Color;
import java.awt.Graphics;
import javarominoes.model.Board;
import javarominoes.model.GameState;
import javarominoes.model.GridZone;
import javarominoes.model.Pieces;
import javarominoes.model.TetrominoState;

/**
 * Draws the active piece's landing silhouette: a translucent copy of the
 * piece at the lowest position it can occupy in its current column.
 *
 * @author dylan
 */
public class SilhouettePieceRenderPhase extends AbstractRenderPhase {

  private static final Color SILHOUETTE_COLOR = new Color(255, 255, 255, 50);

  public SilhouettePieceRenderPhase(Graphics g, GameState gs, int bPx) {
    super(g, gs, bPx);
  }

  public SilhouettePieceRenderPhase(GameState gs, int bPx) {
    super(gs, bPx);
  }

  public SilhouettePieceRenderPhase(GameState gs) {
    super(gs);
  }

  @Override
  public void draw() {
    TetrominoState active = gameState != null ? gameState.active() : null;
    if (graphics == null || active == null) {
      return;
    }
    Board b = gameState.getBoardState();
    TetrominoState sil = TetrominoState.Factory.silhouetteCopy(active,
            b.getSilhouetteY(active));
    TetrominoGraphics.Render.drawPiece(graphics, bckPix, sil, SILHOUETTE_COLOR);
    TetrominoGraphics.Render.outlinePhase__Debug(graphics, bckPix, this);
  }

  /**
   * The banked movement footprint, falling back to where the silhouette sits
   * right now, for the spawn, where no movement has yet been recorded.
   */
  @Override
  public GridZone debugZone() {
    GridZone banked = TetrominoGraphics.getSilhouettePieceZone();
    return banked != null ? banked : currentZone();
  }

  /**
   * Bounding box of the silhouette at its current landing position. Fallback
   * for repaints when no movement-dirtied zone was recorded (e.g. on spawn,
   * where the previous state is null).
   *
   * @return the silhouette's current GridZone, or null if no piece is active
   */
  public GridZone currentZone() {
    TetrominoState active = gameState != null ? gameState.active() : null;
    if (active == null) {
      return null;
    }
    Board b = gameState.getBoardState();
    return GridZone.boundingBox(Pieces.MATRIX[active.tyRot.f][active.tyRot.s],
            b.getSilhouetteCoordinates(active));
  }

  @Override
  public int getRenderPhaseId() {
    return RenderPhase.Factory.ID_SPRP;
  }
}
