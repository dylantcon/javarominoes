/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javarominoes.GameController;
import javax.swing.Timer;

/**
 *
 * @author dylan
 */
public class TetrisKeyListener implements ActionListener, KeyListener {

  private final static int KEY_POLL_MS = 16; // 60HZ
  public final Set<Integer> pressed = ConcurrentHashMap.newKeySet();

  private long leftHeldSinceMs = 0;
  private long lastLeftMoveMs = 0;

  private long rightHeldSinceMs = 0;
  private long lastRightMoveMs = 0;

  private boolean qWasHeldLastFrame = false;
  private boolean eWasHeldLastFrame = false;

  private long softDropHeldSinceMs = 0;
  private long lastSoftDropMs = 0;

  private boolean spaceWasHeldLastFrame = false;

  private static final long DAS_DELAY = 170;
  private static final long DAS_REPEAT = 33;
  private static final long SOFT_DROP_REPEAT = 50;

  public static final Map<String, String> CTRL_MAP;

  static {
    // insertion-ordered: the legend InfoPanel draws from this map reads top to
    // bottom in the order written below, rather than in hash order
    Map<String, String> map = new LinkedHashMap<>();
    map.put("Q",   "Rotate CCW");
    map.put("E",   "Rotate CW");
    map.put("A",   "Move left");
    map.put("S",   "Move down");
    map.put("D",   "Move right");
    map.put("Spc", "Drop piece");
    CTRL_MAP = Collections.unmodifiableMap(map);
  }

  public Timer keyInputTimer = new Timer(KEY_POLL_MS, e -> handleKeyInput());

  private final boolean debug;

  private final GameController game;

  public TetrisKeyListener(GameController gc) {
    this(gc, false);
  }

  public TetrisKeyListener(GameController gc, boolean debug) {
    this.game = gc;
    this.debug = debug;
  }

  public void start() {
    if (keyInputTimer == null) {
      keyInputTimer = new Timer(KEY_POLL_MS, e -> handleKeyInput());
    }

    keyInputTimer.start();
  }

  private void handleKeyInput() {
    if (game.blockTimer.isRunning()) // only respond to these keys if unpaused
    {
      long now = System.currentTimeMillis();
      if (debug) {
        processDebugInput();
      }
      processRotationInput();
      processHorizontalInput(now);
      processDownwardInput(now);
      game.tick(); // score might have changed. tick for updates to score, display
    }
  }

  /**
   * May crash on a race condition if the developer mashes the number keys. Not
   * going to fix this since it's not going to be exposed to the end user.
   */
  private void processDebugInput() {
    boolean foundDebug = false;
    if (hasDebugKeysBanked()) {
      for (int key = KeyEvent.VK_1; key <= KeyEvent.VK_7; ++key) {
        if (foundDebug) {
          // one debug key was found and its input processed. clear, return
          ArrayList<Integer> duds = IntStream.rangeClosed(key, KeyEvent.VK_7)
                  .boxed()
                  .collect(Collectors.toCollection(ArrayList::new));
          pressed.removeIf(val -> duds.contains(val) && pressed.contains(val));
          return;
        } else if (pressed.contains(key)) {
          foundDebug = true;
          game.overrideActiveTetromino(key - KeyEvent.VK_1);
        }
      }
    }
  }

  private void processHorizontalInput(long now) {
    boolean leftHeld = pressed.contains(KeyEvent.VK_A);
    boolean rightHeld = pressed.contains(KeyEvent.VK_D);

    if (leftHeld) {
      if (leftHeldSinceMs == 0) {
        // just pressed, move immediately
        game.movePiece(-1);
        leftHeldSinceMs = now;
        lastLeftMoveMs = now;
      } else if (now - leftHeldSinceMs >= DAS_DELAY
              && now - lastLeftMoveMs >= DAS_REPEAT) {
        // held long enough to auto shift, and enough time since last shift
        game.movePiece(-1);
        lastLeftMoveMs = now;
      }
    } else {
      leftHeldSinceMs = 0;
    }

    if (rightHeld) {
      if (rightHeldSinceMs == 0) {
        // just pressed, move immediately
        game.movePiece(1);
        rightHeldSinceMs = now;
        lastRightMoveMs = now;
      } else if (now - rightHeldSinceMs >= DAS_DELAY
              && now - lastRightMoveMs >= DAS_REPEAT) {
        // held long enough to auto shift, and enough time since last shift
        game.movePiece(1);
        lastRightMoveMs = now;
      }
    } else {
      rightHeldSinceMs = 0;
    }
  }

  private void processRotationInput() {
    boolean qHeldNow = pressed.contains(KeyEvent.VK_Q);
    if (qHeldNow && !qWasHeldLastFrame) {
      game.rotateCCW();
    }
    qWasHeldLastFrame = qHeldNow;
    boolean eHeldNow = pressed.contains(KeyEvent.VK_E);
    if (eHeldNow && !eWasHeldLastFrame) {
      game.rotateCW();
    }
    eWasHeldLastFrame = eHeldNow;
  }

  private void processDownwardInput(long now) {
    boolean spaceHeldNow = pressed.contains(KeyEvent.VK_SPACE);
    if (spaceHeldNow && !spaceWasHeldLastFrame) {
      game.fullDropPiece();
    }
    spaceWasHeldLastFrame = spaceHeldNow;
    if (!spaceWasHeldLastFrame) {
      boolean softDropHeld = pressed.contains(KeyEvent.VK_S);
      if (softDropHeld) {
        if (softDropHeldSinceMs == 0) {
          game.movePieceDown();
          lastSoftDropMs = now;
          softDropHeldSinceMs = now;
        } else if (now - lastSoftDropMs >= SOFT_DROP_REPEAT) {
          game.movePieceDown();
          lastSoftDropMs = now;
        }
      } else {
        softDropHeldSinceMs = 0;
      }
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {

    if (game.blockTimer == null) {
      return; // prior to game lifespan start, ignore input
    }
    // only allow pause-unpause toggle via esc if game is not over
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !game.getBoard().isGameOver()) {
      game.togglePause(System.currentTimeMillis());
      return;
    }

    // now it's safe to add pressed keys to the hash table
    pressed.add(e.getKeyCode());
  }

  @Override
  public void keyReleased(KeyEvent e) {
    pressed.remove(e.getKeyCode());
  }

  @Override
  public void keyTyped(KeyEvent e) {/* not relevant for our implementation */
  }

  @Override
  public void actionPerformed(ActionEvent e) {/* not relevant for our implementation */
  }

  private boolean hasDebugKeysBanked() {
    return IntStream.rangeClosed(KeyEvent.VK_1, KeyEvent.VK_7)
            .anyMatch(pressed::contains);
  }
}
