/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

import javarominoes.view.InfoPanel;
import javarominoes.view.PauseMenuPanel;
import javarominoes.view.BoardPanel;
import javarominoes.model.Board;
import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javarominoes.model.GridZone;
import javarominoes.model.gfx.TetrominoGraphics;
import javarominoes.model.TetrominoState;
import javarominoes.model.control.TetrisKeyListener;
import chiptunesynth.music.MusicHandler;
import javarominoes.model.GameState;
import javarominoes.model.gfx.RenderPhase;
import javarominoes.model.util.Pair;

/**
 *
 * @author dylan
 */
public class GameController extends JLayeredPane implements ActionListener {

  // ui
  private final JPanel basePanel;
  private final PauseMenuPanel pauseMenuPanel;
  private BoardPanel boardPanel;
  private InfoPanel infoPanel;

  // similar to original game's DAS system
  private final TetrisKeyListener tkl;

  private GameState state = null;
  private boolean gameOver;
  private boolean animationHold = false;

  public Timer blockTimer; // fires events for natural block descent every TTD ms

  // TTD = Time To Descend
  public final static int MIN_TTD = 180;
  private boolean MIN_TTD_REACHED = false;
  private final static int INIT_TTD = 700;
  private final static int MUSIC_SPEEDUP_THRESHOLD = INIT_TTD - 100;

  public int TTD = INIT_TTD;  // 700 ms initial ttd
  private long timePaused; // time elapsed while paused during one ttd interval
  private long ttdIntervalJunctureMillis;
  private long lastTimerStopMillis;

  public GameController() {

    // cfg ui
    basePanel = new JPanel(); // base layer 
    basePanel.setLayout(new GridLayout(1, 2));

    state = new GameState(new Board());
    gameOver = false;

    // cfg: pause menu panel, board panel, information panel
    pauseMenuPanel = new PauseMenuPanel(GameController.this);
    pauseMenuPanel.getResumeButton().addActionListener(GameController.this);
    pauseMenuPanel.getRestartButton().addActionListener(GameController.this);

    GridZone.Factory.assignController(GameController.this);

    boardPanel = new BoardPanel(GameController.this);
    infoPanel = new InfoPanel(GameController.this);

    basePanel.add(boardPanel);
    basePanel.add(infoPanel);

    add(basePanel, JLayeredPane.DEFAULT_LAYER);
    add(pauseMenuPanel, JLayeredPane.PALETTE_LAYER);

    // blockTimer is initially null prior to starting game lifespan 
    blockTimer = null;
    tkl = new TetrisKeyListener(GameController.this, false);

    initKeyInput(); // key listeners ignore until blockTimer exists
  }

  public GameState getGameState() {
    return state;
  }

  public Board getBoard() {
    return state.getBoardState();
  }

  public TetrominoState piece() {
    return state.active();
  }

  public boolean isGameOver() {
    return gameOver;
  }

  public PauseMenuPanel getPauseMenuPanel() {
    return pauseMenuPanel != null ? pauseMenuPanel : null;
  }

  public void startGame() {
    feedPieces();
    beginGameTimerLifespans();
    requestFocusInWindow();
  }

  private void beginGameTimerLifespans() {
    long initialTimeMillis = System.currentTimeMillis();
    initializeBlockTimer(initialTimeMillis);
    startBlockTimer(initialTimeMillis);
    tkl.start();
  }

  private void initKeyInput() {
    this.addKeyListener(tkl);
    this.setFocusable(true);
    this.requestFocusInWindow();
  }

  /**
   * @return whether the drop interval moved, and with it the readout which
   * reports it
   */
  private boolean updateTTDInterval() {
    if (MIN_TTD_REACHED) {
      return false;
    }
    int candidateTTD = infoPanel.deltaTTD();
    if (TTD <= MIN_TTD) {
      MIN_TTD_REACHED = true;
      candidateTTD = MIN_TTD;
    }
    if (candidateTTD == TTD) {
      return false;
    }
    TTD = candidateTTD;
    blockTimer.setDelay(TTD);
    updateMusicSpeed();
    return true;
  }

  private static double speedForDropTime(int ttd) {
    /* drop time starts at INIT_TTD = 700ms, ends at MIN_TTD = 180ms
     first 100ms is intro padding, no speedup. remaining 420ms is 
    split into 8 tiers, mapped below: */
    if (ttd >= MUSIC_SPEEDUP_THRESHOLD) {
      return 1.0; //intro
    }
    int tier = Math.min((int) Math.floor((600 - ttd) / 52.5 + 1), 8);
    switch (tier) {
      case 1:
        return 1.05;
      case 2:
        return 1.10;
      case 3:
        return 1.20;
      case 4:
        return 1.30;
      case 5:
        return 1.40;
      case 6:
        return 1.55;
      case 7:
        return 1.70;
      case 8:
        return 2.00;
      default:
        return 1.00;
    }
  }

  private void updateMusicSpeed() {
    if (!gameOver) {
      PauseMenuPanel.musicHandler.setSpeed(speedForDropTime(TTD));
    }
  }

