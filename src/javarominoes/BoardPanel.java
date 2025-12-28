/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;
import javax.swing.*;
import java.awt.*;


/**
 *
 * @author dylan
 */
public class BoardPanel extends JPanel
{ 
  // inner class for the grid itself
  private class GridPanel extends JPanel
  { 
    protected int blockSize;
    private int currentPiece;
    private int currentRotation;
    private int posX, posY;
    private Color pieceColor;
    
    protected GridPanel()
    {
      this.setBackground( Color.BLACK );
    }
    
    // sets the current piece and rotation, managed by GamePanel
    public void setCurrentPiece( int p, int r, int x, int y )
    {
      this.currentPiece = p;
      this.currentRotation = r;
      this.setPieceColor();
      this.posX = x;
      this.posY = y;
    }
    
    private int getBlockSize()
    {
      return Math.max( BoardPanel.this.getHeight() / Board.BOARD_HEIGHT, 1 );
    }
    
    private void setPieceColor()
    {
      pieceColor = getBlockColor( currentPiece + 1 );
    }
    
    protected Color getBlockColor( int num )
    {
      return switch (num)
      {
        case 1 -> new Color( 255, 223, 0 );  // gold
        case 2 -> Color.RED;
        case 3 -> new Color( 255, 92, 0 );   // bright orange
        case 4 -> Color.CYAN;
        case 5 -> Color.GREEN;
        case 6 -> Color.MAGENTA;
        case 7 -> Color.PINK;
        default -> Color.WHITE;
      };
    }
    
    @Override
    public Dimension getPreferredSize()
    {
      blockSize = getBlockSize();
      return new Dimension( blockSize * Board.BOARD_WIDTH, blockSize * Board.BOARD_WIDTH );
    }
    
    @Override
    protected void paintComponent( Graphics g )  // this is responsible for drawing JUST the gridlines
    {
      // call parent paintcomponent
      super.paintComponent( g );
      
      blockSize = getBlockSize();
      int gridWidth = blockSize * 10;         // calculate total width based on block size
      g.setColor( Color.DARK_GRAY );          // gridlines will be dark gray
      
      for ( int row = 0; row < 20; row++ )
      { // line drawn from x = 0, y = row * blockSize -> x = gridWidth, y = row * blockSize
        g.drawLine( 0, row * blockSize, gridWidth, row * blockSize );
      }
      
      // draw a magenta line delineating the bottom-most row
      g.setColor( Color.WHITE );
      g.fill3DRect( 0, Board.BOARD_HEIGHT * blockSize, gridWidth, getHeight(), true );
      g.setColor( Color.DARK_GRAY );
      
      for ( int col = 0; col <= 10; col++ )
      { // line drawn from x = col * blockSize, y = 0 -> x = col * blockSize, y = getHeight()
        g.drawLine( col * blockSize, 0, col * blockSize, getHeight() );
      }
      // first draw all of the blocks already stored in Board matrix
      drawStoredBlocks( g, blockSize );
      // now, draw the silhouette of the piece
      drawPieceSilhouette( g, blockSize );
      // now call BoardPanel's method to draw the active piece
      drawCurrentPiece( g, blockSize );
    }
    
    private void drawStoredBlocks( Graphics g, int blockSize )
    {
      // loop through the board grid and draw the filled blocks
      for ( int x = 0; x < Board.BOARD_WIDTH; x++ )
      {
        for ( int y = 0; y < Board.BOARD_HEIGHT; y++ )
        {
          if ( !board.isFreeBlock( x, y ) )
          {
            g.setColor( getBlockColor( board.getBlockType( x, y ) ) );
            g.fill3DRect( x * blockSize, y * blockSize, blockSize, blockSize, true );
          }
        }
      }
    }

