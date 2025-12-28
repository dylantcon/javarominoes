/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;
import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author dylan
 */
public class GamePanel extends JLayeredPane implements KeyListener, ActionListener
{
  private final JPanel basePanel;
  
  private final JPanel pauseMenuPanel;
  private BoardPanel boardPanel;
  private InfoPanel infoPanel;
  
  public Board board;
  
  // TTD = Time To Descend
  public final static int MIN_TTD = 180;
  private boolean MIN_TTD_REACHED = false;
  private final static int INIT_TTD = 700;
  public int TTD = INIT_TTD;  // 700 ms initial ttd
  private long timePaused; // time elapsed while paused during one ttd interval
  private long ttdIntervalJunctureMillis;
  private long lastTimerStopMillis;

  public Timer timer; // fires events for natural block descent every TTD ms
  
  private int currentPiece;
  private int currentRotation;
  public int nextPiece;
  public int nextRotation;
  private int posX, posY;
  
  private final int[] bag = new int[7];
  private int currentBagIndex;
  
  public GamePanel()
  {
    // cfg ui
    basePanel = new JPanel(); // base layer 
    basePanel.setLayout(new GridLayout(1, 2));
    
    board = new Board(); // cfg model
    
    // cfg pause menu (simple static component)
    // override paintComponent to manually draw semi-transparent background
    pauseMenuPanel = new JPanel()
    {
      @Override
      protected void paintComponent(Graphics g)
      {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
      }
    };
    pauseMenuPanel.setOpaque(false);
    pauseMenuPanel.setLayout(new GridBagLayout());
    pauseMenuPanel.setVisible(false);
    
    // add a label to it
    JLabel pauseLabel = new JLabel("PAUSED");
    pauseLabel.setForeground(Color.WHITE);
    pauseLabel.setFont(new Font("Monospace", Font.BOLD, 38));
    pauseMenuPanel.add(pauseLabel);
            
    boardPanel = new BoardPanel(board);
    infoPanel = new InfoPanel(GamePanel.this);
    
    basePanel.add(boardPanel);
    basePanel.add(infoPanel);
    
    add(basePanel, JLayeredPane.DEFAULT_LAYER);
    add(pauseMenuPanel, JLayeredPane.PALETTE_LAYER);
   
    initializeBag();
    nextPiece = getNextPieceFromBag();
    nextRotation = GamePanel.getRandomNumber(4);
    generateNewPiece();
    
    initKeyInput();
    
    long initialTimeMillis = System.currentTimeMillis();
    initializeTimer(initialTimeMillis);
    startTimer(initialTimeMillis);
  }
  
  private void initializeTimer(long initializationTimeMillis)
  {
    if (timer == null)
      timer = new Timer(INIT_TTD, this);
    else
      timer.setDelay(INIT_TTD);
    
    // during initialization,
    timePaused = 0; // therefore the timer has been paused for 0 ms
    TTD = INIT_TTD; // ttd equals init_ttd (no score accrued)
    
    // initialization is a valid ttd interval juncture
    ttdIntervalJunctureMillis = initializationTimeMillis;
    
    // call ensures stoppedMillis - ttdIntervalJuncture is 0 for initial start
    stopTimer(initializationTimeMillis);
  }
  
  // initialize the bag array and shuffle it
  private void initializeBag()
  {
    for (int i = 0; i < 7; i++)
      bag[i] = i;
    
    shuffleBag();
    currentBagIndex = 0;
  }
  
  // Fisher-Yates shuffle to randomize the bag
  private void shuffleBag()
  {
    for (int i = bag.length - 1; i > 0; i--)
    {
      int j = getRandomNumber(i + 1);
      int temp = bag[i];
      bag[i] = bag[j];
      bag[j] = temp;
    }
  }
  
  // get the next piece from the bag, reshuffling when the bag is empty
  private int getNextPieceFromBag()
  {
    // if we've used all pieces in the bag
    if (currentBagIndex >= bag.length)
    {
      shuffleBag();         // reshuffle it
      currentBagIndex = 0;  // reset the index for iteration
    }
    return bag[currentBagIndex++];
  }
  
  private void initKeyInput()
  {
    this.addKeyListener(this);
    this.setFocusable(true);
    this.requestFocusInWindow();
  }
  
  private void updateTTDInterval()
  {
    if (!MIN_TTD_REACHED)
    {
      TTD = infoPanel.deltaTTD();
      if (TTD > MIN_TTD)
        timer.setDelay(TTD);
      else
      {
        MIN_TTD_REACHED = true;
        timer.setDelay(MIN_TTD);
      }
    }
  }
  
  private void startTimer(long startedMillis)
  {
    if (timer == null)
      return;
    
    if (!timer.isRunning())
    {
      timePaused += startedMillis - lastTimerStopMillis;
      long stoppedMillis = startedMillis - timePaused;
      
      // ignore paused intervals, decrease delay only by active time elapsed
      int remainingTTD = TTD - (int)(stoppedMillis - ttdIntervalJunctureMillis);
      int newDelay = remainingTTD > 0 ? remainingTTD : 0;

      timer.setDelay(newDelay);
      timer.start();
    }
  }
  
  private void tick()
  {
    updateTTDInterval();
    repaint();
  }
  
  private void stopTimer(long timeStopped)
  {
    if (timer == null)
      return;
    
    // second expression is only reachable and true during initialization
    if (timer.isRunning() || ttdIntervalJunctureMillis == timeStopped)
    {
      lastTimerStopMillis = timeStopped;
      timer.stop();
    }
  }
  
  private void flipTimer(long timeFlipped)
  { 
    if (timer == null)
      return;
    if (timer.isRunning()) // if timer was running on call, flip timer to off
      stopTimer(timeFlipped);
    else // if timer was not running on call, flip timer to on
      startTimer(timeFlipped);
  }
  
