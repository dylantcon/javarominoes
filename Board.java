/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

/**
 *
 * @author dylan
 */
public class Board 
{
    // ------------- CONSTANT MEMBER DATA -------------
    public static final int BOARD_WIDTH = 10;           // board width in blocks
    public static final int BOARD_HEIGHT = 20;          // board height in blocks
    public static final int PIECE_BLOCKS = 5;           // number of horizontal and vertical blocks of piece matrix
    
    // enum for state of board squares, mBoard to hold them, 
    private static final int POS_FREE = 0;
    private final int mBoard[][] = new int[BOARD_WIDTH][BOARD_HEIGHT];   // indices are not constant, but starting index (address) is
    // -----------------------------------------------
    
    // sets screen height and inits board
    public Board()
    {
      initBoard();
    }
    
    // sets all squares in board to free
    private void initBoard()
    {
      for ( int i = 0; i < BOARD_WIDTH; i++ )
        for ( int j = 0; j < BOARD_HEIGHT; j++ )
          mBoard[i][j] = POS_FREE;
    }
    
    // store a piece in the board by filling the blocks.
    // pX = current X position of TL of piece matrix, relative to board
    // pY = current Y position of TL of piece matrix, relative to board
    public void storePiece( int pX, int pY, int pPiece, int pRotation )
    {
      // store each square of the piece array into the board
      // j2 = current row index, i2 = current column index
      for ( int j = 0; j < PIECE_BLOCKS; j++ )
      {
        for ( int i = 0; i < PIECE_BLOCKS; i++ )
        {
          // store only the squares of the piece that aren't empty (not [0], but [1] and [2])
          if ( Pieces.getBlockType( pPiece, pRotation, i, j ) != 0 )
          {
            int boardX = pX + i;
            int boardY = pY + j;
            
            if ( boardY >= 0 && boardY < BOARD_HEIGHT && boardX >= 0 && boardX < BOARD_WIDTH )
              mBoard[i + pX][j + pY] = Pieces.getBlockType( pPiece, pRotation, i, j );
          }
        }
      }
    }
    
    // check to see if the game has ended (a square attained top position)
    public boolean isGameOver()
    {
      // if any square on the top line has a square, you lose ( pos [i][0] )
      for ( int i = 0; i < BOARD_WIDTH; i++ )
        if ( mBoard [i][0] != POS_FREE )
          return true;
      return false;
    }
    
    // delete a line of the board by moving all of the lines down
    public void deleteLine( int pY )
    {
      // moves all the upper lines one row down
      for ( int j = pY; j > 0; j-- )
        for ( int i = 0; i < BOARD_WIDTH; i++ )
          mBoard[i][j] = mBoard[i][j - 1];
    }
    
    // delete all lines that should be removed
    public int deletePossibleLines()
    {
      int numCleared = 0;
      for ( int j = 0; j < BOARD_HEIGHT; j++ )
      {
        int i = 0;
        while ( i < BOARD_WIDTH )
        {
          // if current square on current line has a non-filled element, break early
          if ( mBoard[i][j] == POS_FREE )
            break;
          i++;        // otherwise, increment i to continue checking
        }
        // if i has iterated to boardWidth, all squares on line are filled. delete it
        if ( i == BOARD_WIDTH )
        {
          deleteLine( j );
          numCleared++;
        }
      }
      return numCleared;
    }

    public int getBlockType( int i2, int j2 )
    {
      return mBoard[i2][j2];
    }
    
    // returns state of board square at coordinates pX, pY
    public boolean isFreeBlock( int i2, int j2 )
    {
      return ( mBoard[i2][j2] == POS_FREE );
    }
    
    // check if piece can be stored at given position without any collision
    // (overlays 5x5 piece matrix in gamegrid, enforcing non-zero squares to remain in 10x20 area)
    public boolean isPossibleMovement( int pX, int pY, int pPiece, int pRotation )
    {
      // checks collision with pieces already stored in the board OR the board limits )
      for ( int j = 0; j < PIECE_BLOCKS; j++ )
      {
        for ( int i = 0; i < PIECE_BLOCKS; i++ )
        {
          if ( Pieces.getBlockType( pPiece, pRotation, i, j ) != 0 )
          {
            int boardX = pX + i;
            int boardY = pY + j;
            // check if the piece is outside the limits of the board
            if ( boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT )
              return false;
            // check if piece has collided with a block already in the board
            if ( boardY >= 0 && !isFreeBlock( boardX, boardY ) )
              return false;
          }
        }
      }
      // if all checks have been passed with no false return, then movement is possible
      return true;
    }
    
} // end class Board