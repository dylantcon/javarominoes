/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model;

import java.util.ArrayList;
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

  /**
   * Whether a tetromino has come to rest with any of its four blocks above the
   * ceiling, which is to say above row 0, the topmost row of the field.
   *
   * <p>
   * This is the original title's losing condition. A piece which fits wholly
   * within row 0 is a legal placement, and it is only the piece which cannot
   * fit beneath the ceiling that ends the game. The predicate this replaces
   * asked instead whether any block occupied row 0 at all, which killed the
   * player one full row early, and killed him outright for laying a flat 'I'
   * into the topmost row.</p>
   *
   * <p>
   * The question must be put to the piece rather than to the board. Blocks
   * above the ceiling have negative board coordinates: isPossibleMovement
   * tolerates them, so that a piece may spawn above the field and descend into
   * it, and storePiece silently discards them. Neither can report the
   * condition once the piece has landed.</p>
   *
   * @author dylan
   * @param t the tetromino which has just come to rest
   * @return whether any of its blocks lies above row 0
   */
  public boolean exceedsCeiling(TetrominoState t) {
    for (int r = 0; r < PIECE_BLOCKS; ++r) {
      for (int c = 0; c < PIECE_BLOCKS; ++c) {
        if (Pieces.getBlockType(t.tyRot.f, t.tyRot.s, c, r) != POS_FREE
                && t.xy.s + r < 0) {
          return true;
        }
      }
    }
    return false;
  }

  // delete a line of the board by moving all of the lines down
  public void deleteLine(int pY) {
    // moves all the upper lines one row down
    for (int r = pY; r > 0; --r) {
      for (int c = 0; c < width; ++c) {
        mBoard[r][c] = mBoard[r - 1][c];// "i am equal to position above me"
      }
    }
    // nothing sits above row 0, so it is vacated rather than inherited
    for (int c = 0; c < width; ++c) {
      mBoard[0][c] = POS_FREE;
    }
  }

  public boolean isFullLine(int r) {
    for (int c = 0; c < width; ++c) {
      // if current square on current line has a non-filled element, break
      if (mBoard[r][c] == POS_FREE) {
        return false;
      }
    }
    return true;
  }

  /**
   * Groups the filled rows into maximal contiguous runs, each expressed as an
   * inclusive top-bottom Pair.
   *
   * <p>
   * A single Pair spanning the first and last filled row cannot describe a
   * gapped clear. A vertical 'I' may fill rows 16 and 18 while leaving a hole
   * in 17, and the range (16, 18) would delete that untouched row along with
   * them. One Pair per run keeps every reported row genuinely full.</p>
   *
   * @author dylan
   * @return the filled runs, ordered from the top of the board downward, empty
   * when no row is filled
   */
  public ArrayList<Pair<Integer, Integer>> getRangesToClear() {
    ArrayList<Pair<Integer, Integer>> runs = new ArrayList<>();
    int top = -1;

    for (int r = 0; r < height; ++r) {
      if (isFullLine(r)) {
        if (top < 0) {
          top = r; // a run opens on the first filled row after a gap
        }
      } else if (top >= 0) {
        runs.add(new Pair<>(top, r - 1)); // and closes on the row that breaks it
        top = -1;
      }
    }
    if (top >= 0) {
      runs.add(new Pair<>(top, height - 1)); // a run may abut the floor
    }
    return runs;
  }

  public static int dist(Pair<Integer, Integer> topBtm) {
    return topBtm.s - topBtm.f + 1;
  }

  // delete all lines in the inclusive range specified, return lines deleted
  public int deleteLines(Pair<Integer, Integer> topBtm) {
    if (topBtm.f > topBtm.s) return 0;
    if (topBtm.f < 0 || topBtm.f >= height) return 0;
    if (topBtm.s < 0 || topBtm.s >= height) return 0;

    // inclusive range, so if top = btm still delete!
    for (int ln = topBtm.f; ln <= topBtm.s; ++ln) {
      deleteLine(ln);
    }
    return dist(topBtm);
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
