/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model;

import javarominoes.model.util.Pair;

/**
 *
 * @author dylan
 */
public class Board {
  // ------------- CONSTANT MEMBER DATA -------------

  public static final int WIDTH = 10;           // board width in blocks
  public static final int HEIGHT = 20;          // board height in blocks
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
    height = HEIGHT;
    width = WIDTH;

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
    mBoard = new int[height][width];
    for (int r = 0; r < height; ++r) {
      for (int c = 0; c < width; ++c) {
        mBoard[r][c] = POS_FREE;
      }
    }
  }

  // store a piece in the board by filling the blocks.
  // pX = current X position of TL of piece matrix, relative to board
  // pY = current Y position of TL of piece matrix, relative to board
  public void storePiece(int pX, int pY, int pPiece, int pRotation) {
    // store each square of the piece array into the board
    // j2 = current row index, i2 = current column index
    for (int r = 0; r < PIECE_BLOCKS; ++r) {
      for (int c = 0; c < PIECE_BLOCKS; ++c) {
        // store only the squares of the piece that aren't empty (not [0], but [1] and [2])
        if (Pieces.getBlockType(pPiece, pRotation, c, r) != 0) {
          int boardX = pX + c;
          int boardY = pY + r;

          if (boardY >= 0 && boardY < height && boardX >= 0 && boardX < width) {
            mBoard[boardY][boardX] = Pieces.getBlockType(pPiece, pRotation, c, r);
          }
        }
      }
    }
  }

  public void storePiece(TetrominoState t) {
    storePiece(t.xy.f, t.xy.s, t.tyRot.f, t.tyRot.s);
  }

  // check to see if the game has ended (a square attained top position)
  public boolean isGameOver() {
    // if any square on the top line has a square, you lose ( pos [i][0] )
    for (int c = 0; c < WIDTH; ++c) {
      if (mBoard[0][c] != POS_FREE) {
        return true;
      }
    }
    return false;
  }

  // delete a line of the board by moving all of the lines down
  public void deleteLine(int pY) {
    // moves all the upper lines one row down
    for (int r = pY; r > 1; --r) {
      for (int c = 0; c < width; ++c) {
        mBoard[r][c] = mBoard[r - 1][c];// "i am equal to position above me"
      }
    }
  }

  public Pair<Integer, Integer> getRangeToClear() {
    int[] buffer = new int[height];
    int numClearable = 0;
    for (int r = 0; r < height; ++r) {
      int c = 0;
      while (c < width) {
        // if current square on current line has a non-filled element, break
        if (mBoard[r][c] == POS_FREE) {
          break;
        }
        ++c;// otherwise, increment i to continue checking
      }
      // if i has iterated to boardWidth, all squares on line are filled
      if (c == width) {
        buffer[numClearable++] = r;
      }
    }
    return numClearable != 0 ? new Pair<>(buffer[0], buffer[numClearable - 1]) : null;

  }

  public static int dist(Pair<Integer, Integer> topBtm) {
    return topBtm.s - topBtm.f + 1;
  }

  // delete all lines in the inclusive range specified, return lines deleted
  public int deleteLines(Pair<Integer, Integer> topBtm) {
    if (topBtm.f > topBtm.s) return 0;
    if (topBtm.f < 0 && topBtm.f > Board.HEIGHT) return 0;
    if (topBtm.s < 0 && topBtm.s > Board.HEIGHT) return 0;

    // inclusive range, so if top = btm still delete!
    for (int ln = topBtm.f; ln <= topBtm.s; ++ln) {
      deleteLine(ln);
    }
    return dist(topBtm) + 1;
  }

  public int getBlockType(int x2, int y2) {
    return mBoard[y2][x2];
  }

  // used for backtracking during tower generation
  public void clearBlock(int x2, int y2) {
      mBoard[y2][x2] = POS_FREE;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  // returns state of board square at coordinates pX, pY
  public boolean isFreeBlock(int i2, int j2) {
    return (mBoard[j2][i2] == POS_FREE);
  }

  // check if piece can be stored at given position without any collision
  // note: allows pieces to extend above board (negative Y) for spawning
  public boolean isPossibleMovement(int pX, int pY, int pPiece, int pRotation) {
    for (int r = 0; r < PIECE_BLOCKS; r++) {
      for (int c = 0; c < PIECE_BLOCKS; c++) {
        if (Pieces.getBlockType(pPiece, pRotation, c, r) != 0) {
          int boardX = pX + c;
          int boardY = pY + r;
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

  public boolean isPossibleMovement(TetrominoState ts) {
    return isPossibleMovement(ts.xy.f, ts.xy.s, ts.tyRot.f, ts.tyRot.s);
  }

  @SuppressWarnings("empty-statement")
  public int getSilhouetteY(int pX, int pY, int pPiece, int pRotation) {
    // guaranteed to return as it is bounded by board height
    while (isPossibleMovement(pX, ++pY, pPiece, pRotation));

    return pY - 1; //return the last position it could fit in
  }

  public int getSilhouetteY(TetrominoState ts) {
    return getSilhouetteY(ts.xy.f, ts.xy.s, ts.tyRot.f, ts.tyRot.s);
  }

  public Pair<Integer, Integer> getSilhouetteCoordinates(TetrominoState ts) {
    return new Pair<>(ts.xy.f, getSilhouetteY(ts));
  }

  // check if piece is fully within bounds with no collision
  // unlike isPossibleMovement, rejects pieces extending above board
  public boolean containsTetromino(int pX, int pY, int pPiece, int pRotation) {
    for (int r = 0; r < PIECE_BLOCKS; ++r) {
      for (int c = 0; c < PIECE_BLOCKS; ++c) {
        if (Pieces.getBlockType(pPiece, pRotation, c, r) != 0) {
          int boardX = pX + c;
          int boardY = pY + r;
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
