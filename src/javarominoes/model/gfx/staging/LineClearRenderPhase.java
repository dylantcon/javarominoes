/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx.staging;

import java.awt.Color;
import javarominoes.model.Board;
import javarominoes.model.GameState;
import javarominoes.model.GridZone;

/**
 * Two-stage line clear animation over the cleared row band: the band blinks
 * white, then collapses to black from the center outward with a white leading
 * edge. Plays over the pre-clear static layer; the caller rebakes
 * {@link #getRebakeZone()} once the animation finishes to reveal the shifted
 * board.
 *
 * @author dylan
 */
public class LineClearRenderPhase extends AbstractAnimatedRenderPhase {

  private static final int DURATION_MS = 320;
  // first 55% of the animation blinks, the rest collapses
  private static final float BLINK_PORTION = 0.55f;
  private static final int N_BLINKS = 2;
  private static final int EDGE_PX = 2;

  private final int top;
  private final int btm;

  public LineClearRenderPhase(GameState gs, int top, int btm) {
    super(gs, DURATION_MS);
    this.top = top;
    this.btm = btm;
  }

  @Override
  public void draw() {
    if (graphics == null || bckPix <= 0) {
      return;
    }
    float p = progress();
    int yPx = top * bckPix;
    int wPx = Board.WIDTH * bckPix;
    int hPx = (btm - top + 1) * bckPix;

    if (p < BLINK_PORTION) {
      // on-phases show a solid white band, off-phases show the full rows
      int blinkPhase = (int) (p / BLINK_PORTION * (N_BLINKS * 2));
      if (blinkPhase % 2 == 0) {
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, yPx, wPx, hPx);
      }
    } else {
      float collapse = (p - BLINK_PORTION) / (1f - BLINK_PORTION);
      int half = (int) ((wPx / 2f) * collapse);

      graphics.setColor(Color.BLACK);
      graphics.fillRect((wPx / 2) - half, yPx, half, hPx);
      graphics.fillRect(wPx / 2, yPx, half, hPx);

      graphics.setColor(Color.WHITE);
      graphics.fillRect(Math.max((wPx / 2) - half - EDGE_PX, 0), yPx, EDGE_PX, hPx);
      graphics.fillRect(Math.min((wPx / 2) + half, wPx - EDGE_PX), yPx, EDGE_PX, hPx);
    }
  }

  @Override
  public GridZone getZone() {
    return GridZone.Factory.rowBand(top, btm);
  }

  @Override
  public GridZone debugZone() {
    return getZone();
  }

  /**
   * Deleting rows shifts everything above them down, so the whole region from
   * the top of the board through the cleared band must be rebaked.
   */
  public GridZone getRebakeZone() {
    return GridZone.Factory.rowBand(0, btm);
  }

  @Override
  public int getRenderPhaseId() {
    return RenderPhase.Factory.ID_LCRP;
  }
}
