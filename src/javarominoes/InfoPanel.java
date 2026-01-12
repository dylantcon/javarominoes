/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.*;

/**
 *
 * @author dylan
 */
public class InfoPanel extends JPanel implements ComponentListener {

  private int incPiece;
  private int incRotation;
  private int currentScore;
  private final GamePanel game;
  
  private final static Font FONT_BASE = new Font("Monospaced", Font.BOLD, 18);
  
  private Font currentFont;

  public InfoPanel(GamePanel g) {
    this.setBackground(Color.GRAY);
    game = g;
    currentScore = 0;

    currentFont = FONT_BASE;
    
    incPiece = game.nextPiece;
    incPiece = game.nextRotation;
    
    addComponentListener(InfoPanel.this);
  }

  public void updateIncomingPieceInfo(int p, int r) {
    incPiece = p;
    incRotation = r;
    repaint();
  }

  public int getLineClearScore(int numLines) {
    return switch (numLines) {
      case 1 ->
        100;
      case 2 ->
        300;
      case 3 ->
        500;
      case 4 ->
        800;
      default ->
        0;
    };
  }

  public void increaseScore(int points) {
    currentScore += points;
    repaint();
  }

  // initially takes average of linear and logarithmic curve.
  public int deltaTTD() {
    double linear = ((-currentScore / 37) + 700);
    double logCurve = (700 - (180 * Math.log10((currentScore / 120) + 1)));
    if (currentScore < 13735) {
      return (int) ((logCurve + linear) / 2);
    }
    return (int) logCurve;
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