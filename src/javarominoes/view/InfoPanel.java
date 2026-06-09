/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.view;

import javarominoes.model.TetrominoGraphics;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javarominoes.GameController;
import javax.swing.*;

/**
 *
 * @author dylan
 */
public class InfoPanel extends JPanel implements ComponentListener {

  private int incPiece;
  private int incRotation;
  private int currentScore;
  private final GameController game;
  
  private final static Font FONT_BASE = new Font("Monospaced", Font.BOLD, 18);
  
  private final static int TRANS_Y = 700;
  private final static int LOG_OUTER_MLTPLCND = 160;
  private final static int LOG_INNER_DIVISOR = 140;
  private final static double LINEAR_DIVISOR = Math.pow(10.0, Math.sqrt(Math.E));
  private final static int LIN_LOG_INTERCEPT = 14361;
  
  private Font currentFont;

  public InfoPanel(GameController g) {
    this.setBackground(Color.GRAY);
    game = g;
    currentScore = 0;

    currentFont = FONT_BASE;
    
    incPiece = game.nextPiece;
    incRotation = game.nextRotation;
    
    addComponentListener(InfoPanel.this);
  }

  public void updateIncomingPieceInfo(int p, int r) {
    incPiece = p;
    incRotation = r;
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
    return TRANS_Y-(LOG_OUTER_MLTPLCND*Math.log10((score/LOG_INNER_DIVISOR)+1));
  }
  
  private static double linearCurve(int score) {
    return ((-score/LINEAR_DIVISOR)+TRANS_Y);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    this.drawPiecePreviewRegion(g);
    this.drawScoreAndSpeed(g);
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
    TetrominoGraphics.Render.drawPiece(g, cellL,
            incPiece, incRotation, 0, 0, false, null);
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
  
  private Font getProportionalFont() {
    return FONT_BASE.deriveFont(getWidth() / 35f);
  }

  @Override
  public void componentResized(ComponentEvent e) {
    currentFont = getProportionalFont();
  }

  @Override
  public void componentMoved(ComponentEvent e) {}
  
  @Override
  public void componentShown(ComponentEvent e) {
    currentFont = getProportionalFont();
  }
  
  @Override
  public void componentHidden(ComponentEvent e) {}
}