  private void reinitializeGame()
  {
    long initialTimeMillis = System.currentTimeMillis();
    
    if (this.timer != null)
      stopTimer(initialTimeMillis);
    
    basePanel.removeAll();
    
    board = new Board();
    boardPanel = new BoardPanel(board);
    infoPanel = new InfoPanel(GamePanel.this);
    
    basePanel.add(boardPanel);
    basePanel.add(infoPanel);
   
    nextPiece = GamePanel.getRandomNumber(7);
    nextRotation = GamePanel.getRandomNumber(4);
    generateNewPiece();
    
    initializeTimer(initialTimeMillis);
    startTimer(initialTimeMillis);
    
    revalidate();
    repaint();
  }
  
  private void generateNewPiece()
  {
    // assign next piece and rotation as current piece
    currentPiece = nextPiece;
    currentRotation = nextRotation;
    
    // set the initial position of x and y, based on piece and rotation type
    setPieceOffset(currentPiece, currentRotation);
    
    // randomly select the next piece and rotation for the upcoming piece
    nextPiece = getNextPieceFromBag();
    nextRotation = GamePanel.getRandomNumber(4);
    
    boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
    infoPanel.updateIncomingPieceInfo(nextPiece, nextRotation);
  }
  
  private static int getRandomNumber(int end)
  {
    return ((int)(Math.random() * end));
  }
  
  private void setPieceOffset(int curPiece, int curRotation)
  {
    posX = Pieces.getXInitialPos(curPiece, curRotation);
    posY = Pieces.getYInitialPos(curPiece, curRotation);
  }
  
  public Color fetchBlockColor(int r)
  {
    return switch (r + 1)
    {
      case 1 -> new Color(255, 223, 0);
      case 2 -> Color.RED;
      case 3 -> new Color(255, 92, 0);
      case 4 -> Color.CYAN;
      case 5 -> Color.GREEN;
      case 6 -> Color.MAGENTA;
      case 7 -> Color.PINK;
      default -> Color.WHITE;
    };
  }
  
  private boolean checkCollision()
  {
    return !board.isPossibleMovement(posX, posY, currentPiece, currentRotation);
  }
  
  public void movePieceDown()
  {
    posY++;
    infoPanel.increaseScore(1);
    
    if (checkCollision() == true)
    {
      posY--;
      board.storePiece(posX, posY, currentPiece, currentRotation);
      if (board.isGameOver() == false)
      {
        infoPanel.increaseScore(infoPanel.getLineClearScore(board.deletePossibleLines()));
        generateNewPiece();
      }
      else
      {
        reinitializeGame();
        return;
      }
    }
    boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
  }
  
  // this is for full drop until vertical collision (space bar)
  public void fullDropPiece()
  {
    do
    {
      posY++;
      infoPanel.increaseScore(2);
    } while (this.checkCollision() == false);
    
    // now store, exactly 1 position higher than collision triggering row
    board.storePiece(posX, posY - 1, currentPiece, currentRotation);
    
    if (board.isGameOver() == false)
    {
      infoPanel.increaseScore(infoPanel.getLineClearScore(board.deletePossibleLines()));
      this.generateNewPiece();
      boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
    }
    else
      this.reinitializeGame();
  }
  
  public void movePiece(int dx)
  {
    posX += dx;
    if (this.checkCollision() == true)
      posX -= dx;
    boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
  }
  
  public void rotateCW()
  {
    currentRotation = (currentRotation + 1) % 4;
    if (this.checkCollision() == true)
      currentRotation = (currentRotation - 1 + 4) % 4;
    boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
  }
  
  public void rotateCCW()
  {
    currentRotation = (currentRotation - 1 + 4) % 4;
    if (this.checkCollision() == true)
      currentRotation = (currentRotation + 1) % 4;
    boardPanel.updateCurrentPiece(currentPiece, currentRotation, posX, posY);
  }
  
  @Override
  public void keyPressed(KeyEvent e)
  { 
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
    {
      flipTimer(System.currentTimeMillis());
      pauseMenuPanel.setVisible(!timer.isRunning());
      repaint();
      return;
    }
    
    // key events in switch may result in a higher score; update ttd interval
    if (this.timer.isRunning()) // only respond to these keys if unpaused
    {
      switch (e.getKeyCode())
      {
        case KeyEvent.VK_A -> movePiece(-1);
        case KeyEvent.VK_D -> movePiece(1);
        case KeyEvent.VK_S -> movePieceDown();
        case KeyEvent.VK_Q -> rotateCCW();
        case KeyEvent.VK_E -> rotateCW();
        case KeyEvent.VK_SPACE -> fullDropPiece();
        default -> {}
      }
      // score might have changed. tick for updates to score, display
      tick();
    }
  }
  
  @Override
  public void keyReleased(KeyEvent e)
  {
    // not needed here, but could be implemented
  }
  
  @Override
  public void keyTyped(KeyEvent e)
  {
    // not relevant for our implementation
  }
  
  @Override
  public void actionPerformed(ActionEvent e)
  {
    // in scope here means event fired. update last timer event timestamp
    ttdIntervalJunctureMillis = System.currentTimeMillis();
    timePaused = 0; // it is impossible for paused time to elapse here. set to 0
    
    // timer fired, move current airborne block down one row and tick
    movePieceDown();
    tick();
  }
  
  @Override
  public void doLayout()
  {
    super.doLayout();
    pauseMenuPanel.setBounds(0, 0, getWidth(), getHeight());
    basePanel.setBounds(0, 0, getWidth(), getHeight());
  }
}