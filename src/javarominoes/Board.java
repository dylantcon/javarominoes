/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

/**
 *
 * @author dylan
 */
public class Board {
  // ------------- CONSTANT MEMBER DATA -------------

  public static final int BOARD_WIDTH = 10;           // board width in blocks
  public static final int BOARD_HEIGHT = 20;          // board height in blocks
  public static final int PIECE_BLOCKS = 5;           // number of horizontal and vertical blocks of piece matrix
  public static final int ROTATOR_IDX = 2;

  // enum for state of board squares, mBoard to hold them, 
  public static final int POS_FREE = 0;
  // -----------------------------------------------

  private final int height;
  private final int width;
  
  private int mBoard[][];
  
  // -----------------------------------------------

  // sets screen height and inits board (used by actual game with std dimensions)
  public Board() {
    height = BOARD_HEIGHT;
    width = BOARD_WIDTH;
    
    initBoard();
  }
  
  // used by parallaxscrollpanel for towers in backing layers
  public Board(int h, int w) {
    height = h;
    width = w;
    
    initBoard();
  }

  // sets all squares in board to free. must have dimensions configured
  private void initBoard() {
    mBoard = new int[width][height];
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        mBoard[i][j] = POS_FREE;
      }
    }
  }

  // store a piece in the board by filling the blocks.
  // pX = current X position of TL of piece matrix, relative to board
  // pY = current Y position of TL of piece matrix, relative to board
  public void storePiece(int pX, int pY, int pPiece, int pRotation) {
    // store each square of the piece array into the board
    // j2 = current row index, i2 = current column index
    for (int j = 0; j < PIECE_BLOCKS; j++) {
      for (int i = 0; i < PIECE_BLOCKS; i++) {
        // store only the squares of the piece that aren't empty (not [0], but [1] and [2])
        if (Pieces.getBlockType(pPiece, pRotation, i, j) != 0) {
          int boardX = pX + i;
          int boardY = pY + j;

          if (boardY >= 0 && boardY < height && boardX >= 0 && boardX < width) {
            mBoard[i + pX][j + pY] = Pieces.getBlockType(pPiece, pRotation, i, j);
          }
        }
      }
    }
  }

  // check to see if the game has ended (a square attained top position)
  public boolean isGameOver() {
    // if any square on the top line has a square, you lose ( pos [i][0] )
    for (int i = 0; i < BOARD_WIDTH; i++) {
      if (mBoard[i][0] != POS_FREE) {
        return true;
      }
    }
    return false;
  }

  // delete a line of the board by moving all of the lines down
  public void deleteLine(int pY) {
    // moves all the upper lines one row down
    for (int j = pY; j > 0; j--) {
      for (int i = 0; i < width; i++) {
        mBoard[i][j] = mBoard[i][j - 1];
      }
    }
  }

  // delete all lines that should be removed
  public int deletePossibleLines() {
    int numCleared = 0;
    for (int j = 0; j < height; j++) {
      int i = 0;
      while (i < width) {
        // if current square on current line has a non-filled element, break early
        if (mBoard[i][j] == POS_FREE) {
          break;
        }
        i++;        // otherwise, increment i to continue checking
      }
      // if i has iterated to boardWidth, all squares on line are filled. delete it
      if (i == width) {
        deleteLine(j);
        numCleared++;
      }
    }
    return numCleared;
  }

  public int getBlockType(int i2, int j2) {
    return mBoard[i2][j2];
  }

  // used for backtracking during tower generation
  public void clearBlock(int i2, int j2) {
    mBoard[i2][j2] = POS_FREE;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  // returns state of board square at coordinates pX, pY
  public boolean isFreeBlock(int i2, int j2) {
    return (mBoard[i2][j2] == POS_FREE);
  }

  // check if piece can be stored at given position without any collision
  // note: allows pieces to extend above board (negative Y) for spawning
  public boolean isPossibleMovement(int pX, int pY, int pPiece, int pRotation) {
    for (int j = 0; j < PIECE_BLOCKS; j++) {
      for (int i = 0; i < PIECE_BLOCKS; i++) {
        if (Pieces.getBlockType(pPiece, pRotation, i, j) != 0) {
          int boardX = pX + i;
          int boardY = pY + j;
          if (boardX < 0 || boardX >= width || boardY >= height) {
            return false;
          }
          if (boardY >= 0 && !isFreeBlock(boardX, boardY)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  // check if piece is fully within bounds with no collision
  // unlike isPossibleMovement, rejects pieces extending above board
  public boolean containsTetromino(int pX, int pY, int pPiece, int pRotation) {
    for (int j = 0; j < PIECE_BLOCKS; j++) {
      for (int i = 0; i < PIECE_BLOCKS; i++) {
        if (Pieces.getBlockType(pPiece, pRotation, i, j) != 0) {
          int boardX = pX + i;
          int boardY = pY + j;
          if (boardX < 0 || boardX >= width || boardY < 0 || boardY >= height) {
            return false;
          }
          if (!isFreeBlock(boardX, boardY)) {
            return false;
          }
        }
      }
    }
    return true;
  }

} // end class Board
