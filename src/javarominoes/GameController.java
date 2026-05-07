/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

import javarominoes.view.InfoPanel;
import javarominoes.view.PauseMenuPanel;
import javarominoes.view.BoardPanel;
import javarominoes.model.Pieces;
import javarominoes.model.Board;
import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author dylan
 */
public class GameController extends JLayeredPane implements KeyListener, ActionListener {
  
  private final JPanel basePanel;

  private final PauseMenuPanel pauseMenuPanel;
  private BoardPanel boardPanel;
  private InfoPanel infoPanel;
  
  private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();

  public Board board;

  // TTD = Time To Descend
  public final static int MIN_TTD = 180;
  private boolean MIN_TTD_REACHED = false;
  private final static int INIT_TTD = 700;
  private final static int MUSIC_SPEEDUP_THRESHOLD = INIT_TTD - 100;
  
  public int TTD = INIT_TTD;  // 700 ms initial ttd
  private long timePaused; // time elapsed while paused during one ttd interval
  private long ttdIntervalJunctureMillis;
  private long lastTimerStopMillis;
  
  private final static int KEY_POLL_MS = 16; // 60HZ
  
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

  public Timer blockTimer; // fires events for natural block descent every TTD ms
  public Timer keyInputTimer = new Timer(KEY_POLL_MS, e -> handleKeyInput());

  private int currentPiece;
  private int currentRotation;
  public int nextPiece;
  public int nextRotation;
  private int posX, posY;

  private final int[] bag = new int[7];
  private int currentBagIndex;

  public GameController() {
    
    // cfg ui
    basePanel = new JPanel(); // base layer 
    basePanel.setLayout(new GridLayout(1, 2));

    board = new Board(); // cfg model

    // cfg: pause menu panel, board panel, information panel
    pauseMenuPanel = new PauseMenuPanel(GameController.this);
    pauseMenuPanel.getResumeButton().addActionListener(GameController.this);
    pauseMenuPanel.getRestartButton().addActionListener(GameController.this);

    boardPanel = new BoardPanel(board);
    infoPanel = new InfoPanel(GameController.this);

    basePanel.add(boardPanel);
    basePanel.add(infoPanel);

    add(basePanel, JLayeredPane.DEFAULT_LAYER);
    add(pauseMenuPanel, JLayeredPane.PALETTE_LAYER);

    initializeBag();
    nextPiece = getNextPieceFromBag();
    nextRotation = GameController.getRandomNumber(4);
    generateNewPiece();

    // blockTimer is initially null prior to starting game lifespan 
    blockTimer = null;
    
    initKeyInput(); // key listeners ignore until blockTimer exists
  }

  public PauseMenuPanel getPauseMenuPanel() {
    return pauseMenuPanel != null ? pauseMenuPanel : null;
  }
  
  public void gameStartLifespan()
  {
    long initialTimeMillis = System.currentTimeMillis();
    initializeBlockTimer(initialTimeMillis);
    startBlockTimer(initialTimeMillis);
    keyInputTimer.start();
  }
  
  // initialize the bag array and shuffle it
  private void initializeBag() {
    for (int i = 0; i < 7; i++) {
      bag[i] = i;
    }

    shuffleBag();
    currentBagIndex = 0;
  }

  // Fisher-Yates shuffle to randomize the bag
  private void shuffleBag() {
    for (int i = bag.length - 1; i > 0; i--) {
      int j = getRandomNumber(i);
      int temp = bag[i];
      bag[i] = bag[j];
      bag[j] = temp;
    }
  }

  // get the next piece from the bag, reshuffling when the bag is empty
  private int getNextPieceFromBag() {
    // if we've used all pieces in the bag
    if (currentBagIndex >= bag.length) {
      shuffleBag();         // reshuffle it
      currentBagIndex = 0;  // reset the index for iteration
    }
    return bag[currentBagIndex++];
  }

  private void initKeyInput() {
    this.addKeyListener(this);
    this.setFocusable(true);
    this.requestFocusInWindow();
  }

  private void updateTTDInterval() {
    if (!MIN_TTD_REACHED) {
      int candidateTTD = infoPanel.deltaTTD();
      if (TTD <= MIN_TTD) {
        MIN_TTD_REACHED = true;
        candidateTTD = MIN_TTD;
      }
      TTD = candidateTTD;
      blockTimer.setDelay(TTD);
      updateMusicSpeed();
    }
  }
  
  private static double speedForDropTime(int ttd) {
    /* drop time starts at INIT_TTD = 700ms, ends at MIN_TTD = 180ms
     first 100ms is intro padding, no speedup. remaining 420ms is 
    split into 8 tiers, mapped below: */
    if (ttd >= MUSIC_SPEEDUP_THRESHOLD) return 1.0; //intro
    int tier = Math.min((int)Math.floor((600 - ttd) / 52.5 + 1), 8);
    switch (tier)
    {
      case 1 -> { return 1.05; }
      case 2 -> { return 1.10; }
      case 3 -> { return 1.20; }
      case 4 -> { return 1.30; }
      case 5 -> { return 1.40; }
      case 6 -> { return 1.55; }
      case 7 -> { return 1.70; }
      case 8 -> { return 2.00; }
      default -> { return 1.00; }
    }
  }
  
