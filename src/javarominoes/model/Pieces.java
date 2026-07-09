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
public class Pieces {

  public static final int N_TETROMINO = 7;
  public static final int N_ORIENTATIONS = 4;

  public static final int[][][][] MATRIX
          = {
            // 'square' piece
            {
              { // default pos (squares oriented s.t. [1] squares are as far right as possible)
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0},
                {0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi/2), or -90deg
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0},
                {0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi), or -180deg
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0},
                {0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0}
              },
              { // -((3pi/2)), or -270deg
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0},
                {0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0}
              }
            },
            // end of 'square' rotations

            // 'I' line piece
            {
              { // default pos (squares oriented s.t. [1] squares are as far right as possible)
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 2, 2, 2, 2},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi/2), or -90deg
                {0, 0, 0, 0, 0},
                {0, 0, 2, 0, 0},
                {0, 0, 2, 0, 0},
                {0, 0, 2, 0, 0},
                {0, 0, 2, 0, 0}
              },
              { // -(pi), or -180deg
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {2, 2, 2, 2, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -((3pi/2)), or -270deg
                {0, 0, 2, 0, 0},
                {0, 0, 2, 0, 0},
                {0, 0, 2, 0, 0},
                {0, 0, 2, 0, 0},
                {0, 0, 0, 0, 0}
              }
            },
            // end of 'I' rotations

            // 'L' piece
            {
              { // default pos (squares oriented s.t. [1] squares are as far right as possible)
                {0, 0, 0, 0, 0},
                {0, 0, 3, 0, 0},
                {0, 0, 3, 0, 0},
                {0, 0, 3, 3, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi/2), or -90deg
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 3, 3, 3, 0},
                {0, 3, 0, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi), or -180deg
                {0, 0, 0, 0, 0},
                {0, 3, 3, 0, 0},
                {0, 0, 3, 0, 0},
                {0, 0, 3, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -((3pi/2)), or -270deg
                {0, 0, 0, 0, 0},
                {0, 0, 0, 3, 0},
                {0, 3, 3, 3, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
              }
            },
            // end of 'L' rotations

            // 'mirrored L' piece 
            {
              { // default pos (squares oriented s.t. [1] squares are as far left as possible)
                {0, 0, 0, 0, 0},
                {0, 0, 4, 0, 0},
                {0, 0, 4, 0, 0},
                {0, 4, 4, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi/2), or -90deg
                {0, 0, 0, 0, 0},
                {0, 4, 0, 0, 0},
                {0, 4, 4, 4, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi), or -180deg
                {0, 0, 0, 0, 0},
                {0, 0, 4, 4, 0},
                {0, 0, 4, 0, 0},
                {0, 0, 4, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -((3pi/2)), or -270deg
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 4, 4, 4, 0},
                {0, 0, 0, 4, 0},
                {0, 0, 0, 0, 0}
              }
            },
            // end of 'mirrored L' rotations

            // 'skew' piece
            {
              { // default pos (squares oriented s.t. [1] squares are as far right as possible)
                {0, 0, 0, 0, 0},
                {0, 0, 0, 5, 0},
                {0, 0, 5, 5, 0},
                {0, 0, 5, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi/2), or -90deg
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 5, 5, 0, 0},
                {0, 0, 5, 5, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi), or -180deg
                {0, 0, 0, 0, 0},
                {0, 0, 5, 0, 0},
                {0, 5, 5, 0, 0},
                {0, 5, 0, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -((3pi/2)), or -270deg
                {0, 0, 0, 0, 0},
                {0, 5, 5, 0, 0},
                {0, 0, 5, 5, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
              }
            },
            // end of 'skew' rotations

            // 'mirrored skew' piece
            {
              { // default pos (squares oriented s.t. [1] squares are as far right as possible)
                {0, 0, 0, 0, 0},
                {0, 0, 6, 0, 0},
                {0, 0, 6, 6, 0},
                {0, 0, 0, 6, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi/2), or -90deg
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 6, 6, 0},
                {0, 6, 6, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi), or -180deg
                {0, 0, 0, 0, 0},
                {0, 6, 0, 0, 0},
                {0, 6, 6, 0, 0},
                {0, 0, 6, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -((3pi/2)), or -270deg
                {0, 0, 0, 0, 0},
                {0, 0, 6, 6, 0},
                {0, 6, 6, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
              }
            },
            // end of 'mirrored skew' rotations

            // 'T' piece
            {
              { // default pos (squares oriented s.t. [1] squares are as far right as possible)
                {0, 0, 0, 0, 0},
                {0, 0, 7, 0, 0},
                {0, 0, 7, 7, 0},
                {0, 0, 7, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi/2), or -90deg
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 7, 7, 7, 0},
                {0, 0, 7, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -(pi), or -180deg
                {0, 0, 0, 0, 0},
                {0, 0, 7, 0, 0},
                {0, 7, 7, 0, 0},
                {0, 0, 7, 0, 0},
                {0, 0, 0, 0, 0}
              },
              { // -((3pi/2)), or -270deg
                {0, 0, 0, 0, 0},
                {0, 0, 7, 0, 0},
                {0, 7, 7, 7, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
              }
            }
          // end of of 'T' rotations
          }; // end of piece matrix

  /* It is important to consider how each piece is to be oriented
    * as it is created at the top of the screen while the game is
    * playing. Tetris follows the convention that every piece that
    * spawns should:
    *
    * (1): Be oriented such that there is only ONE row of squares
    *      initially visible in the game grid as the piece spawns,
    *
    * (2): Be oriented such that the pivot square [2] is as close
    *      to the center of the 10x20 game grid as possible.
    *
    * With this in mind, it follows that each piece (& its rotations)
    * must have some 'translation' that satisfies this convention, so
    * that every piece spawned is created correctly with the 'extra'
    * upper squares contained above the game grid. Since we are working
    * in a two dimensional space, this can be represented by two nums,
    * for { horizontal translation, vertical translation }. Thus, we 
    * need another multi-dimensional array, in this case 3 dim:
    * 'int[][][] mPiecesInitial, with each dimension being:
    *
    *                    [  7  ][  4  ][  2  ]
    *                    (type)(rotate)(horiz/vert translation)
    *
    * Semantically, these translations are meant to interact with our
    * 5x5 square grid as it is superimposed onto the 10x20 game grid.
    * Every piece, prior to translation, has its 5x5 grid positioned
    * such that its right-most and upper-most sides are incident to the
    * top right of the 10x20 game grid. A positive integer represents
    * either a negative translation in the Y, or a positive translation
    * in the X. Conversely, a negative integer (which is all we will use)
    * represents an upward translation in the Y, or a leftward translation
    * in the X.
   */
  public final static int[][][] INITPOS
          = // displacement of the piece to the position where it is first drawn in the board when it is created
          {
            // 'square' piece translations
            {
              // default pos    
              {2, -3},
              // -(pi/2) or -90deg
              {2, -3},
              // -(pi) or -180deg
              {2, -3},
              // -((3pi/2)) or -270deg
              {2, -3}
            },
            // 'I' piece translations
            {
              // default pos    
              {2, -3},
              // -(pi/2) or -90deg
              {2, -5},
              // -(pi) or -180deg
              {2, -4},
              // -((3pi/2)) or -270deg
              {2, -4},
            },
            // 'L' piece translations
            {
              // default pos    
              {2, -3},
              // -(pi/2) or -90deg
              {2, -3},
              // -(pi) or -180deg
              {2, -3},
              // -((3pi/2)) or -270deg
              {2, -3}
            },
            // 'mirrored L' piece translations
            {
              // default pos    
              {2, -3},
              // -(pi/2) or -90deg
              {2, -3},
              // -(pi) or -180deg
              {2, -3},
              // -((3pi/2)) or -270deg
              {2, -3}
            },
            // 'skew' piece translations
            {
              // default pos    
              {2, -3},
              // -(pi/2) or -90deg
              {2, -3},
              // -(pi) or -180deg
              {2, -3},
              // -((3pi/2)) or -270deg
              {2, -3}
            },
            // 'mirrored skew' piece translations
            {
              // default pos    
              {2, -3},
              // -(pi/2) or -90deg
              {2, -3},
              // -(pi) or -180deg
              {2, -3},
              // -((3pi/2)) or -270deg
              {2, -3}
            },
            // 'T' piece translations
            {
              // default pos    
              {2, -3},
              // -(pi/2) or -90deg
              {2, -3},
              // -(pi) or -180deg
              {2, -3},
              // -((3pi/2)) or -270deg
              {2, -3}
            }
          };

  /**
   * **************************************************************************
   * Member fn: 'getBlockType( int pPiece, int pRotation, int pX, int pY )'
   *
   * Return the type of a square ( 0 = no-block, 1-7 = filled block, where the
   * value is the piece type + 1 )
   *
   * Parameters:
   *
   * >> pPiece: Piece to draw >> pRotation: 1 of the 4 possible rotations >> pX:
   * Horizontal position in blocks >> pY: Vertical position in blocks
   *
   * @param p
   * @param r
   * @param pX
   * @param pY
   * @return
   * **************************************************************************
   */
  public static int getBlockType(int p, int r, int pX, int pY) {
    return MATRIX[p][r][pY][pX];
  }

  /**
   * **************************************************************************
   * Member fn: 'getXInitialPos( int pPiece, int pRotation )'
   *
   * Returns the horizontal displacement of a piece that has to be applied in
   * order to create the block such that it is in the correct game grid
   * location.
   *
   * Parameters:
   *
   * >> pPiece: Piece to draw >> pRotation: 1 of the 4 possible rotations
   *
   * @param p
   * @param r
   * @return
   * **************************************************************************
   */
  private static int getXInitialPos(int p, int r) {
    return INITPOS[p][r][0];
  }

  /**
   * **************************************************************************
   * Member fn: 'getYInitialPos( int pPiece, int pRotation )'
   *
   * Returns the vertical displacement of a piece that has to be applied in
   * order to create the block such that it is in the correct game grid
   * location.
   *
   * Parameters:
   *
   * >> pPiece: Piece to draw >> pRotation: 1 of the 4 possible rotations
   *
   * @param p
   * @param r
   * @return
   * **************************************************************************
   */
  private static int getYInitialPos(int p, int r) {
    return INITPOS[p][r][1];
  }

  /**
   * Rolls initial piece spawn position, including specific offsets, into a
   * {@link Pair} object for consumption by shape factory.
   *
   * @author dylan
   * @param p
   * <p>
   * The index of the collection of piece matrices associated with a specific
   * piece type, having four elements, one for each possible 90-degree rotation
   * of that particular tetromino.</p>
   * @param r
   * <p>
   * The index of the specific piece matrix associated with one of the four
   * possible 90-degree rotations of the tetromino specified by the value of
   * parameter <code>p</code> .</p>
   * @return
   * <p>
   * A pair of integer values. The first element is between 0 and
   * <code>N_TETROMINO - 1</code>, and the second is between 0 and
   * <code>N_ORIENTATIONS - 1</code>.
   */
  public static Pair<Integer, Integer> spawnFor(int p, int r) {
    return new Pair<>(getXInitialPos(p, r), getYInitialPos(p, r));
  }


  /**
   * Rolls the inverse of an initial piece spawn position's specific offsets 
   * into a {@link Pair} object for consumption by shape factory.
   *
   * @author dylan
   * @param p
   * <p>
   * The index of the collection of piece matrices associated with a specific
   * piece type, having four elements, one for each possible 90-degree rotation
   * of that particular tetromino.</p>
   * @param r
   * <p>
   * The index of the specific piece matrix associated with one of the four
   * possible 90-degree rotations of the tetromino specified by the value of
   * parameter <code>p</code> .</p>
   * @return
   * <p>
   * A pair of integer values. The first element is the negation of a number 
   * between 0 and <code>N_TETROMINO - 1</code>, and the second is the negation
   * of a number between 0 and <code>N_ORIENTATIONS - 1</code>.
   */
  public static Pair<Integer, Integer> inverseFor(int p, int r) {
    return new Pair<>(-getXInitialPos(p, r), -getYInitialPos(p, r));
  }
}