  private void initializeBlockTimer(long initializationTimeMillis) {
    if (blockTimer == null) {
      blockTimer = new Timer(INIT_TTD, this);
    } else {
      blockTimer.setInitialDelay(INIT_TTD);
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

      // an overdue interval fires promptly as a REAL timer event, which
      // resets the interval juncture; the old synthetic dispatch left the
      // timer stopped, freezing input and gravity until the next resume
      int newDelay = Math.max(remainingTTD, 1);

      // start() schedules the first fire from initialDelay, not delay;
      // delay then governs the post-resume cadence
      blockTimer.setInitialDelay(newDelay);
      blockTimer.setDelay(TTD);
      blockTimer.start();
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

  /**
   * Called by the key listener at every input poll, some sixty times a second.
   *
   * <p>
   * It used to repaint() this JLayeredPane unconditionally, which dirtied the
   * whole component tree. Swing unions that with the per-zone repaints the
   * GridPanel issues, so the grid was fully repainted at the poll rate and the
   * dirty zones never took effect during play. It also redrew the InfoPanel's
   * keycap legend sixty times a second in order to show a readout that changes
   * a few dozen times per game.</p>
   *
   * <p>
   * A tick can only change the drop interval. The score repaints the readout
   * from increaseScore(), and every movement of a piece dispatches its own
   * render phases, so nothing else here needs a repaint.</p>
   */
  public void tick() {
    if (updateTTDInterval()) {
      infoPanel.repaint();
    }
  }

  /**
   * Freezes the descent timer while the view plays a placement or line clear
   * animation. startBlockTimer's pause accounting guarantees the interrupted
   * TTD interval's total active duration is preserved across the freeze.
   */
  public void holdForAnimation() {
    if (gameOver || animationHold) {
      return;
    }
    animationHold = true;
    stopBlockTimer(System.currentTimeMillis());
  }

  public void releaseAnimationHold() {
    if (!animationHold) {
      return;
    }
    animationHold = false;
    // if the user paused mid-animation, resuming belongs to the pause menu
    if (!gameOver && !pauseMenuPanel.isVisible()) {
      startBlockTimer(System.currentTimeMillis());
    }
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

  public void togglePause(long toggledMillis) {
    flipBlockTimer(toggledMillis);
    pauseMenuPanel.setVisible(!blockTimer.isRunning());
    repaint();
  }

  // game lifespan must be started via call to beginGameTimerLifespans
  public void reinitializeGame() {

    if (this.blockTimer != null) {
      blockTimer.stop();
    }

    // clean up main layer by resetting model and re-initing components
    basePanel.removeAll();
    state.setBoardState(new Board());
    gameOver = false;
    animationHold = false; // a discarded BoardPanel's animations no longer hold

    boardPanel = new BoardPanel(GameController.this);
    infoPanel = new InfoPanel(GameController.this);

    // clean up pause menu layer by setting invis, revert pause state if needed
    pauseMenuPanel.setVisible(false);
    if (!pauseMenuPanel.isShowingPause()) {
      pauseMenuPanel.setPaused();
    }

    if (PauseMenuPanel.musicHandler.doingPlayback()) {
      PauseMenuPanel.musicHandler.restartMusic();
    }

    basePanel.add(boardPanel);
    basePanel.add(infoPanel);

    feedPieces();

    revalidate();
    repaint();
  }

  private void feedPieces() {
    if (state.inactive() == null) {
      state.setInactiveState(TetrominoState.Factory.yates());
    }
    // assign next piece and rotation to current piece, and give it a location
    state.withActiveState(state.inactive());
    state.active().setOffset();
    // randomly select the next piece and rotation for the upcoming piece
    state.setInactiveState(TetrominoState.Factory.yates());

    // footprint must be banked AFTER the spawn swap; before it, active()
    // still holds downwardProbe's collided copy, one row below the landing
    TetrominoGraphics.bankLastTetrominoFootprint(null); // null obj pattern
    boardPanel.bankRenderPhase(RenderPhase.Factory.airbornePieceRenderPhase(state));
    boardPanel.bankRenderPhase(RenderPhase.Factory.silhouettePieceRenderPhase(state));
    infoPanel.updateIncomingPieceInfo(state.inactive());
    boardPanel.dispatchGridPanelRerender();
  }

  /**
   * For debugging purposes. Replaces current falling piece with piece pPiece,
   * relocating it to the associated starting position using the offsets.
   *
   * @param pPiece
   */
  public void overrideActiveTetromino(int pPiece) {
    state.setActiveState(TetrominoState.Factory.create(pPiece).setOffset());
    boardPanel.bankRenderPhase(RenderPhase.Factory.airbornePieceRenderPhase(state));
  }

  private boolean stateCollides(TetrominoState t) {
    return !state.getBoardState().isPossibleMovement(t);
  }

  private void processGameEnd() {
    gameOver = true;
    pauseMenuPanel.setGameOver();
    PauseMenuPanel.musicHandler.setSpeed(MusicHandler.GAME_OVER_SPEED);
    togglePause(System.currentTimeMillis());
  }

  private void processPiecePlacement(TetrominoState t) {
    // ask before storing: storePiece discards the blocks above the ceiling,
    // and a board which has already swallowed them cannot be asked afterward
    boolean lockedOut = state.getBoardState().exceedsCeiling(t);
    state.getBoardState().storePiece(t);

    if (lockedOut == false) {
      // bake the landed piece into the static layer and pulse it
      TetrominoGraphics.markLandingDirtyZone(t);
      boardPanel.bankRenderPhase(RenderPhase.Factory.fixedBlocksRenderPhase(state));
      boardPanel.bankRenderPhase(RenderPhase.Factory.piecePlacementRenderPhase(state, t));

      // one run per contiguous band of filled rows; a gapped clear must not
      // collapse into a single range, or the unfilled rows between the bands
      // would be deleted along with them
      ArrayList<Pair<Integer, Integer>> runs = state.getBoardState().getRangesToClear();

      if (!runs.isEmpty()) {
        int rowsCleared = 0;
        for (Pair<Integer, Integer> run : runs) {
          rowsCleared += Board.dist(run);
          boardPanel.bankRenderPhase(RenderPhase.Factory.lineClearRenderPhase(state, run.f, run.s));
        }
        infoPanel.increaseScore(infoPanel.getLineClearScore(rowsCleared));
        // dispatch BEFORE deleting so the landing bakes and the flash starts
        // while the static layer still shows the full rows; the shifted rows
        // rebake when the animation finishes
        boardPanel.dispatchGridPanelRerender();

        // deleting a run only shifts the rows above it, so every run below
        // the one being deleted keeps its indices. descend in order
        for (Pair<Integer, Integer> run : runs) {
          state.getBoardState().deleteLines(run);
        }
      }

      feedPieces(); // inTetromino=nextTetromino, nextTetromino=yates
    } else {
      processGameEnd();
    }
  }

  private void checkTetrominoTranslationState(TetrominoState t) {
    if (stateCollides(state.active()) && !attemptWallKick(t)) {
      state.setActiveState(t); // no way through. revert to the last legal state
      return;
    }

    // succeeded, still midair, calculate dirtied region. the footprint is
    // always banked against t, the last legal state: a kicked piece has left
    // its old cells behind, and only a nonzero delta will dirty them
    TetrominoGraphics.bankLastTetrominoFootprint(t);
    boardPanel.bankRenderPhase(RenderPhase.Factory.airbornePieceRenderPhase(state));
    boardPanel.bankRenderPhase(RenderPhase.Factory.silhouettePieceRenderPhase(state));
    boardPanel.dispatchGridPanelRerender();
  }

  /**
   * Rotations alone may kick off of a wall. A rotation which collides retries
   * itself one column to the left, and then one column to the right, before it
   * is abandoned. Translations are given no such reprieve.
   *
   * @param t the last legal state, prior to the attempted translation
   * @return whether a kick resolved the collision. the active state is mutated
   * into the kicked position when it did
   */
  private boolean attemptWallKick(TetrominoState t) {
    if (!TetrominoState.Factory.copy(state.active()).deltaState(t).isRotationDelta()) {
      return false;
    }
    if (!stateCollides(TetrominoState.Factory.kickLeftCopy(state.active()))) {
      state.active().decX();
      return true;
    }
    if (!stateCollides(TetrominoState.Factory.kickRightCopy(state.active()))) {
      state.active().incX();
      return true;
    }
    return false;
  }

  public void movePieceDown() {
    TetrominoState t = state.active();
    // between when a piece is erased/made to be static in Board, we analyze
    // the effects on the rendered area to determine what regions must be drawn
    if (!state.downwardProbe()) {
      processPiecePlacement(t);
      return;
    } else {
      // no lines cleared. same piece midair, successfully translated down one.
      infoPanel.increaseScore(); // award point
    }
    TetrominoGraphics.bankLastTetrominoFootprint(t);// t always is last valid spot.
    boardPanel.bankRenderPhase(RenderPhase.Factory.airbornePieceRenderPhase(state));
    boardPanel.dispatchGridPanelRerender();
  }

  // this is for full drop until vertical collision (space bar)
  public void fullDropPiece() {
    TetrominoState t = state.active();
    while (state.downwardProbe()) {
      t = state.active();
      infoPanel.increaseScore(2);
    }

    // now store, exactly 1 position higher than collision triggering row (t)
    processPiecePlacement(t);
  }

  public void movePiece(int dx) {
    TetrominoState t = state.active();
    state.setActiveState(TetrominoState.Factory.translateCopy(t, dx, 0));
    checkTetrominoTranslationState(t);
  }

  public void rotateCW() {
    TetrominoState t = state.active();
    state.setActiveState(TetrominoState.Factory.rotateCopy(t, 1));
    checkTetrominoTranslationState(t);
  }

  public void rotateCCW() {
    TetrominoState t = state.active();
    state.setActiveState(TetrominoState.Factory.rotateCopy(t, -1));
    checkTetrominoTranslationState(t);
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
      beginGameTimerLifespans();
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
