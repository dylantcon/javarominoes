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
public class GameState {

  private Board board;
  private final Pair<TetrominoState, TetrominoState> inOut;

  public GameState(Board board) {
    this.board = board;
    inOut = new Pair<>(null, null);
  }

  public boolean downwardProbe() {
    if (inOut.hasNullItems()) return true;
    withActiveState(TetrominoState.Factory.descendCopy(inOut.f));
    return board.isPossibleMovement(active());
  }

  public Board getBoardState() {
    return board;
  }

  public TetrominoState active() {
    return inOut.f;
  }

  public TetrominoState inactive() {
    return inOut.s;
  }

  public void setBoardState(Board b) {
    board = b;
  }

  public void setActiveState(TetrominoState act) {
    inOut.withFirst(act);
  }

  public void setInactiveState(TetrominoState inact) {
    inOut.withSecond(inact);
  }
  
  public GameState withActiveState(TetrominoState act) {
    setActiveState(act);
    return this;
  }

  public GameState withInactiveState(TetrominoState inact) {
    setInactiveState(inact);
    return this;
  }
}