  private void updateMusicSpeed() {
    PauseMenuPanel.musicHandler.setSpeed(speedForDropTime(TTD));
  }

  private void initializeBlockTimer(long initializationTimeMillis) {
    if (blockTimer == null) {
      blockTimer = new Timer(INIT_TTD, this);
    } else {
      blockTimer.setDelay(INIT_TTD);
    }

    // during initialization,
    timePaused = 0; // therefore the blockTimer has been paused for 0 ms
    TTD = INIT_TTD; // ttd equals init_ttd (no score accrued)

    // initialization is a valid ttd interval juncture
    ttdIntervalJunctureMillis = initializationTimeMillis;

    // call ensures stoppedMillis - ttdIntervalJuncture is 0 for initial start
    stopBlockTimer(initializationTimeMillis);
  }
  
  private void startBlockTimer(long startedMillis) {
    if (blockTimer == null) {
      return;
    }

    if (!blockTimer.isRunning()) {
      timePaused += startedMillis - lastTimerStopMillis;
      long stoppedMillis = startedMillis - timePaused;

      // ignore paused intervals, decrease delay only by active time elapsed
      int remainingTTD = TTD - (int) (stoppedMillis - ttdIntervalJunctureMillis);
      int newDelay = remainingTTD > 0 ? remainingTTD : 0;

      if (newDelay > 0) {
        blockTimer.setDelay(newDelay);
        blockTimer.start();
      } else {
        ActionEvent e = new ActionEvent(blockTimer, ActionEvent.ACTION_PERFORMED, "p");
        actionPerformed(e);
      }
    }
  }

  private void stopBlockTimer(long stoppedMillis) {
    if (blockTimer == null) {
      return;
    }

    // second expression is only reachable and true during initialization
    if (blockTimer.isRunning() || ttdIntervalJunctureMillis == stoppedMillis) {
      lastTimerStopMillis = stoppedMillis;
      blockTimer.stop();
    }
  }
  
  private void tick() {
    updateTTDInterval();
    repaint();
  }

  private void flipBlockTimer(long flippedMillis) {
    if (blockTimer == null) {
      return;
    }
    if (blockTimer.isRunning()) // if blockTimer was running on call, flip blockTimer to off
    {
      stopBlockTimer(flippedMillis);
    } else // if blockTimer was not running on call, flip blockTimer to on
    {
      startBlockTimer(flippedMillis);
    }
  }

  private void togglePause(long toggledMillis) {
    flipBlockTimer(toggledMillis);
    pauseMenuPanel.setVisible(!blockTimer.isRunning());
    repaint();
  }

  
  // game lifespan must be started via call to gameStartLifespan
  public void reinitializeGame() {

    if (this.blockTimer != null) {
      blockTimer.stop();
    }

    // clean up main layer by resetting model and re-initing components
    basePanel.removeAll();
    board = new Board();
    boardPanel = new BoardPanel(board);
    infoPanel = new InfoPanel(GameController.this);

    // clean up pause menu layer by setting invis, revert pause state if needed
    pauseMenuPanel.setVisible(false);
    if (!pauseMenuPanel.isShowingPause()) {
      pauseMenuPanel.setPaused();
    }
    
    if (PauseMenuPanel.musicHandler.doingPlayback())
      PauseMenuPanel.musicHandler.restartMusic();

    basePanel.add(boardPanel);
    basePanel.add(infoPanel);

    nextPiece = GameController.getRandomNumber(7);
    nextRotation = GameController.getRandomNumber(4);
    generateNewPiece();

    revalidate();
    repaint();
  }

  private void generateNewPiece() {
    // assign next piece and rotation as current piece
    currentPiece = nextPiece;
    currentRotation = nextRotation;

    // set the initial position of x and y, based on piece and rotation type
    setPieceOffset(currentPiece, currentRotation);

    // randomly select the next piece and rotation for the upcoming piece
    nextPiece = getNextPieceFromBag();
    nextRotation = GameController.getRandomNumber(4);

    boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
    infoPanel.updateIncomingPieceInfo(nextPiece, nextRotation);
  }

  private static int getRandomNumber(int end) {
    return ((int) (Math.random() * end));
  }

  private void setPieceOffset(int curPiece, int curRotation) {
    posX = Pieces.getXInitialPos(curPiece, curRotation);
    posY = Pieces.getYInitialPos(curPiece, curRotation);
  }

  private boolean checkCollision() {
    return !board.isPossibleMovement(posX, posY, currentPiece, currentRotation);
  }

  public void movePieceDown() {
    posY++;
    infoPanel.increaseScore(1);

    if (checkCollision() == true) {
      posY--;
      board.storePiece(posX, posY, currentPiece, currentRotation);
      if (board.isGameOver() == false) {
        infoPanel.increaseScore(infoPanel.getLineClearScore(board.deletePossibleLines()));
        generateNewPiece();
      } else {
        pauseMenuPanel.setGameOver();
        togglePause(System.currentTimeMillis());
      }
    }
    boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
  }