    private void drawCurrentPiece( Graphics g, int blockSize )
    {
      // loop through the 5x5 piece matrix to draw the current piece, guided by posX and posY
      for ( int x = 0; x < 5; x++ )
      {
        for ( int y = 0; y < 5; y++ )
        {
          if ( Pieces.getBlockType( currentPiece, currentRotation, x, y ) != 0 )
          {
            g.setColor( pieceColor );
            // shade the rotator block darker when we reach it
            if ( x == 2 && y == 2 )
              g.setColor( pieceColor.darker() );
            
            int drawX = ( x + posX ) * blockSize;
            int drawY = ( y + posY ) * blockSize;
            g.fill3DRect( drawX, drawY, blockSize, blockSize, true );       
          }
        }
      }
    }
    
    private void drawPieceSilhouette( Graphics g, int blockSize )
    {
      g.setColor( new Color( 255, 255, 255, 50 ) );
      
      int tempX = posX;
      int tempY = posY;
      
      while ( board.isPossibleMovement( tempX, tempY, currentPiece, currentRotation ) )
        tempY++;
      tempY--;
      
      // loop through the 5x5 piece matrix to draw the current piece, guided by tempX and tempY
      for ( int x = 0; x < 5; x++ )
      {
        for ( int y = 0; y < 5; y++ )
        {
          if ( Pieces.getBlockType( currentPiece, currentRotation, x, y ) != 0 )
          {
            int drawX = ( x + tempX ) * blockSize;
            int drawY = ( y + tempY ) * blockSize;
            g.fill3DRect( drawX, drawY, blockSize, blockSize, true );
          }
        }
      }
    }
    
  } // end inner class 1 GridPanel
  
  private class PaddingPanel extends JPanel
  {
    protected GridPanel gP;
    
    protected PaddingPanel( GridPanel g )
    {
      gP = g;
    }
    
    @Override
    public void paintComponent( Graphics g )
    {
      super.paintComponent( g );
      g.setColor( Color.YELLOW );
      g.fill3DRect( 0, 0, this.getWidth(), this.getHeight(), true );
      
      // apply a gradient on the borders, use graphics2d
      Graphics2D g2d = (Graphics2D) g;
      GradientPaint mgGrad;                                                 // moss green
      mgGrad = new GradientPaint( 0,               0,                new Color( 17, 66, 50, 0 ), 
                                  this.getWidth(), this.getHeight(), new Color( 17, 66, 50, 200 ) );
      g2d.setPaint( mgGrad );
      g2d.fillRect( 0, 0, this.getWidth(), this.getHeight() );
    }
    
    @Override
    public Dimension getPreferredSize()
    {
      int availableWidth = ( BoardPanel.this.getWidth() - gP.getPreferredSize().width ) / 2;
      return new Dimension( Math.max( availableWidth, 0 ), BoardPanel.this.getHeight() );
    }
  } // end inner class 2 PaddingPanel
  
  private final Board board;                 // board object representing the static grid
  private final GridPanel gridpanel;
  private final PaddingPanel[] pads;
  
  // constructor initializes the board and layout
  public BoardPanel( Board b )
  {
    board = b;
    
    this.setLayout( new BorderLayout() );       // specify the border layout, so that we can have padding
    gridpanel = new GridPanel();                // create a new JPanel with overridden paintComponent method
    
    pads = new PaddingPanel[] { new PaddingPanel( gridpanel ), new PaddingPanel( gridpanel ) };
      
    this.add( pads[0], BorderLayout.EAST );
    this.add( gridpanel, BorderLayout.CENTER );    // add the grid panel to the center of the layout
    this.add( pads[1], BorderLayout.WEST );
  }
 
  @Override
  public void paintComponent( Graphics g )
  {
    super.paintComponent( g );
    repaint();
  }
  
  // this is used by the GamePanel manager class when a new Piece is created or a rotation is performed
  public void updateCurrentPiece( int piece, int rotation, int x, int y )
  {
    // update the GridPanel inner class to have the rendering reflect the changes to the block
    gridpanel.setCurrentPiece( piece, rotation, x, y );
    this.repaint();
  }
}