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
 *
 * @author dylan
 */
public final class MainMenuPanel extends MenuPanel
{
  private final JLabel titleLabel;
  private final JLabel subtitleLabel;
  private final JButton playButton;
  private final JButton exitToDesktopButton;
  
  private static final String TITLE_TEXT = "Javarominoes";
  private static final String SUBTITLE_TEXT = "Developed by Dylan Connolly";
  private static final String PLAY_TEXT = "Play Game";
  private static final String EXIT_DESKTOP_TEXT = "Exit to Desktop";
  
  private static final float TITLE_PT = 58f;
  private static final float SUBTITLE_PT = 24f; 
          
  public MainMenuPanel()
  {
    super();
    
    setBackground(Color.BLACK);
    
    titleLabel = new JLabel(TITLE_TEXT);
    titleLabel.setForeground(Color.WHITE);
    titleLabel.setFont(FONTBASE.deriveFont(Font.BOLD | Font.ITALIC, TITLE_PT));
    
    subtitleLabel = new JLabel(SUBTITLE_TEXT);
    subtitleLabel.setForeground(Color.DARK_GRAY);
    subtitleLabel.setFont(FONTBASE.deriveFont(Font.ITALIC, SUBTITLE_PT));
    
    playButton = super.buildButton(PLAY_TEXT, Color.GREEN, Color.BLUE);
    exitToDesktopButton = super.buildButton(EXIT_DESKTOP_TEXT, Color.WHITE, Color.RED);
    
    initGbl();
  }
  
  public JButton getPlayButton()
  {
    return playButton != null ? playButton : null;
  }
  
  public JButton getExitToDesktopButton()
  {
    return exitToDesktopButton != null ? exitToDesktopButton : null;
  }
  
  @Override
  protected void initGbl() 
  {
    Insets headerP = new Insets(20, 0, 20, 0);
    Insets stdP = new Insets(10, 20, 10, 20);
    
    super.gblAdd(titleLabel, 0, 0, 1.0, 0.0, GridBagConstraints.CENTER, headerP);
    super.gblAdd(subtitleLabel, 0, 1, 1.0, 0.0, GridBagConstraints.CENTER, headerP);
    super.gblAdd(playButton, 0, 2, 1.0, 1.0, GridBagConstraints.CENTER, stdP);
    super.gblAdd(exitToDesktopButton, 0, 3, 1.0, 1.0, GridBagConstraints.CENTER, stdP);
  }
  
  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, getHeight(), getWidth());
    
    super.paintComponent(g);
  }
}
