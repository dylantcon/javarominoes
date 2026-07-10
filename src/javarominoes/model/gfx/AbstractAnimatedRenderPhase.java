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
   * Whether the descent timer must be frozen while this animation plays.
   *
   * <p>
   * A line clear must halt the game: the rows it is dissolving are already gone
   * from the board, so a piece descending through them would be reasoning about
   * a board the player cannot yet see. A placement pulse need not: it is a
   * flourish over blocks which are already fixed and already baked into the
   * static layer, and freezing the controls to play it only makes the game feel
   * as though it were stuttering.</p>
   *
   * @author dylan
   * @return whether gravity and input should wait for this animation
   */
  public boolean haltsGameplay() {
    return true;
  }

  /**
   * @return the grid region this animation dirties each frame
   */
  public abstract GridZone getZone();
}
