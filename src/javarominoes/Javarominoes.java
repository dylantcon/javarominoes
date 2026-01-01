/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;

/**
 *
 * @author dylan
 */
public class Javarominoes implements ActionListener {
  
  private static JFrame javarominoes;
  private static GamePanel gamePanel;
  private static MainMenuPanel mainMenuPanel;
  private static ParallaxScrollPanel parallaxPanel;
  private static JLayeredPane menuContainer;

  public Javarominoes() {
    javarominoes = new JFrame("Javarominoes");
    javarominoes.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    gamePanel = new GamePanel();
    // refreshrate: 75hz, layer size geometric series common ratio: 0.8
    parallaxPanel = new ParallaxScrollPanel(75, 0.8f);
    mainMenuPanel = new MainMenuPanel();
    
    menuContainer = new JLayeredPane() {
      @Override
      public void doLayout() {
        mainMenuPanel.setBounds(0, 0, getWidth(), getHeight());
        parallaxPanel.setBounds(0, 0, getWidth(), getHeight());
        
        mainMenuPanel.repaint();
        parallaxPanel.repaint();
        
        super.doLayout();
      }
    };
    
    menuContainer.add(parallaxPanel, JLayeredPane.DEFAULT_LAYER);
    menuContainer.add(mainMenuPanel, JLayeredPane.PALETTE_LAYER);
    // now it can be added and removed when needed

    mainMenuPanel.getPlayButton().addActionListener(Javarominoes.this);
    mainMenuPanel.getExitToDesktopButton().addActionListener(Javarominoes.this);
    
    gamePanel.getPauseMenuPanel()
            .getMainMenuButton()
            .addActionListener(Javarominoes.this);

    javarominoes.setSize(640, 480);
    doFrameAddMenu();
  }

  public void startApp() {
    javarominoes.setVisible(true);
    doFrameAddMenu();
  }
  
  private void doFramePlayGame() {
    if (gamePanel == null)
      gamePanel = new GamePanel();
    
    javarominoes.add(gamePanel);
    
    javarominoes.revalidate();
    javarominoes.repaint();
    
    gamePanel.gameStartLifespan();
    gamePanel.requestFocusInWindow();
  }

  private void doFrameRemoveGame() {
    gamePanel.reinitializeGame();
    
    javarominoes.remove(gamePanel);
    javarominoes.revalidate();
    javarominoes.repaint();
  }
  
  private void doFrameAddMenu() {
    javarominoes.add(menuContainer);
    javarominoes.revalidate();
    javarominoes.repaint();
  }
  
  private void doFrameRemoveMenu() {
    javarominoes.remove(menuContainer);
    javarominoes.revalidate();
    javarominoes.repaint();
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
    if (gamePanel != null) {
      if (e.getSource() == gamePanel.getPauseMenuPanel().getMainMenuButton()) {
        doFrameRemoveGame();
        doFrameAddMenu();
        return;
      }
    }

    if (e.getSource() == mainMenuPanel.getPlayButton()) {
      doFrameRemoveMenu();
      doFramePlayGame();
      return;
    }
    if (e.getSource() == mainMenuPanel.getExitToDesktopButton()) {
      javarominoes.dispose();
    }
  }

  public static void main(String[] args) {
    Javarominoes g = new Javarominoes(); // build before using
    g.startApp();
  }
}
