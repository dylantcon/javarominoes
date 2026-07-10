/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javarominoes.model.Board;
import javarominoes.model.GameState;
import javarominoes.model.GridZone;

/**
 *
 * @author dylan
 */
public class BoardRegionRenderPhase extends AbstractRenderPhase {

  public BoardRegionRenderPhase(Graphics g, GameState gs, int bPx) {
    super(g, gs, bPx);
  }

  public BoardRegionRenderPhase(GameState gs, int bPx) {
    super(gs, bPx);
  }

  public BoardRegionRenderPhase(GameState gs) {
    super(gs);
  }

  /**
   * Every primitive here is bounded by the clip, rather than merely masked by
   * it. A zone bake redraws the gridlines which bound the zone's cells and no
   * others; a full bake, whose clip is the whole surface, is unchanged.
   */
  @Override
  public void draw() {
    if (graphics == null) {
      return;
    }
    if (bckPix == -1) {
      return;
    }

    int gridW = bckPix * Board.WIDTH;
    int gridH = bckPix * Board.HEIGHT;

    Rectangle clip = graphics.getClipBounds();
    if (clip == null) {
      clip = new Rectangle(0, 0, gridW, gridH);
    }
    Rectangle cells = TetrominoGraphics.Render.clippedCells(graphics, bckPix);

    // draw black background, over no more of the surface than is visible
    graphics.setColor(Color.BLACK);
    graphics.fillRect(clip.x, clip.y, clip.width, clip.height);

    graphics.setColor(Color.DARK_GRAY);

    // horizontal gridlines, one atop each visible row
    int lastRow = Math.min(Board.HEIGHT - 1, cells.y + cells.height);
    for (int row = cells.y; row <= lastRow; ++row) {
      // line drawn from x=0,y=row*blockPixels->x=gridW,y=row*blockPixels
      graphics.drawLine(0, row * bckPix, gridW, row * bckPix);
    }

    // white strip delineating bottom most row, only when it is in view
    if (clip.y + clip.height > gridH) {
      graphics.setColor(Color.WHITE);
      graphics.fill3DRect(0, gridH, gridW, gridH, true);
      graphics.setColor(Color.DARK_GRAY);
    }

    // vertical gridlines, one flanking each visible column
    int lastCol = Math.min(Board.WIDTH, cells.x + cells.width);
    for (int col = cells.x; col <= lastCol; ++col) {
      graphics.drawLine(col * bckPix, 0, col * bckPix, gridH);
    }

    TetrominoGraphics.Render.outlinePhase__Debug(graphics, bckPix, this);
  }

  /**
   * The board region is the board. Its outline traces the playfield.
   */
  @Override
  public GridZone debugZone() {
    return GridZone.Factory.rowBand(0, Board.HEIGHT - 1);
  }

  @Override
  public int getRenderPhaseId() {
    return RenderPhase.Factory.ID_BRRP;
  }

}
