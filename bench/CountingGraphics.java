import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;

/**
 * A Graphics which delegates everything and counts the primitives that pass
 * through it.
 *
 * <p>
 * The point of counting is that the JVM and the browser bill for rendering in
 * different currencies. Java2D rasterizes in software, so its cost tracks the
 * pixel area covered. CheerpJ maps AWT onto an HTML canvas, so its cost tracks
 * the number of primitives issued, each one crossing the boundary out of
 * WebAssembly, very nearly regardless of the area it covers.</p>
 *
 * <p>
 * Note that {@link Graphics#fill3DRect} is concrete in java.awt.Graphics: it
 * decomposes into one fillRect and four drawLines, and allocates two Colors by
 * way of brighter() and darker(). Every block drawn is therefore five
 * primitives, not one. Counting at this level sees that; reading the source
 * does not.</p>
 *
 * @author dylan
 */
public class CountingGraphics extends Graphics {

  public static final class Counts {

    public int fillRect, drawLine, drawImage, fillPoly, drawString, setColor, other;
    public long filledPx;

    public int primitives() {
      return fillRect + drawLine + drawImage + fillPoly + drawString + other;
    }

    @Override
    public String toString() {
      return String.format(
              "primitives=%d (fillRect=%d drawLine=%d drawImage=%d fillPoly=%d "
              + "drawString=%d other=%d) setColor=%d filled_px=%d",
              primitives(), fillRect, drawLine, drawImage, fillPoly,
              drawString, other, setColor, filledPx);
    }
  }

  private final Graphics g;
  public final Counts n;

  public CountingGraphics(Graphics g, Counts n) {
    this.g = g;
    this.n = n;
  }

  @Override
  public Graphics create() {
    return new CountingGraphics(g.create(), n); // children share the tally
  }

  @Override
  public void fillRect(int x, int y, int w, int h) {
    n.fillRect++;
    n.filledPx += (long) Math.max(w, 0) * Math.max(h, 0);
    g.fillRect(x, y, w, h);
  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2) {
    n.drawLine++;
    g.drawLine(x1, y1, x2, y2);
  }

  @Override
  public boolean drawImage(Image i, int x, int y, ImageObserver o) {
    n.drawImage++;
    return g.drawImage(i, x, y, o);
  }

  @Override
  public boolean drawImage(Image i, int x, int y, int w, int h, ImageObserver o) {
    n.drawImage++;
    return g.drawImage(i, x, y, w, h, o);
  }

  @Override
  public boolean drawImage(Image i, int x, int y, Color c, ImageObserver o) {
    n.drawImage++;
    return g.drawImage(i, x, y, c, o);
  }

  @Override
  public boolean drawImage(Image i, int x, int y, int w, int h, Color c, ImageObserver o) {
    n.drawImage++;
    return g.drawImage(i, x, y, w, h, c, o);
  }

  @Override
  public boolean drawImage(Image i, int dx1, int dy1, int dx2, int dy2,
          int sx1, int sy1, int sx2, int sy2, ImageObserver o) {
    n.drawImage++;
    return g.drawImage(i, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, o);
  }

  @Override
  public boolean drawImage(Image i, int dx1, int dy1, int dx2, int dy2,
          int sx1, int sy1, int sx2, int sy2, Color c, ImageObserver o) {
    n.drawImage++;
    return g.drawImage(i, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, c, o);
  }

  @Override
  public void fillPolygon(int[] x, int[] y, int c) {
    n.fillPoly++;
    g.fillPolygon(x, y, c);
  }

  @Override
  public void drawPolygon(int[] x, int[] y, int c) {
    n.other++;
    g.drawPolygon(x, y, c);
  }

  @Override
  public void drawPolyline(int[] x, int[] y, int c) {
    n.other++;
    g.drawPolyline(x, y, c);
  }

  @Override
  public void drawString(String s, int x, int y) {
    n.drawString++;
    g.drawString(s, x, y);
  }

  @Override
  public void drawString(AttributedCharacterIterator it, int x, int y) {
    n.drawString++;
    g.drawString(it, x, y);
  }

  @Override
  public void clearRect(int x, int y, int w, int h) {
    n.other++;
    g.clearRect(x, y, w, h);
  }

  @Override
  public void drawRoundRect(int x, int y, int w, int h, int aw, int ah) {
    n.other++;
    g.drawRoundRect(x, y, w, h, aw, ah);
  }

  @Override
  public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) {
    n.other++;
    g.fillRoundRect(x, y, w, h, aw, ah);
  }

  @Override
  public void drawOval(int x, int y, int w, int h) {
    n.other++;
    g.drawOval(x, y, w, h);
  }

  @Override
  public void fillOval(int x, int y, int w, int h) {
    n.other++;
    g.fillOval(x, y, w, h);
  }

  @Override
  public void drawArc(int x, int y, int w, int h, int s, int a) {
    n.other++;
    g.drawArc(x, y, w, h, s, a);
  }

  @Override
  public void fillArc(int x, int y, int w, int h, int s, int a) {
    n.other++;
    g.fillArc(x, y, w, h, s, a);
  }

  @Override
  public void copyArea(int x, int y, int w, int h, int dx, int dy) {
    n.other++;
    g.copyArea(x, y, w, h, dx, dy);
  }

  @Override
  public void setColor(Color c) {
    n.setColor++;
    g.setColor(c);
  }

  // --- pure state, no drawing, not counted ---
  @Override
  public void translate(int x, int y) {
    g.translate(x, y);
  }

  @Override
  public Color getColor() {
    return g.getColor();
  }

  @Override
  public void setPaintMode() {
    g.setPaintMode();
  }

  @Override
  public void setXORMode(Color c) {
    g.setXORMode(c);
  }

  @Override
  public Font getFont() {
    return g.getFont();
  }

  @Override
  public void setFont(Font f) {
    g.setFont(f);
  }

  @Override
  public FontMetrics getFontMetrics(Font f) {
    return g.getFontMetrics(f);
  }

  @Override
  public Rectangle getClipBounds() {
    return g.getClipBounds();
  }

  @Override
  public void clipRect(int x, int y, int w, int h) {
    g.clipRect(x, y, w, h);
  }

  @Override
  public void setClip(int x, int y, int w, int h) {
    g.setClip(x, y, w, h);
  }

  @Override
  public Shape getClip() {
    return g.getClip();
  }

  @Override
  public void setClip(Shape s) {
    g.setClip(s);
  }

  @Override
  public void dispose() {
    g.dispose();
  }
}
