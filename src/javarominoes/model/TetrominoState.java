/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model;

import javarominoes.model.util.Pair;

/**
 * Specifically, this is the state of an "active" tetromino, that has been
 * generated and is queued for placement on the the board.
 *
 * @author dylan
 */
public class TetrominoState {

  /**
   * 1. Piece matrix source type index {@link Pieces}, 2. Piece matrix source
   * rotation index {@link Pieces}
   */
  public Pair<Integer, Integer> tyRot = null;

  /**
   * 1. Piece matrix center location on game grid, xPos {@link Pieces}, 2. Piece
   * matrix center location on game grid, yPos {@link Pieces},
   */
  public Pair<Integer, Integer> xy = null;
  
  /**
   * Invoked upon creation. Pieces must have a shape and a rotation to be shown,
   * but not necessarily a position on the game board.
   * 
   * @param p The piece type-rotation set index.
   * @param r The piece type-rotation matrix index.
   * @return 
   */
  public TetrominoState withTypeRot(int p, int r) {
    if (tyRot != null) {
      tyRot.withFirst(p).withSecond(r);
    } else {
      tyRot = new Pair<>(p, r);
    }
    
    return this;
  }

  public TetrominoState deltaState(TetrominoState delta) {
    return new TetrominoState()
            .withX(xy.f - delta.xy.f)
            .withY(xy.s - delta.xy.s)
            .withType(tyRot.f - delta.tyRot.f)
            .withRot(tyRot.s - delta.tyRot.s);
  }
  
  public TetrominoState setOffset() {
    if (tyRot != null) {
      xy = Pieces.spawnFor(tyRot.f, tyRot.s);
    }

    return this;
  }

  public TetrominoState decrementOffset() {
    if (tyRot != null) {
      sumXY(Pieces.inverseFor(tyRot.f, tyRot.s));
    }

    return this;
  }
  
  public TetrominoState withTypeRot(Pair<Integer, Integer> tr) {
    tyRot = copyIntegerPair(tr);
    return this;
  }
  
  public TetrominoState withPosition(Pair<Integer, Integer> pnt) {
    xy = copyIntegerPair(pnt);
    return this;
  }
  
  public static Pair<Integer, Integer> copyIntegerPair(Pair<Integer, Integer> ns) {
    return new Pair<>(ns.f, ns.s);
  }
  
  public static TetrominoState duplicate(TetrominoState t) {
    return new TetrominoState()
            .withTypeRot(t.tyRot)
            .withPosition(t.xy);
  }

  public boolean isLeftHalf() {
    return xy.f - Pieces.inverseFor(tyRot.f, tyRot.s).f < Board.WIDTH / 2;
  }

  public boolean isRotationDelta() {
    return xy.f == 0 && xy.s == 0 && tyRot.f == 0 && tyRot.s != 0;
  }

  public int getWallRepulsion() {
    if (isLeftHalf())
      return +1;
    else
      return -1;
  }

  public TetrominoState repulseWall() {
    return sumX(getWallRepulsion());
  }
  
  public TetrominoState withX(int x) {
    if (xy == null) xy = new Pair<>(null, null);
    xy.f = x;
    return this;
  }
  
  public TetrominoState withY(int y) {
    if (xy == null) xy = new Pair<>(null, null);
    xy.s = y;
    return this;
  }

  public TetrominoState withType(int type) {
    if (tyRot == null) tyRot = new Pair<>(null, null);
    tyRot.f = type;
    return this;
  }
  
  public TetrominoState withRot(int rot) {
    if (tyRot == null) tyRot = new Pair<>(null, null);
    tyRot.s = rot;
    return this;
  }

  public TetrominoState decX() {
    --xy.f;
    return this;
  }
  
  public TetrominoState decY() {
    --xy.s;
    return this;
  }
  
  public TetrominoState incX() {
    ++xy.f;
    return this;
  }
  
  public TetrominoState incY() {
    ++xy.s;
    return this;
  }
  
  public TetrominoState sumX(int dx) {
    xy.f += dx;
    return this;
  }
  
  public TetrominoState sumY(int dy) {
    xy.s += dy;
    return this;
  }

  public TetrominoState sumXY(Pair<Integer, Integer> xy) {
    return sumX(xy.f).sumY(xy.s);
  }
  
  public TetrominoState sumR(int dr) {
    tyRot.s = dr > 0 
            ? (dr + tyRot.s) % Pieces.N_ORIENTATIONS
            : (dr + tyRot.s + Pieces.N_ORIENTATIONS) % Pieces.N_ORIENTATIONS;
    return this;
  }
  
  public TetrominoState rotateCW() {
    return this.sumR(1);
  }
  
  public TetrominoState rotateCCW() {
    return this.sumR(-1);
  }
  
  /**
   * TetrominoState Factory, creates random pieces using Fisher Yates shuffle
   */
  public static class Factory {

    private final static int[] bag = new int[7];
    private static boolean readyBag = false;

    private static int currentBagIndex;

    // initialize the bag array and shuffle it
    private static void initializeBag() {
      for (int i = 0; i < 7; i++) {
        bag[i] = i;
      }

      shuffleBag();
      currentBagIndex = 0;
    }

    // Fisher-Yates shuffle to randomize the bag
    private static void shuffleBag() {
      for (int i = bag.length - 1; i > 0; i--) {
        int j = getRandomNumber(i);
        int temp = bag[i];
        bag[i] = bag[j];
        bag[j] = temp;
      }
    }

    public static int getRandomNumber(int end) {
      return ((int) (Math.random() * end));
    }

    // get the next piece from the bag, reshuffling when the bag is empty
    private static int popBag() {
      if (!readyBag) {
        initializeBag();
        readyBag = true;
      }
      
      // if we've used all pieces in the bag
      if (currentBagIndex >= bag.length) {
        shuffleBag();         // reshuffle it
        currentBagIndex = 0;  // reset the index for iteration
      }
      return bag[currentBagIndex++];
    }

    public static TetrominoState create(int pPiece) {
      return new TetrominoState()
              .withTypeRot(pPiece, getRandomNumber(Pieces.N_ORIENTATIONS));
    }

    public static TetrominoState yates() {
      return create(popBag());
    }
    
    public static TetrominoState silhouetteCopy(TetrominoState t, int y)  {
      return TetrominoState.duplicate(t)
              .withY(y);
    }
    
    public static TetrominoState translateCopy(TetrominoState t, int dx, int dy) {
      return TetrominoState.duplicate(t)
              .sumX(dx)
              .sumY(dy);
    }

    public static TetrominoState repulseWallCopy(TetrominoState t) {
      return TetrominoState.duplicate(t).repulseWall();
    }

    public static TetrominoState kickRightCopy(TetrominoState t) {
      return TetrominoState.duplicate(t).incX();
    }

    public static TetrominoState kickLeftCopy(TetrominoState t) {
      return TetrominoState.duplicate(t).decX();
    }

    public static TetrominoState descendCopy(TetrominoState t) {
      return translateCopy(t, 0, 1);
    }
    
    public static TetrominoState rotateCopy(TetrominoState t, int dr) {
      return TetrominoState.duplicate(t)
              .sumR(dr);
    }
    
    public static TetrominoState copy(TetrominoState t) {
      return TetrominoState.duplicate(t);
    }
  }
}
