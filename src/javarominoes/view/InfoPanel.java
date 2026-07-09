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
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import javarominoes.GameController;
import javarominoes.model.TetrominoState;
import javarominoes.model.control.TetrisKeyListener;
import javax.swing.JPanel;

/**
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
  private final static int KEYCAP_XOFF_PX = 9;
  private final static int KEYCAP_YOFF_PX = 16;

  private Font currentFont;
  private int keyCapFacePx = 0;

  private final static int V_OFF = 6; // vertical offset applied to keycaps

  public InfoPanel(GameController g) {
    this.setBackground(Color.GRAY);
    game = g;
    currentScore = 0;

    currentFont = FONT_BASE;

    nextTetromino = game.getGameState().inactive();

    addComponentListener(InfoPanel.this);
  }

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

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    this.drawPiecePreviewRegion(g);
    this.drawScoreAndSpeed(g);
    this.drawControls(g);
  }

  // sorry for magic numbers this was rushed
  private void drawPiecePreviewRegion(Graphics g) {

    // adjust box dimensions here
    int lowerLim = this.getHeight() / 2;
    int displayBoxLength = (lowerLim * 6) / 7;
    int vertPad = (displayBoxLength / 8);
    int horizPad = ((this.getWidth() - displayBoxLength) / 2);

    // draw box where piece will be drawn (1) white box (2) black inner box
    g.setColor(Color.WHITE);
    g.drawRect(horizPad, vertPad, displayBoxLength, displayBoxLength);

    g.setColor(Color.BLACK);
    g.fillRect(horizPad + 1, vertPad + 1, displayBoxLength - 1, displayBoxLength - 1);

    int cellL = (displayBoxLength - 1) / 5; // adjust for smaller inner box
    TetrominoGraphics.offsetNextRender().xBy(horizPad + 1).yBy(vertPad + 1);
    TetrominoGraphics.Render.drawPiece(g, cellL, nextTetromino);
  }

  private void drawScoreAndSpeed(Graphics g) {
    g.setColor(Color.WHITE);
    g.setFont(currentFont);

    // draw the current score
    String scoreText = "Score: " + currentScore;
    g.drawString(scoreText, 10, this.getHeight() - 60);

    // draw the current drop speed in milliseconds
    int currentSpeed = deltaTTD();
    String speedText = "Drop Speed: " + currentSpeed + " ms";
    g.drawString(speedText, 10, this.getHeight() - 30);
  }

  private void drawControls(Graphics g) {
    keyCapFacePx = getKeyCapFacePixels(g);

    FontMetrics met = g.getFontMetrics(currentFont);
    int txtLnMax = met.stringWidth(TetrisKeyListener.CTRL_MAP.get("Q")) + V_OFF;
    int initXOff = keyCapFacePx + (2 * KEYCAP_XOFF_PX) + V_OFF * 2;
    
    int keyX = getWidth() - Math.max(txtLnMax, initXOff);
    int keyY = V_OFF * 2;

    for (String key : TetrisKeyListener.CTRL_MAP.keySet()) {
      drawLabelledKeyCap(g, keyX, keyY, key, TetrisKeyListener.CTRL_MAP.get(key));
      keyY += getKeycapEffectiveHeight(met) + V_OFF;
    }
  }

  private void drawLabelledKeyCap(Graphics g, int x, int y, String k, String l) {
    Graphics2D g2d = (Graphics2D) g;

    Point[] faceVerts = getKeyCapFaceVertices(keyCapFacePx);
    Point[] sidesVerts = getKeyCapSidesVertices(keyCapFacePx, faceVerts);
    Point[] sideSepVerts = getKeyCapSideSeparators(sidesVerts, faceVerts);

    offsetKeycap(faceVerts, sidesVerts, sideSepVerts, x, y);

    drawPolygonWithFill(g2d, KEYCAP_LT, faceVerts);
    drawPolygonWithFill(g2d, KEYCAP_DK, sidesVerts);
    drawLineSegmentPairs(g2d, sideSepVerts);

    FontMetrics metrics = g.getFontMetrics(currentFont);
    int keyNameWidthPx = metrics.stringWidth(k);
    int keyLabelWidthPx = metrics.stringWidth(l);
    int stringHeight = metrics.getHeight();

    int keyNameTextRunX = (keyCapFacePx / 2) - (keyNameWidthPx / 2);
    int keyNameTextRunY = (keyCapFacePx / 2);

    int keyLabelTextRunX = (keyCapFacePx / 2) - (keyLabelWidthPx / 2);
    int keyLabelTextRunY = getKeycapEffectiveHeight(metrics) - stringHeight / 2;

    g.drawString(k, x + keyNameTextRunX, y + keyNameTextRunY);
    g.drawString(l, x + keyLabelTextRunX, y + keyLabelTextRunY);
  }

  private int getKeycapEffectiveHeight(FontMetrics m) {
    return keyCapFacePx + KEYCAP_YOFF_PX + V_OFF + m.getHeight();
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

  /**
   *
   * @param g2d
   * @param c
   * @param points
   */
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

  /**
   *
   * @param g
   * @param keyCapPx
   * @return
   */
  private Point[] getKeyCapFaceVertices(int keyCapPx) {
    return new Point[]{
      new Point(0, 0),
      new Point(0, keyCapPx),
      new Point(keyCapPx, keyCapPx),
      new Point(keyCapPx, 0),
    };
  }

  /**
   *
   * @param keyCapFacePx
   * @param capFacePts
   * @return
   */
  private Point[] getKeyCapSidesVertices(int keyCapFacePx, Point[] capFacePts) {
    return new Point[]{
      capFacePts[0],
      new Point(-KEYCAP_XOFF_PX, KEYCAP_YOFF_PX),
      new Point(-KEYCAP_XOFF_PX, KEYCAP_YOFF_PX + keyCapFacePx),
      new Point(KEYCAP_XOFF_PX + keyCapFacePx, KEYCAP_YOFF_PX + keyCapFacePx),
      new Point(KEYCAP_XOFF_PX + keyCapFacePx, KEYCAP_YOFF_PX),
      capFacePts[3],
      capFacePts[2],
      capFacePts[1],
    };
  }

  /**
   *
   * @param capSdVerts
   * @param capFacePts
   * @return
   */
  private Point[] getKeyCapSideSeparators(Point[] capSdVerts, Point[] capFacePts) {
    return new Point[]{
      capFacePts[1],
      capSdVerts[2],
      capFacePts[2],
      capSdVerts[3]
    };
  }

  private int getKeyCapFacePixels(Graphics g) {
    return g.getFontMetrics(currentFont).getHeight() * 2;
  }

  private Font getProportionalFont() {
    return FONT_BASE.deriveFont(getWidth() / 38f);
  }

  @Override
  public void componentResized(ComponentEvent e) {
    currentFont = getProportionalFont();
  }

  @Override
  public void componentMoved(ComponentEvent e) {
  }

  @Override
  public void componentShown(ComponentEvent e) {
    currentFont = getProportionalFont();
  }

  @Override
  public void componentHidden(ComponentEvent e) {
  }
}
