/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author dylan
 */
public class InfoPanel extends JPanel {

  private int incPiece;
  private int incRotation;
  private int currentScore;
  private final GamePanel game;

  public InfoPanel(GamePanel g) {
    this.setBackground(Color.GRAY);
    game = g;
    currentScore = 0;

    incPiece = game.nextPiece;
    incPiece = game.nextRotation;
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

  // initially takes average of linear and logarithmic curve
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
    this.drawIncomingPiece(g);
    this.drawScoreAndSpeed(g);
  }

  public void drawIncomingPiece(Graphics g) {
    int lowerLim = this.getHeight() / 2;
    int displayBoxLength = (lowerLim * 3) / 4;
    int vertPad = (displayBoxLength / 8) + 2;
    int horizPad = ((this.getWidth() - displayBoxLength) / 2) + 2;

    // draw box where piece will be drawn
    g.setColor(Color.WHITE);
    g.drawRect(horizPad, vertPad, displayBoxLength, displayBoxLength);
    g.setColor(Color.BLACK);
    g.fillRect(horizPad + 1, vertPad + 1, displayBoxLength - 1, displayBoxLength - 1);

    int cellL = displayBoxLength / 5;

    TetrominoGraphics.padNextRender().xBy(horizPad).yBy(vertPad);
    TetrominoGraphics.Render.drawPiece(g, cellL,
            incPiece, incRotation, 0, 0, false, null);
  }

  public void drawScoreAndSpeed(Graphics g) {
    g.setColor(Color.WHITE);
    g.setFont(new Font("Monospaced", Font.BOLD, 16));

    // draw the current score
    String scoreText = "Score: " + currentScore;
    g.drawString(scoreText, 10, this.getHeight() - 60);

    // draw the current drop speed in milliseconds
    int currentSpeed = deltaTTD();
    String speedText = "Drop Speed: " + currentSpeed + " ms";
    g.drawString(speedText, 10, this.getHeight() - 30);
  }
}