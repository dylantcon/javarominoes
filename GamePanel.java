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
public class GamePanel extends JPanel implements KeyListener, ActionListener
{
  private BoardPanel boardpanel;
  private InfoPanel infoP;
  public Board board;
  public final static int MIN_TTD = 180;
  private boolean MIN_TTD_REACHED = false;
  
  private int currentPiece;
  private int currentRotation;
  public int nextPiece;
  public int nextRotation;
  private int posX, posY;
  
  public Timer timer;
  private final int initTTD = 700;
  public int TTD = initTTD;  // 700 ms initial ttd
  
  private final int[] bag = new int[7];
  private int currentBagIndex;
  
  public GamePanel()
  {
    this.setLayout( new GridLayout( 1, 2 ) );
    
    board = new Board();
    boardpanel = new BoardPanel( board );
    
    this.add( boardpanel );
    infoP = new InfoPanel( GamePanel.this );
    this.add( infoP );
   
    this.initializeBag();
    nextPiece = this.getNextPieceFromBag();
    nextRotation = GamePanel.getRandomNumber( 4 );
    this.generateNewPiece();
    
    this.initKeyInput();
    
    timer = new Timer( TTD, this );
    timer.start();
  }
  
  // initialize the bag array and shuffle it
  private void initializeBag()
  {
    for ( int i = 0; i < 7; i++ )
      bag[i] = i;
    
    this.shuffleBag();
    currentBagIndex = 0;
  }
  
  // Fisher-Yates shuffle to randomize the bag
  private void shuffleBag()
  {
    for ( int i = bag.length - 1; i > 0; i-- )
    {
      int j = getRandomNumber( i + 1 );
      int temp = bag[i];
      bag[i] = bag[j];
      bag[j] = temp;
    }
  }
  
  // get the next piece from the bag, reshuffling when the bag is empty
  private int getNextPieceFromBag()
  {
    // if we've used all pieces in the bag
    if ( currentBagIndex >= bag.length )
    {
      shuffleBag();         // reshuffle it
      currentBagIndex = 0;  // reset the index for iteration
    }
    return bag[currentBagIndex++];
  }
  
  private void initKeyInput()
  {
    this.addKeyListener( this );
    this.setFocusable( true );
    this.requestFocusInWindow();
  }
  
  private void shortenTTDInterval()
  {
    if ( !MIN_TTD_REACHED )
    {
      TTD = infoP.deltaTTD();
      if ( TTD > MIN_TTD )
        timer.setDelay( TTD );
      else
      {
        MIN_TTD_REACHED = true;
        timer.setDelay( MIN_TTD );
      }
    }
  }
  
  private void generateNewPiece()
  {
    // assign next piece and rotation as current piece
    currentPiece = nextPiece;
    currentRotation = nextRotation;
    
    // set the initial position of x and y, based on piece and rotation type
    this.setPieceOffset( currentPiece, currentRotation );
    
    // randomly select the next piece and rotation for the upcoming piece
    nextPiece = this.getNextPieceFromBag();
    nextRotation = GamePanel.getRandomNumber( 4 );
    
    boardpanel.updateCurrentPiece( currentPiece, currentRotation, posX, posY );
    infoP.updateIncomingPieceInfo( nextPiece, nextRotation );
  }
  
  private static int getRandomNumber( int end )
  {
    return ( (int)( Math.random() * end ) );
  }
  
  private void setPieceOffset( int curPiece, int curRotation )
  {
    posX = Pieces.getXInitialPos( curPiece, curRotation );
    posY = Pieces.getYInitialPos( curPiece, curRotation );
  }
  
