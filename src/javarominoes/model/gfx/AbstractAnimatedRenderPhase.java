/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx;

import javarominoes.model.GameState;
import javarominoes.model.GridZone;

/**
 * A render phase that stays resident across multiple paints, animating from
 * progress 0 to 1 over a fixed wall-clock duration. Consumers call
 * {@link #begin()} once when the phase is accepted, redraw its zone while
 * {@link #isFinished()} is false, and discard it afterward.
 *
 * @author dylan
 */
public abstract class AbstractAnimatedRenderPhase extends AbstractRenderPhase {

  private final int durationMs;
  private long startMillis = -1;

  public AbstractAnimatedRenderPhase(GameState gs, int durationMs) {
    super(gs);
    this.durationMs = durationMs;
  }

  /**
   * Starts the animation clock. Subsequent calls are no-ops so a phase cannot
   * be restarted by accident.
   */
  public void begin() {
    if (startMillis < 0) {
      startMillis = System.currentTimeMillis();
    }
  }

  /**
   * @return animation progress clamped to [0, 1]; 0 until begin() is called
   */
  protected float progress() {
    if (startMillis < 0) {
      return 0f;
    }
    float p = (System.currentTimeMillis() - startMillis) / (float) durationMs;
    return Math.min(Math.max(p, 0f), 1f);
  }

  public boolean isFinished() {
    return startMillis >= 0 && progress() >= 1f;
  }

  /**
   * @return the grid region this animation dirties each frame
   */
  public abstract GridZone getZone();
}
