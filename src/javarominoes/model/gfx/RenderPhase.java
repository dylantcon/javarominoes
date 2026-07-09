/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package javarominoes.model.gfx;

import java.awt.Graphics;
import javarominoes.model.GameState;
import javarominoes.model.TetrominoState;

/**
 *
 * @author dylan
 */
public interface RenderPhase {

  public int getRenderPhaseId();
  
  public void draw();

  public static class Factory {

    public final static int ID_BRRP = 1;
    public final static int ID_FBRP = ID_BRRP << 1;
    public final static int ID_SPRP = ID_BRRP << 2;
    public final static int ID_APRP = ID_BRRP << 3;
    public final static int ID_PPRP = ID_BRRP << 4;
    public final static int ID_LCRP = ID_BRRP << 5;

    public static AbstractRenderPhase boardRegionRenderPhase(Graphics g, GameState s, int bPx) {
      return new BoardRegionRenderPhase(g, s, bPx);
    }

    public static AbstractRenderPhase fixedBlocksRenderPhase(Graphics g, GameState s, int bPx) {
      return new FixedBlocksRenderPhase(g, s, bPx);
    }

    public static AbstractRenderPhase silhouettePieceRenderPhase(Graphics g, GameState s, int bPx) {
      return new SilhouettePieceRenderPhase(g, s, bPx);
    }

    public static AbstractRenderPhase airbornePieceRenderPhase(Graphics g, GameState s, int bPx) {
      return new AirbornePieceRenderPhase(g, s, bPx);
    }

    public static AbstractRenderPhase boardRegionRenderPhase(GameState s, int bPx) {
      return new BoardRegionRenderPhase(s, bPx);
    }

    public static AbstractRenderPhase fixedBlocksRenderPhase(GameState s, int bPx) {
      return new FixedBlocksRenderPhase(s, bPx);
    }

    public static AbstractRenderPhase silhouettePieceRenderPhase(GameState s, int bPx) {
      return new SilhouettePieceRenderPhase(s, bPx);
    }

    public static AbstractRenderPhase airbornePieceRenderPhase(GameState s, int bPx) {
      return new AirbornePieceRenderPhase(s, bPx);
    }

    public static AbstractRenderPhase boardRegionRenderPhase(GameState s) {
      return new BoardRegionRenderPhase(s);
    }

    public static AbstractRenderPhase fixedBlocksRenderPhase(GameState s) {
      return new FixedBlocksRenderPhase(s);
    }

    public static AbstractRenderPhase silhouettePieceRenderPhase(GameState s) {
      return new SilhouettePieceRenderPhase(s);
    }

    public static AbstractRenderPhase airbornePieceRenderPhase(GameState s) {
      return new AirbornePieceRenderPhase(s);
    }

    public static AbstractRenderPhase piecePlacementRenderPhase(GameState s, TetrominoState landed) {
      return new PiecePlacementRenderPhase(s, landed);
    }

    public static AbstractRenderPhase lineClearRenderPhase(GameState s, int top, int btm) {
      return new LineClearRenderPhase(s, top, btm);
    }
  }
}