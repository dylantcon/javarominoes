/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

/**
 *
 * @author dylan
 */
public class Javarominoes implements ActionListener
{
  private static JFrame javarominoes;
  private static GamePanel gamePanel;
  private static MainMenuPanel mainMenuPanel;
  
  public Javarominoes()
  {
    javarominoes = new JFrame("Javarominoes");
    javarominoes.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    gamePanel = null;
    mainMenuPanel = new MainMenuPanel();
    
    mainMenuPanel.getPlayButton().addActionListener(Javarominoes.this);
    mainMenuPanel.getExitToDesktopButton().addActionListener(Javarominoes.this);
    
    javarominoes.setSize(640, 480);
    javarominoes.add(mainMenuPanel);
  }
  
  private void assignNewGamePanel()
  {
    gamePanel = new GamePanel();
    gamePanel.getPauseMenuPanel()
            .getMainMenuButton()
            .addActionListener(Javarominoes.this);
  }
  
  public void showGame()
  {
    javarominoes.setVisible(true);
  }
  
  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (gamePanel != null)
    {
      if (e.getSource() == gamePanel.getPauseMenuPanel().getMainMenuButton())
      {
        javarominoes.remove(gamePanel);
        gamePanel = null;
        javarominoes.add(mainMenuPanel);
        javarominoes.revalidate();
        javarominoes.repaint();
        return;
      }
    }
      
    if (e.getSource() == mainMenuPanel.getPlayButton())
    {
      javarominoes.remove(mainMenuPanel);
      assignNewGamePanel();
      javarominoes.add(gamePanel);
      javarominoes.revalidate();
      gamePanel.requestFocusInWindow();
      return;
    }
    if (e.getSource() == mainMenuPanel.getExitToDesktopButton())
    {
      javarominoes.dispose();
    }
  }
  
  public static void main(String[] args)
  {
    Javarominoes g = new Javarominoes();
    g.showGame();
  }
}