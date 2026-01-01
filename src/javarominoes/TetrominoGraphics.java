/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author dylan
 */
public class TetrominoGraphics {

  private static int PAD_X = 0;
  private static int PAD_Y = 0;

  public static RenderPaddingBuilder padNextRender() {
    return new RenderPaddingBuilder();
  }

  private static void clearRenderPadding() {
    PAD_X = PAD_Y = 0;
  }

  public static class RenderPaddingBuilder {

    public RenderPaddingBuilder xBy(int px) {
      TetrominoGraphics.PAD_X = px;
      return this;
    }

    public RenderPaddingBuilder yBy(int px) {
      TetrominoGraphics.PAD_Y = px;
      return this;
    }
  }

  public static class Render {

    public static Color getBlockColor(int num) {
      return switch (num) {
        case 1 ->
          new Color(255, 223, 0);  // gold
        case 2 ->
          Color.RED;
        case 3 ->
          new Color(255, 92, 0);   // bright orange
        case 4 ->
          Color.CYAN;
        case 5 ->
          Color.GREEN;
        case 6 ->
          Color.MAGENTA;
        case 7 ->
          Color.PINK;
        default ->
          Color.WHITE;
      };
    }

    // not paddable, only used internally by board panel. can use board color
    // directly (no zero-indexing)
    public static void drawStaticBoardBlocks(Graphics g, Board b, int bPx) {
      for (int column = 0; column < Board.BOARD_WIDTH; column++) {
        for (int row = 0; row < Board.BOARD_HEIGHT; row++) {
          if (!b.isFreeBlock(column, row)) {
            g.setColor(getBlockColor(b.getBlockType(column, row)));
            g.fill3DRect(column * bPx, row * bPx, bPx, bPx, true);
          }
        }
      }
    }

    /*
    * gX , gY are block coordinates of the top left of the piece matrix, and it
    * is assumed that the origin of the coordinate system corresponds to 0px,0px
    * on the targeted JComponent's graphics context. Hence, targeted JComponent 
    * is effectively subdivided into contiguous squares, that may or may not be
    * part of a game board. To render a piece without the grid position offset,
    * simply pass gX and gY as zero. pc is the index of the specific piece matrix
     */
    public static void drawPiece(Graphics g, int bPx, int pc, int rot,
            /**
             * ***
             */
            int gX, int gY, boolean showRotator, Color override) {
      // must have graphics. assume parameters passed correctly
      if (g == null) {
        return;
      }

      // piece color is deterministic, get it now to prevent excessive lookups
      Color baseColor = override == null ? getBlockColor(pc + 1) : override;
      int xPx, yPx;
      boolean isRotator;

      // loop through piece matrix to draw requested piece pc with rotation rot
      for (int column = 0; column < Board.PIECE_BLOCKS; column++) {
        for (int row = 0; row < Board.PIECE_BLOCKS; row++) {
          // if the block is not a free position...
          if (Pieces.getBlockType(pc, rot, column, row) != Board.POS_FREE) {
            isRotator = column == Board.ROTATOR_IDX && row == Board.ROTATOR_IDX;
            xPx = ((column + gX) * bPx) + TetrominoGraphics.PAD_X;
            yPx = ((row + gY) * bPx) + TetrominoGraphics.PAD_Y;
            if (isRotator && showRotator) {
              g.setColor(baseColor.darker());
              g.fill3DRect(xPx, yPx, bPx, bPx, true);
            } else {
              g.setColor(baseColor);
              g.fill3DRect(xPx, yPx, bPx, bPx, true);
            }
          }
        }
      }
      // padding set only affects the next render. reset it to 0.
      TetrominoGraphics.clearRenderPadding();
    }
  }
}
