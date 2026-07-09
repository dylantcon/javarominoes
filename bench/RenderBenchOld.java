import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javarominoes.model.Board;
import javarominoes.view.BoardPanel;

/**
 * Baseline: the GridPanel as it stood at e2c2fc7, before the phased renderer.
 * Every paint redraws the gridlines, re-walks all 200 board cells to draw the
 * landed blocks, re-runs the silhouette's collision descent, and draws the
 * active piece. Every piece movement calls repaint() with no arguments, so the
 * clip is always the whole panel.
 *
 * <p>
 * Compile against a checkout of e2c2fc7. See bench/README.md.</p>
 *
 * @author dylan
 */
public class RenderBenchOld {

  static final int BPX = Integer.getInteger("bench.bpx", 30);
  static final int W = 10 * BPX, H = 20 * BPX;
  static final int WARMUP = Integer.getInteger("bench.warmup", 3000);
  static final int ITERS = Integer.getInteger("bench.iters", 20000);
  static final int FRAME_MS = Integer.getInteger("bench.frameMs", 16);
  static final int POLL_MS = Integer.getInteger("bench.pollMs", 16);
  static final int SECONDS = Integer.getInteger("bench.seconds", 3);
  static final boolean EDT_ONLY = Boolean.getBoolean("bench.edtOnly");

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");

    Board b = new Board();
    Field mb = Board.class.getDeclaredField("mBoard");
    mb.setAccessible(true);
    int[][] m = (int[][]) mb.get(b); // pre-refactor layout is [width][height]
    for (int y = 8; y < 20; ++y) {
      for (int x = 0; x < 10; ++x) {
        if ((x * 7 + y * 3) % 5 != 0) {
          m[x][y] = (x + y) % 7 + 1;
        }
      }
    }

    BoardPanel bp = new BoardPanel(b);
    bp.setSize(W + 80, H);

    Field gpF = BoardPanel.class.getDeclaredField("gridpanel");
    gpF.setAccessible(true);
    final Object gp = gpF.get(bp);
    ((JComponent) gp).setSize(W, H);

    final Method setPiece = gp.getClass().getDeclaredMethod("setCurrentPiece",
            int.class, int.class, int.class, int.class);
    setPiece.setAccessible(true);
    final Method paint = gp.getClass().getDeclaredMethod("paintComponent", Graphics.class);
    paint.setAccessible(true);

    BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
    final Graphics2D g = img.createGraphics();

    final int[] tick = new int[1];

    Runnable fullPaint = () -> {
      try {
        g.setClip(0, 0, W, H);
        paint.invoke(gp, g);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

    // one piece movement, exactly as updateCurrentPiece drives it
    Runnable moveCycle = () -> {
      try {
        setPiece.invoke(gp, 2, 0, 3 + (tick[0]++ & 1), 2);
        g.setClip(0, 0, W, H); // repaint() with no args: the whole panel
        paint.invoke(gp, g);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

    setPiece.invoke(gp, 2, 0, 3, 2);

    if (Boolean.getBoolean("bench.count")) {
      CountingGraphics.Counts n = new CountingGraphics.Counts();
      CountingGraphics cg = new CountingGraphics(img.createGraphics(), n);
      cg.setClip(0, 0, W, H);
      paint.invoke(gp, cg);
      System.out.println("OLD paint_" + n);
      System.exit(0);
    }

    if (!EDT_ONLY) {
      BenchSupport.throughput("OLD full_paint_ns", fullPaint, WARMUP, ITERS);
      BenchSupport.throughput("OLD move_cycle_ns", moveCycle, WARMUP, ITERS);
      System.out.println("OLD move_clip_px=" + (W * H));
    }
    BenchSupport.edtContention("OLD ", moveCycle, FRAME_MS, POLL_MS, SECONDS);

    g.dispose();
    System.exit(0);
  }
}