  private void reinitializeGame()
  {
    if ( this.timer != null )
      this.timer.stop();
    
    this.removeAll();
    
    this.board = new Board();
    this.boardpanel = new BoardPanel( board );
    this.infoP = new InfoPanel( GamePanel.this );
    
    this.add( boardpanel );
    this.add( infoP );
   
    this.nextPiece = GamePanel.getRandomNumber( 7 );
    this.nextRotation = GamePanel.getRandomNumber( 4 );
    this.generateNewPiece();
    
    TTD = initTTD;
    
    timer = new Timer( TTD, this );
    timer.start();
    
    this.revalidate();
    this.repaint();
  }
  
  public Color fetchBlockColor( int r )
  {
    r++;
    return switch ( r )
    {
      case 1 -> new Color( 255, 223, 0 );
      case 2 -> Color.RED;
      case 3 -> new Color( 255, 92, 0 );
      case 4 -> Color.CYAN;
      case 5 -> Color.GREEN;
      case 6 -> Color.MAGENTA;
      case 7 -> Color.PINK;
      default -> Color.WHITE;
    };
  }
  
  private boolean checkCollision()
  {
    return !board.isPossibleMovement( posX, posY, currentPiece, currentRotation );
  }
  
  public void movePieceDown()
  {
    posY++;
    infoP.increaseScore( 1 );
    this.shortenTTDInterval();
    
    if ( this.checkCollision() == true )
    {
      posY--;
      board.storePiece( posX, posY, currentPiece, currentRotation );
      if ( board.isGameOver() == false )
      {
        infoP.increaseScore( infoP.getLineClearScore( board.deletePossibleLines() ) );
        this.generateNewPiece();
      }
      else
      {
        this.reinitializeGame();
        return;
      }
    }
    boardpanel.updateCurrentPiece( currentPiece, currentRotation, posX, posY );
  }
  
  // this is for full drop until vertical collision
  public void fullDropPiece()
  {
    do
    {
      posY++;
      infoP.increaseScore( 2 );
    } while ( this.checkCollision() == false );
    
    board.storePiece( posX, posY - 1, currentPiece, currentRotation );
    
    if ( board.isGameOver() == false )
    {
      infoP.increaseScore( infoP.getLineClearScore( board.deletePossibleLines() ) );
      this.shortenTTDInterval();
      this.generateNewPiece();
      boardpanel.updateCurrentPiece( currentPiece, currentRotation, posX, posY );
    }
    else
      this.reinitializeGame();
  }
  
  public void movePiece( int dx )
  {
    posX += dx;
    if ( this.checkCollision() == true )
      posX -= dx;
    boardpanel.updateCurrentPiece( currentPiece, currentRotation, posX, posY );
  }
  
  public void rotateCW()
  {
    currentRotation = ( currentRotation + 1 ) % 4;
    if ( this.checkCollision() == true )
      currentRotation = ( currentRotation - 1 + 4 ) % 4;
    boardpanel.updateCurrentPiece( currentPiece, currentRotation, posX, posY);
  }
  
  public void rotateCCW()
  {
    currentRotation = ( currentRotation - 1 + 4 ) % 4;
    if ( this.checkCollision() == true )
      currentRotation = ( currentRotation + 1 ) % 4;
    boardpanel.updateCurrentPiece( currentPiece, currentRotation, posX, posY );
  }
  
  @Override
  public void keyPressed( KeyEvent e )
  {
    switch ( e.getKeyCode() )
    {
      case KeyEvent.VK_A -> this.movePiece( -1 );
      case KeyEvent.VK_D -> this.movePiece( 1 );
      case KeyEvent.VK_S -> this.movePieceDown();
      case KeyEvent.VK_Q -> this.rotateCCW();
      case KeyEvent.VK_E -> this.rotateCW();
      case KeyEvent.VK_SPACE -> this.fullDropPiece();
      default -> {
      }
    }
  }
  
  @Override
  public void keyReleased( KeyEvent e )
  {
    // not needed here, but could be implemented
  }
  
  @Override
  public void keyTyped( KeyEvent e )
  {
    // not relevant for our implementation
  }
  
  @Override
  public void actionPerformed( ActionEvent e )
  {
    this.movePieceDown();
  }
  
}