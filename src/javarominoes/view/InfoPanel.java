/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.view;

import java.awt.Color;
import javarominoes.model.gfx.TetrominoGraphics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javarominoes.GameController;
import javarominoes.model.GridZone;
import javarominoes.model.Pieces;
import javarominoes.model.TetrominoState;
import javarominoes.model.control.TetrisKeyListener;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * The right-hand panel: the incoming piece, the score and drop-speed readouts,
 * and a legend of the keyboard controls drawn as a column of isometric keycaps.
 *
 * <p>
 * Every dimension here is derived from the panel's own, so the whole thing
 * scales with the window. The readout font is the one proportional quantity
 * from which the rest descends: the keycap font is derived from it, the keycap
 * face is sized by that font's metrics, and the skirt offsets are sized by the
 * face. See {@link #idealKeyCapXOffsetPx()}.</p>
 *
 * @author dylan
 */
public class InfoPanel extends JPanel implements ComponentListener {

  private TetrominoState nextTetromino;
  private int currentScore;
  private final GameController game;

  private final static Font FONT_BASE = new Font("Monospaced", Font.BOLD, 18);

  private final static int TRANS_Y = 700;
  private final static int LOG_OUTER_MLTPLCND = 160;
  private final static int LOG_INNER_DIVISOR = 140;
  private final static double LINEAR_DIVISOR = Math.pow(10.0, Math.sqrt(Math.E));
  private final static int LIN_LOG_INTERCEPT = 14361;

  private final static Color KEYCAP_LT = new Color(0xC3C3C3);
  private final static Color KEYCAP_DK = new Color(0x7F7F7F);
  private final static Color PREVIEW_FRAME = new Color(0xE8E8E8);
  private final static Color PREVIEW_WELL = new Color(0x101010);
  private final static Color PREVIEW_BEVEL = new Color(0x5A5A5A);

  /**
   * The readout font is sized at one thirty-eighth of the panel's width. The
   * keycap font is a fraction of that, which keeps the legend subordinate to
   * the readouts it sits above, and gives one knob for the legend's whole
   * scale.
   */
  private final static float READOUT_DIVISOR = 38f;
  private final static float KEYCAP_FONT_RATIO = 0.78f;
  private final static float MIN_FONT_PT = 7f;

  /**
   * A keycap's face is two lines of its own font tall, and its skirt is thrown
   * down and to the left by these fractions of the face. The ratio between them
   * is what sells the three-quarter view: too little vertical throw and the cap
   * reads flat, too much and it reads as a wall.
   */
  private final static int FACE_FONT_MULTIPLE = 2;
  private final static float XOFF_FACE_RATIO = 0.28f;
  private final static float YOFF_FACE_RATIO = 0.46f;
  private final static int MIN_OFFSET_PX = 2;

  private final static int EDGE_PAD = 8;
  private final static int ROW_GAP = 6;
  private final static float PREVIEW_HEIGHT_RATIO = 0.42f;
  private final static int PREVIEW_INSET_CELLS = 5;

  private Font readoutFont;
  private Font keycapFont;
  private int keyCapFacePx;

  /**
   * The legend is static for a given size, so it is drawn once into an image
   * and blitted thereafter. Six keycaps are twelve Path2D fills and twelve
   * strings, which is not a thing to redo whenever the score moves.
   */
  private BufferedImage legendCache;

  private boolean showControls = true;
  private final JButton controlsToggle;

  public InfoPanel(GameController g) {
    this.setBackground(Color.GRAY);
    this.setLayout(null); // the toggle is placed by doLayout, everything else painted
    game = g;
    currentScore = 0;

    readoutFont = FONT_BASE;
    keycapFont = FONT_BASE;

    nextTetromino = game.getGameState().inactive();

    controlsToggle = buildControlsToggle();
    add(controlsToggle);

    addComponentListener(InfoPanel.this);
    rescaleTypography();
  }

  // ------------------------------------------------------------------
  // the controls overlay, and the button which dismisses it
  // ------------------------------------------------------------------
  /**
   * A focusable button would take the keyboard away from GameController the
   * moment it were clicked, and the game would stop responding to the very keys
   * this legend documents.
   */
  private JButton buildControlsToggle() {
    JButton b = new JButton(toggleLabel());
    b.setFocusable(false);
    b.setFocusPainted(false);
    b.setMargin(new Insets(0, 0, 0, 0));
    b.setBackground(KEYCAP_LT);
    b.setForeground(Color.DARK_GRAY);
    b.setToolTipText("Show or hide the controls");
    b.addActionListener(e -> setControlsShown(!showControls));
    return b;
  }

  /**
   * ASCII only. The button is one glyph wide and the source stays 7-bit.
   */
  private String toggleLabel() {
    return showControls ? "X" : "?";
  }

  public void setControlsShown(boolean shown) {
    if (showControls == shown) {
      return;
    }
    showControls = shown;
    controlsToggle.setText(toggleLabel());
    legendCache = null; // the preview reclaims the gutter, so both must redraw
    revalidate();
    repaint();
  }

  private int toggleSizePx() {
    return clamp(getWidth() / 14, 16, 30);
  }

  @Override
  public void doLayout() {
    super.doLayout();
    int s = toggleSizePx();
    controlsToggle.setBounds(getWidth() - s - EDGE_PAD, EDGE_PAD, s, s);
  }

  // ------------------------------------------------------------------
  // model plumbing, unchanged
  // ------------------------------------------------------------------
  public void updateIncomingPieceInfo(TetrominoState next) {
    nextTetromino = next;
    repaint();
  }

  public int getLineClearScore(int numLines) {
    switch (numLines) {
      case 1:
        return 100;
      case 2:
        return 300;
      case 3:
        return 500;
      case 4:
        return 800;
      default:
        return 0;
    }
  }

  public void increaseScore(int points) {
    currentScore += points;
    repaint();
  }

  public void increaseScore() {
    increaseScore(1);
  }

  // initially takes average of linear and logarithmic curve.
  public int deltaTTD() {
    double linear = linearCurve(currentScore);
    double logCurve = logarithmicCurve(currentScore);
    if (currentScore < LIN_LOG_INTERCEPT) {
      return (int) ((logCurve + linear) / 2);
    }
    return (int) logCurve;
  }

  private static double logarithmicCurve(int score) {
    return TRANS_Y - (LOG_OUTER_MLTPLCND * Math.log10((score / LOG_INNER_DIVISOR) + 1));
  }

  private static double linearCurve(int score) {
    return ((-score / LINEAR_DIVISOR) + TRANS_Y);
  }

  // ------------------------------------------------------------------
  // typography: one proportional quantity, everything else derived
  // ------------------------------------------------------------------
  private void rescaleTypography() {
    float readoutPt = Math.max(getWidth() / READOUT_DIVISOR, MIN_FONT_PT);
    readoutFont = FONT_BASE.deriveFont(readoutPt);
    keycapFont = readoutFont.deriveFont(Math.max(readoutPt * KEYCAP_FONT_RATIO, MIN_FONT_PT));

    // the face wants to be two lines of its own font tall, but six of them
    // stacked must still fit between the toggle and the readouts. a squat
    // panel gets squat caps rather than a legend running off its bottom edge
    FontMetrics capM = getFontMetrics(keycapFont);
    int rows = Math.max(1, TetrisKeyListener.CTRL_MAP.size());
    int perRow = legendAreaHeightPx() / rows;
    int faceFromFont = capM.getHeight() * FACE_FONT_MULTIPLE;
    int faceThatFits = perRow - capM.getHeight() - ROW_GAP - MIN_OFFSET_PX;
    keyCapFacePx = Math.max(capM.getHeight(), Math.min(faceFromFont, faceThatFits));

    controlsToggle.setFont(readoutFont);
    legendCache = null; // every cached pixel was sized by the old fonts
  }

  private static int clamp(int v, int lo, int hi) {
    return Math.max(lo, Math.min(hi, v));
  }

  // ------------------------------------------------------------------
  // keycap geometry
  // ------------------------------------------------------------------
  /**
   * The horizontal throw of a keycap's skirt.
   *
   * <p>
   * Proportional to the face, so the cap keeps its proportions as the panel
   * scales, but never so wide that the cap, both of its skirts, and the widest
   * label cannot sit side by side within the panel. On a panel narrow enough
   * for that to bind, the cap flattens toward a plan view rather than
   * overrunning its gutter.</p>
   *
   * @return the face-to-side offset in x, at least MIN_OFFSET_PX
   */
  private int idealKeyCapXOffsetPx() {
    int fromFace = Math.round(keyCapFacePx * XOFF_FACE_RATIO);
    int spare = getWidth() - (2 * EDGE_PAD) - keyCapFacePx - widestLabelPx();
    return clamp(Math.min(fromFace, spare / 2), MIN_OFFSET_PX, keyCapFacePx);
  }

  /**
   * The vertical throw of a keycap's skirt.
   *
   * <p>
   * Proportional to the face on the same principle, and bounded by the height
   * each of the legend's rows may claim. A short panel divided among six caps
   * leaves little room per row; rather than let the skirts collide with the
   * label beneath them, the throw shrinks and the caps read as viewed from
   * nearer to head on.</p>
   *
   * @return the face-to-side offset in y, at least MIN_OFFSET_PX
   */
  private int idealKeyCapYOffsetPx() {
    int fromFace = Math.round(keyCapFacePx * YOFF_FACE_RATIO);
    int rows = Math.max(1, TetrisKeyListener.CTRL_MAP.size());
    int perRow = legendAreaHeightPx() / rows;
    int spare = perRow - keyCapFacePx - getFontMetrics(keycapFont).getHeight() - ROW_GAP;
    return clamp(Math.min(fromFace, spare), MIN_OFFSET_PX, keyCapFacePx);
  }

  private int widestLabelPx() {
    FontMetrics m = getFontMetrics(keycapFont);
    int widest = 0;
    for (String label : TetrisKeyListener.CTRL_MAP.values()) {
      widest = Math.max(widest, m.stringWidth(label));
    }
    return widest;
  }

  private int legendTopPx() {
    return EDGE_PAD + toggleSizePx() + ROW_GAP;
  }

  private int legendAreaHeightPx() {
    return Math.max(1, getHeight() - legendTopPx() - readoutBlockHeightPx() - EDGE_PAD);
  }

  private int legendRowHeightPx() {
    return keyCapFacePx + idealKeyCapYOffsetPx()
            + getFontMetrics(keycapFont).getHeight() + ROW_GAP;
  }

  private int legendWidthPx() {
    return Math.max(keyCapFacePx + (2 * idealKeyCapXOffsetPx()), widestLabelPx());
  }

  private int readoutBlockHeightPx() {
    return getFontMetrics(readoutFont).getHeight() * 3;
  }

  // ------------------------------------------------------------------
  // painting
  // ------------------------------------------------------------------
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    this.drawPiecePreviewRegion(g);
    this.drawScoreAndSpeed(g);
    if (showControls) {
      this.drawControls(g);
    }
  }

  /**
   * The preview well is square, as wide as the panel allows once the legend has
   * claimed its gutter, and no taller than a fixed share of the panel. Hiding
   * the legend hands its gutter back, and the well grows into it.
   */
  private Rectangle previewBounds() {
    int gutter = showControls ? legendWidthPx() + EDGE_PAD : 0;
    int availW = getWidth() - gutter - (2 * EDGE_PAD);
    int availH = Math.round(getHeight() * PREVIEW_HEIGHT_RATIO);

    int side = Math.max(Math.min(availW, availH), PREVIEW_INSET_CELLS);
    int x = EDGE_PAD + Math.max(0, (availW - side) / 2);
    int y = legendTopPx() + Math.max(0, (availH - side) / 2);

    return new Rectangle(x, y, side, side);
  }

  private void drawPiecePreviewRegion(Graphics g) {
    Rectangle box = previewBounds();

    // a bevelled well: bright frame, dark rim, black interior
    g.setColor(PREVIEW_FRAME);
    g.drawRect(box.x, box.y, box.width, box.height);
    g.setColor(PREVIEW_BEVEL);
    g.drawRect(box.x + 1, box.y + 1, box.width - 2, box.height - 2);
    g.setColor(PREVIEW_WELL);
    g.fillRect(box.x + 2, box.y + 2, box.width - 3, box.height - 3);

    if (nextTetromino == null || nextTetromino.tyRot == null) {
      return;
    }

    // one cell of the 5x5 piece matrix, then centre the piece's own bounding
    // box within the well rather than the matrix's, so that an 'I' and an 'S'
    // both sit in the middle instead of wherever their matrices put them
    int cellL = (box.width - 4) / PREVIEW_INSET_CELLS;
    GridZone bbox = GridZone.boundingBox(
            Pieces.MATRIX[nextTetromino.tyRot.f][nextTetromino.tyRot.s]);
    if (bbox == null) {
      return;
    }
    int xOff = box.x + 2 + ((box.width - 4) - (bbox.w * cellL)) / 2 - (bbox.x * cellL);
    int yOff = box.y + 2 + ((box.height - 4) - (bbox.h * cellL)) / 2 - (bbox.y * cellL);

    TetrominoGraphics.offsetNextRender().xBy(xOff).yBy(yOff);
    TetrominoGraphics.Render.drawPiece(g, cellL, nextTetromino);
  }

  private void drawScoreAndSpeed(Graphics g) {
    g.setColor(Color.WHITE);
    g.setFont(readoutFont);

    FontMetrics m = getFontMetrics(readoutFont);
    int baseline = getHeight() - EDGE_PAD - m.getDescent();

    String speedText = "Drop Speed: " + deltaTTD() + " ms";
    g.drawString(speedText, EDGE_PAD, baseline);

    String scoreText = "Score: " + currentScore;
    g.drawString(scoreText, EDGE_PAD, baseline - m.getHeight());
  }

  private void drawControls(Graphics g) {
    if (legendCache == null) {
      legendCache = buildLegendCache();
    }
    if (legendCache != null) {
      g.drawImage(legendCache, getWidth() - legendCache.getWidth() - EDGE_PAD,
              legendTopPx(), null);
    }
  }

  /**
   * Renders the whole legend once, at the current scale. Invalidated by a
   * resize and by the toggle, and by nothing else.
   *
   * <p>
   * The image is opaque, and carries the panel's own background, so that
   * blitting it is a copy rather than a per-pixel alpha composite. A
   * translucent cache measured barely faster than drawing the keycaps outright.
   * The panel's background is a flat colour, so nothing is lost by baking it
   * in; were it ever a gradient this would have to become ARGB again.</p>
   *
   * @return the legend, or null when the panel has yet to be laid out
   */
  private BufferedImage buildLegendCache() {
    int w = legendWidthPx();
    int rowH = legendRowHeightPx();
    int h = rowH * TetrisKeyListener.CTRL_MAP.size();
    if (w <= 0 || h <= 0) {
      return null;
    }

    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = img.createGraphics();
    try {
      g2d.setColor(getBackground());
      g2d.fillRect(0, 0, w, h);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
              RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setFont(keycapFont);

      int xOff = idealKeyCapXOffsetPx();
      int yOff = idealKeyCapYOffsetPx();

      // the skirt hangs xOff to the left of the face, so the face cannot start
      // at zero without the cache clipping it away
      int capX = Math.max(xOff, (w - keyCapFacePx) / 2);
      int rowY = 0;

      for (String key : TetrisKeyListener.CTRL_MAP.keySet()) {
        drawLabelledKeyCap(g2d, capX, rowY, xOff, yOff,
                key, TetrisKeyListener.CTRL_MAP.get(key));
        rowY += rowH;
      }
    } finally {
      g2d.dispose();
    }
    return img;
  }

  private void drawLabelledKeyCap(Graphics2D g2d, int x, int y, int xOff, int yOff,
          String k, String l) {

    Point[] faceVerts = getKeyCapFaceVertices(keyCapFacePx);
    Point[] sidesVerts = getKeyCapSidesVertices(keyCapFacePx, faceVerts, xOff, yOff);
    Point[] sideSepVerts = getKeyCapSideSeparators(sidesVerts, faceVerts);

    offsetKeycap(faceVerts, sidesVerts, sideSepVerts, x, y);

    drawPolygonWithFill(g2d, KEYCAP_LT, faceVerts);
    drawPolygonWithFill(g2d, KEYCAP_DK, sidesVerts);
    drawLineSegmentPairs(g2d, sideSepVerts);

    FontMetrics metrics = g2d.getFontMetrics(keycapFont);
    int keyNameWidthPx = metrics.stringWidth(k);
    int keyLabelWidthPx = metrics.stringWidth(l);

    int keyNameTextRunX = (keyCapFacePx / 2) - (keyNameWidthPx / 2);
    int keyNameTextRunY = (keyCapFacePx / 2) + (metrics.getAscent() / 2);

    int keyLabelTextRunX = (keyCapFacePx / 2) - (keyLabelWidthPx / 2);
    int keyLabelTextRunY = keyCapFacePx + yOff + ROW_GAP + metrics.getAscent();

    g2d.setColor(Color.BLACK);
    g2d.drawString(k, x + keyNameTextRunX, y + keyNameTextRunY);
    g2d.setColor(Color.WHITE);
    g2d.drawString(l, x + keyLabelTextRunX, y + keyLabelTextRunY);
  }

  private static void offsetKeycap(Point[] fc, Point[] sd, Point[] sdSep, int x, int y) {
    ArrayList<Point> seen = new ArrayList<>();
    Point[][] geometry = {fc, sd, sdSep};
    for (Point[] pointList : geometry) {
      for (Point p : pointList) {
        if (!seen.contains(p)) {
          p.x += x;
          p.y += y;
          seen.add(p);
        }
      }
    }
  }

  private void drawPolygonWithFill(Graphics2D g2d, Color c, Point... points) {

    Path2D.Double path = new Path2D.Double();
    for (int i = 0; i < points.length; ++i) {
      if (i == 0) {
        path.moveTo(points[i].x, points[i].y);
      } else {
        path.lineTo(points[i].x, points[i].y);
      }
    }

    path.closePath();
    g2d.setColor(c);
    g2d.fill(path);

    g2d.setColor(Color.BLACK);
    g2d.draw(path);
  }

  private void drawLineSegmentPairs(Graphics2D g2d, Point... pairs) {
    if (pairs.length % 2 != 0) {
      return;
    }

    Path2D.Double path = new Path2D.Double();
    g2d.setColor(Color.BLACK);
    for (int i = 0; i < pairs.length; ++i) {
      if (i % 2 == 0) {
        path.reset();
        path.moveTo(pairs[i].x, pairs[i].y);
      } else {
        path.lineTo(pairs[i].x, pairs[i].y);
        g2d.draw(path);
      }
    }
  }

  private Point[] getKeyCapFaceVertices(int keyCapPx) {
    return new Point[]{
      new Point(0, 0),
      new Point(0, keyCapPx),
      new Point(keyCapPx, keyCapPx),
      new Point(keyCapPx, 0),
    };
  }

  private Point[] getKeyCapSidesVertices(int keyCapFacePx, Point[] capFacePts,
          int xOff, int yOff) {
    return new Point[]{
      capFacePts[0],
      new Point(-xOff, yOff),
      new Point(-xOff, yOff + keyCapFacePx),
      new Point(xOff + keyCapFacePx, yOff + keyCapFacePx),
      new Point(xOff + keyCapFacePx, yOff),
      capFacePts[3],
      capFacePts[2],
      capFacePts[1],
    };
  }

  private Point[] getKeyCapSideSeparators(Point[] capSdVerts, Point[] capFacePts) {
    return new Point[]{
      capFacePts[1],
      capSdVerts[2],
      capFacePts[2],
      capSdVerts[3]
    };
  }

  // ------------------------------------------------------------------
  // resize
  // ------------------------------------------------------------------
  @Override
  public void componentResized(ComponentEvent e) {
    rescaleTypography();
    revalidate();
    repaint();
  }

  @Override
  public void componentMoved(ComponentEvent e) {
  }

  @Override
  public void componentShown(ComponentEvent e) {
    rescaleTypography();
    repaint();
  }

  @Override
  public void componentHidden(ComponentEvent e) {
  }
}
