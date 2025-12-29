/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.*;

/**
 * Designed to be a transparent overlay panel for use in classes
 * extending JLayeredPane. Buttons must be assigned event handlers
 * 
 * @author dylan
 */
public final class PauseMenuPanel extends MenuPanel 
{
  private final JComponent parent;
  
  private final JLabel pauseLabel;
  private final JButton resumeButton;
  private final JButton restartButton;
  private final JButton mainMenuButton;
  
  private static final String PAUSE_TEXT = "PAUSED";
  private static final String GAMEOVER_TEXT = "GAME OVER";
  private static final String RESUME_TEXT = "Resume";
  private static final String RESTART_TEXT = "Restart";
  private static final String MMENU_TEXT = "Return to Main Menu";
  
  // a convenience boolean
  private boolean showingPause;
  
  public PauseMenuPanel(JComponent p)
  {
    super();
    
    parent = p;
    
    setOpaque(false);
    
    pauseLabel = new JLabel(PAUSE_TEXT);
    pauseLabel.setForeground(Color.WHITE);
    pauseLabel.setFont(FONTBASE.deriveFont(Font.BOLD, LABEL_FONT_PT));
    
    resumeButton = buildButton(RESUME_TEXT, Color.YELLOW, Color.DARK_GRAY);
    restartButton = buildButton(RESTART_TEXT, Color.MAGENTA, Color.BLACK);
    mainMenuButton = buildButton(MMENU_TEXT, Color.RED, Color.BLACK);
    
    initGbl();
    
    showingPause = true;
    setVisible(false);
  }
  
  public JButton getResumeButton()
  {
    return resumeButton != null ? resumeButton : null;
  }
  
  public JButton getRestartButton()
  {
    return restartButton != null ? restartButton : null;
  }
  
  public JButton getMainMenuButton()
  {
    return mainMenuButton != null ? mainMenuButton : null;
  }
  
  public boolean isShowingPause()
  {
    return showingPause;
  }
  
  public void setPaused()
  {
    JButton excludes[] = {restartButton, mainMenuButton};
    super.showButtons(excludes);
    
    pauseLabel.setText(PAUSE_TEXT);
    pauseLabel.setForeground(Color.WHITE);
    showingPause = true;
    
    repaint();
  }
  
  public void setGameOver()
  {
    JButton excludes[] = {restartButton, mainMenuButton};
    hideButtons(excludes);
    
    pauseLabel.setText(GAMEOVER_TEXT);
    pauseLabel.setForeground(Color.RED);
    showingPause = false;
    
    repaint();
  }
  
  @Override
  protected void initGbl()
  {
    Insets headerP = new Insets(20, 0, 20, 0);
    Insets stdP = new Insets(10, 20, 10, 20);
    
    gblAdd(pauseLabel, 0, 0, 1.0, 0.0, GridBagConstraints.CENTER, headerP);
    gblAdd(resumeButton, 0, 1, 1.0, 0.0, GridBagConstraints.CENTER, stdP);
    gblAdd(restartButton, 0, 2, 1.0, 0.0, GridBagConstraints.CENTER, stdP);
    gblAdd(mainMenuButton, 0, 3, 1.0, 0.0, GridBagConstraints.CENTER, stdP);
  }    
  
  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(new Color(0, 0, 0, 150));
    g.fillRect(0, 0, parent.getWidth(), parent.getHeight());
    super.paintComponent(g);
  }
}
