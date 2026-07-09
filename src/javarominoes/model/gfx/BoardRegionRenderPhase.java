/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.gfx;

import java.awt.Color;
import java.awt.Graphics;
import javarominoes.model.Board;
import javarominoes.model.GameState;

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

    // draw black background
    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, gridW, gridH);

    graphics.setColor(Color.DARK_GRAY);

    // horizontal gridlines
    for (int row = 0; row < Board.HEIGHT; ++row) {
      // line drawn from x=0,y=row*blockPixels->x=gridW,y=row*blockPixels
      graphics.drawLine(0, row * bckPix, gridW, row * bckPix);
    }

    // white strip delineating bottom most row
    graphics.setColor(Color.WHITE);
    graphics.fill3DRect(0, gridH, gridW, gridH, true);
    graphics.setColor(Color.DARK_GRAY);

    // vertical gridlines
    for (int col = 0; col <= Board.WIDTH; ++col) {
      graphics.drawLine(col * bckPix, 0, col * bckPix, gridH);
    }
  }

  @Override
  public int getRenderPhaseId() {
    return RenderPhase.Factory.ID_BRRP;
  }

}
