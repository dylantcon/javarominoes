/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javarominoes.GameController;
import javax.swing.Timer;

/**
 *
 * @author dylan
 */
public class TetrisKeyListener implements ActionListener, KeyListener {
  
  private final static int KEY_POLL_MS = 16; // 60HZ
  public final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();
  
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
  
  public Timer keyInputTimer = new Timer(KEY_POLL_MS, e -> handleKeyInput());
  
  private final GameController managedGame;
  
  public TetrisKeyListener(GameController gc) {
    managedGame = gc;
  }
  
  public void start() {
    if (keyInputTimer == null)
      keyInputTimer = new Timer(KEY_POLL_MS, e -> handleKeyInput());
    
    keyInputTimer.start();
  }
  
  private void handleKeyInput() {
    if (managedGame.blockTimer.isRunning()) // only respond to these keys if unpaused
    {
      long now = System.currentTimeMillis();
      processRotationInput();
      processHorizontalInput(now);
      processDownwardInput(now);
      managedGame.tick(); // score might have changed. tick for updates to score, display
    }
  }
  
    private void processHorizontalInput(long now) {
    boolean leftHeld = pressedKeys.contains(KeyEvent.VK_A);
    boolean rightHeld = pressedKeys.contains(KeyEvent.VK_D);
    
    if (leftHeld) {
      if (leftHeldSinceMs == 0) {
        // just pressed, move immediately
        managedGame.movePiece(-1);
        leftHeldSinceMs = now;
        lastLeftMoveMs = now;
      } else if (now - leftHeldSinceMs >= DAS_DELAY
              && now - lastLeftMoveMs >= DAS_REPEAT) {
        // held long enough to auto shift, and enough time since last shift
        managedGame.movePiece(-1);
        lastLeftMoveMs = now;
      }
    }
    else leftHeldSinceMs = 0;
    
    if (rightHeld) {
      if (rightHeldSinceMs == 0) {
        // just pressed, move immediately
        managedGame.movePiece(1);
        rightHeldSinceMs = now;
        lastRightMoveMs = now;
      } else if (now - rightHeldSinceMs >= DAS_DELAY
              && now - lastRightMoveMs >= DAS_REPEAT) {
        // held long enough to auto shift, and enough time since last shift
        managedGame.movePiece(1);
        lastRightMoveMs = now;
      }
    }
    else rightHeldSinceMs = 0;
  }
  
  private void processRotationInput() {
    boolean qHeldNow = pressedKeys.contains(KeyEvent.VK_Q);
    if (qHeldNow && !qWasHeldLastFrame) {
      managedGame.rotateCCW();
    }
    qWasHeldLastFrame = qHeldNow;
    boolean eHeldNow = pressedKeys.contains(KeyEvent.VK_E);
    if (eHeldNow && !eWasHeldLastFrame) {
      managedGame.rotateCW();
    }
    eWasHeldLastFrame = eHeldNow;
  }
  
  private void processDownwardInput(long now) {
    boolean spaceHeldNow = pressedKeys.contains(KeyEvent.VK_SPACE);
    if (spaceHeldNow && !spaceWasHeldLastFrame) {
      managedGame.fullDropPiece();
    }
    spaceWasHeldLastFrame = spaceHeldNow;
    if (!spaceWasHeldLastFrame) {
      boolean softDropHeld = pressedKeys.contains(KeyEvent.VK_S);
      if (softDropHeld) {
        if (softDropHeldSinceMs == 0) {
          managedGame.movePieceDown();
          lastSoftDropMs = now;
          softDropHeldSinceMs = now;
        }
        else if (now - lastSoftDropMs >= SOFT_DROP_REPEAT) {
          managedGame.movePieceDown();
          lastSoftDropMs = now;
        }
      }
      else softDropHeldSinceMs = 0;
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    
    if (managedGame.blockTimer == null)
      return; // prior to game lifespan start, ignore input
    
    // only allow pause-unpause toggle via esc if game is not over
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !managedGame.board.isGameOver()) {
      managedGame.togglePause(System.currentTimeMillis());
      return;
    }
    
    // now it's safe to add pressed keys to the hash table
    pressedKeys.add(e.getKeyCode());
  }

  @Override
  public void keyReleased(KeyEvent e) {
    pressedKeys.remove(e.getKeyCode());
  }

  @Override
  public void keyTyped(KeyEvent e) {/* not relevant for our implementation */}

  @Override
  public void actionPerformed(ActionEvent e) {/* not relevant for our implementation */}

}