  // this is for full drop until vertical collision (space bar)
  public void fullDropPiece() {
    do {
      posY++;
      infoPanel.increaseScore(2);
    } while (checkCollision() == false);

    // now store, exactly 1 position higher than collision triggering row
    board.storePiece(posX, posY - 1, currentPiece, currentRotation);

    if (board.isGameOver() == false) {
      infoPanel.increaseScore(infoPanel.getLineClearScore(board.deletePossibleLines()));
      generateNewPiece();
      boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
    } else {
      pauseMenuPanel.setGameOver();
      togglePause(System.currentTimeMillis());
    }
  }

  public void movePiece(int dx) {
    posX += dx;
    if (checkCollision() == true) {
      posX -= dx;
    }
    boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
  }

  public void rotateCW() {
    currentRotation = (currentRotation + 1) % 4;
    if (checkCollision() == true) {
      currentRotation = (currentRotation - 1 + 4) % 4;
    }
    boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
  }

  public void rotateCCW() {
    currentRotation = (currentRotation - 1 + 4) % 4;
    if (checkCollision() == true) {
      currentRotation = (currentRotation + 1) % 4;
    }
    boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
  }
  
  private void processHorizontalInput(long now) {
    boolean leftHeld = pressedKeys.contains(KeyEvent.VK_A);
    boolean rightHeld = pressedKeys.contains(KeyEvent.VK_D);
    
    if (leftHeld) {
      if (leftHeldSinceMs == 0) {
        // just pressed, move immediately
        movePiece(-1);
        leftHeldSinceMs = now;
        lastLeftMoveMs = now;
      } else if (now - leftHeldSinceMs >= DAS_DELAY
              && now - lastLeftMoveMs >= DAS_REPEAT) {
        // held long enough to auto shift, and enough time since last shift
        movePiece(-1);
        lastLeftMoveMs = now;
      }
    }
    else leftHeldSinceMs = 0;
    
    if (rightHeld) {
      if (rightHeldSinceMs == 0) {
        // just pressed, move immediately
        movePiece(1);
        rightHeldSinceMs = now;
        lastRightMoveMs = now;
      } else if (now - rightHeldSinceMs >= DAS_DELAY
              && now - lastRightMoveMs >= DAS_REPEAT) {
        // held long enough to auto shift, and enough time since last shift
        movePiece(1);
        lastRightMoveMs = now;
      }
    }
    else rightHeldSinceMs = 0;
  }
  
  private void processRotationInput() {
    boolean qHeldNow = pressedKeys.contains(KeyEvent.VK_Q);
    if (qHeldNow && !qWasHeldLastFrame) {
      rotateCCW();
    }
    qWasHeldLastFrame = qHeldNow;
    boolean eHeldNow = pressedKeys.contains(KeyEvent.VK_E);
    if (eHeldNow && !eWasHeldLastFrame) {
      rotateCW();
    }
    eWasHeldLastFrame = eHeldNow;
  }
  
  private void processDownwardInput(long now) {
    boolean spaceHeldNow = pressedKeys.contains(KeyEvent.VK_SPACE);
    if (spaceHeldNow && !spaceWasHeldLastFrame) {
      fullDropPiece();
    }
    spaceWasHeldLastFrame = spaceHeldNow;
    if (!spaceWasHeldLastFrame) {
      boolean softDropHeld = pressedKeys.contains(KeyEvent.VK_S);
      if (softDropHeld) {
        if (softDropHeldSinceMs == 0) {
          movePieceDown();
          lastSoftDropMs = now;
          softDropHeldSinceMs = now;
        }
        else if (now - lastSoftDropMs >= SOFT_DROP_REPEAT) {
          movePieceDown();
          lastSoftDropMs = now;
        }
      }
      else softDropHeldSinceMs = 0;
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    
    if (blockTimer == null)
      return; // prior to game lifespan start, ignore input
    
    // only allow pause-unpause toggle via esc if game is not over
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !board.isGameOver()) {
      togglePause(System.currentTimeMillis());
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
  
  private void handleKeyInput() {
    if (blockTimer.isRunning()) // only respond to these keys if unpaused
    {
      long now = System.currentTimeMillis();
      processRotationInput();
      processHorizontalInput(now);
      processDownwardInput(now);
      tick(); // score might have changed. tick for updates to score, display
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == blockTimer) {
      // in scope here means event fired. update last blockTimer event timestamp
      ttdIntervalJunctureMillis = System.currentTimeMillis();
      timePaused = 0; // impossible for paused time to have elapsed at juncture

      // blockTimer fired, move current airborne block down one row and tick
      movePieceDown();
      tick();
      return;
    }
    if (e.getSource() == pauseMenuPanel.getRestartButton()) {
      reinitializeGame();
      gameStartLifespan();
      return;
    }
    if (e.getSource() == pauseMenuPanel.getResumeButton()) {
      togglePause(System.currentTimeMillis());
    }
  }

  @Override
  public void doLayout() {
    super.doLayout();
    pauseMenuPanel.setBounds(0, 0, getWidth(), getHeight());
    basePanel.setBounds(0, 0, getWidth(), getHeight());
  }
}
