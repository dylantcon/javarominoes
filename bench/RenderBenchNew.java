import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javarominoes.GameController;
import javarominoes.model.Board;
import javarominoes.model.GridZone;
import javarominoes.model.TetrominoState;
import javarominoes.model.gfx.RenderPhase;
import javarominoes.model.gfx.TetrominoGraphics;
import javarominoes.view.BoardPanel;

/**
 * The phased renderer: a baked static layer for the board region and the landed
 * blocks, resident phases for the silhouette and the airborne piece, and every
 * repaint clipped to the GridZone the event actually dirtied.
 *
 * <p>
 * Compile against the working tree. See bench/README.md.</p>
 *
 * @author dylan
 */
public class RenderBenchNew {

  static final int BPX = 30, W = 10 * BPX, H = 20 * BPX;
  static final int WARMUP = 3000, ITERS = 20000;
  static final int FRAME_MS = 16, POLL_MS = 16, SECONDS = 3;

  static Rectangle px(GridZone z) {
    return z == null ? null : new Rectangle(z.x * BPX, z.y * BPX, z.w * BPX, z.h * BPX);
  }

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");

    GameController c = new GameController();
    Board b = c.getBoard();

    Field mb = Board.class.getDeclaredField("mBoard");
    mb.setAccessible(true);
    int[][] m = (int[][]) mb.get(b); // row-major: [height][width]
    for (int y = 8; y < 20; ++y) {
      for (int x = 0; x < 10; ++x) {
        if ((x * 7 + y * 3) % 5 != 0) {
          m[y][x] = (x + y) % 7 + 1;
        }
      }
    }

    c.getGameState().setActiveState(
            new TetrominoState().withTypeRot(2, 0).withX(3).withY(2));

    BoardPanel bp = new BoardPanel(c);
    bp.setSize(W + 80, H);

    Field gpF = BoardPanel.class.getDeclaredField("gridPanel");
    gpF.setAccessible(true);
    final Object gp = gpF.get(bp);
    ((JComponent) gp).setSize(W, H);

    final Method paint = gp.getClass().getDeclaredMethod("paintComponent", Graphics.class);
    paint.setAccessible(true);
    final Field staticLayer = gp.getClass().getDeclaredField("staticLayer");
    staticLayer.setAccessible(true);

    BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
    final Graphics2D g = img.createGraphics();

    // make the silhouette and airborne phases resident, and bake the static layer
    bp.bankRenderPhase(RenderPhase.Factory.airbornePieceRenderPhase(c.getGameState()));
    bp.bankRenderPhase(RenderPhase.Factory.silhouettePieceRenderPhase(c.getGameState()));
    bp.dispatchGridPanelRerender();
    g.setClip(0, 0, W, H);
    paint.invoke(gp, g);

    Runnable fullPaint = () -> {
      try {
        g.setClip(0, 0, W, H);
        paint.invoke(gp, g);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

    final long[] clipPx = new long[2]; // [total, samples]
    final int[] tick = new int[1];

    // one piece movement: bank the footprint, drain the queue, paint the clip
    Runnable moveCycle = () -> {
      try {
        TetrominoState last = TetrominoState.Factory.copy(c.piece());
        int dx = (tick[0]++ & 1) == 0 ? 1 : -1;
        c.getGameState().setActiveState(TetrominoState.Factory.translateCopy(last, dx, 0));

        TetrominoGraphics.bankLastTetrominoFootprint(last);
        bp.bankRenderPhase(RenderPhase.Factory.airbornePieceRenderPhase(c.getGameState()));
        bp.bankRenderPhase(RenderPhase.Factory.silhouettePieceRenderPhase(c.getGameState()));
        bp.dispatchGridPanelRerender();

        Rectangle clip = px(TetrominoGraphics.getActivePieceZone());
        Rectangle sil = px(TetrominoGraphics.getSilhouettePieceZone());
        if (clip == null) {
          clip = new Rectangle(0, 0, W, H);
        } else if (sil != null) {
          clip = clip.union(sil);
        }
        clipPx[0] += (long) clip.width * clip.height;
        clipPx[1]++;

        g.setClip(clip.x, clip.y, clip.width, clip.height);
        paint.invoke(gp, g);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

    // worst case: the static layer invalidated, rebaked from scratch, repainted
    Runnable rebakePaint = () -> {
      try {
        staticLayer.set(gp, null);
        g.setClip(0, 0, W, H);
        paint.invoke(gp, g);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

    if (Boolean.getBoolean("bench.count")) {
      // a steady-state paint: blit the baked layer, then the resident phases
      CountingGraphics.Counts p = new CountingGraphics.Counts();
      CountingGraphics cg = new CountingGraphics(img.createGraphics(), p);
      cg.setClip(0, 0, W, H);
      paint.invoke(gp, cg);
      System.out.println("NEW paint_" + p);

      // a landing: the static layer rebaked. drawStaticBoardBlocks moved here,
      // and runs once per event rather than once per frame
      CountingGraphics.Counts bk = new CountingGraphics.Counts();
      BufferedImage layer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
      CountingGraphics bg = new CountingGraphics(layer.createGraphics(), bk);
      RenderPhase.Factory.boardRegionRenderPhase(bg, c.getGameState(), BPX).draw();
      TetrominoGraphics.Render.drawStaticBoardBlocks(bg, b, BPX);
      System.out.println("NEW bake_" + bk);
      System.exit(0);
    }

    BenchSupport.throughput("NEW full_paint_ns", fullPaint, WARMUP, ITERS);
    BenchSupport.throughput("NEW move_cycle_ns", moveCycle, WARMUP, ITERS);
    System.out.println("NEW move_clip_px=" + (clipPx[0] / clipPx[1]));
    BenchSupport.throughput("NEW rebake_paint_ns", rebakePaint, WARMUP / 4, ITERS / 4);

    BenchSupport.edtContention("NEW ", moveCycle, FRAME_MS, POLL_MS, SECONDS);

    g.dispose();
    System.exit(0);
  }
}
