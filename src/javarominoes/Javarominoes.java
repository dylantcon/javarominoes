/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

import javarominoes.view.ParallaxScrollPanel;
import javarominoes.view.MainMenuPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javarominoes.model.KorobeinikiMusicHandler;
import javarominoes.model.MusicHandler;
import javarominoes.view.PauseMenuPanel;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;

/**
 *
 * @author dylan
 */
public class Javarominoes implements ActionListener {
  
  private static JFrame javarominoes;
  private static GameController gameController;
  private static MainMenuPanel mainMenuPanel;
  private static ParallaxScrollPanel parallaxPanel;
  private static JLayeredPane menuContainer;
  
  public final static int INITIAL_X = 1280;
  public final static int INITIAL_Y = 720;

  public Javarominoes() {
    javarominoes = new JFrame("Javarominoes");
    javarominoes.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    gameController = new GameController();
    
    // refreshrate: 75hz, layer size geometric series common ratio: 0.5
    parallaxPanel = new ParallaxScrollPanel(75, 0.5f);
    mainMenuPanel = new MainMenuPanel();
    
    // default to custom synthesizer, starting at 0.5 volume. let app handle playback
    mainMenuPanel.setMusicHandler(new KorobeinikiMusicHandler(0.5));
    
    menuContainer = new JLayeredPane() {
      @Override
      public void doLayout() {
        super.doLayout();
        
        mainMenuPanel.setBounds(0, 0, getWidth(), getHeight());
        parallaxPanel.setBounds(0, 0, getWidth(), getHeight());
        
        mainMenuPanel.repaint();
        parallaxPanel.repaint();
      }
    };
    
    menuContainer.add(parallaxPanel, JLayeredPane.DEFAULT_LAYER);
    menuContainer.add(mainMenuPanel, JLayeredPane.PALETTE_LAYER);
    // now it can be added and removed when needed

    mainMenuPanel.getPlayButton().addActionListener(Javarominoes.this);
    mainMenuPanel.getExitToDesktopButton().addActionListener(Javarominoes.this);
    
    gameController.getPauseMenuPanel()
            .getMainMenuButton()
            .addActionListener(Javarominoes.this);

    javarominoes.setSize(INITIAL_X, INITIAL_Y);
    doFrameAddMenu();
  }

  public void startApp() {
    javarominoes.setVisible(true);
    MainMenuPanel.musicHandler.startMusic();
    doFrameAddMenu();
  }
  
  private void doFramePlayGame() {
    
    gameController.getPauseMenuPanel()
              .setMusicHandler(MainMenuPanel.musicHandler);
    javarominoes.add(gameController);
    
    javarominoes.revalidate();
    javarominoes.repaint();
    
    gameController.gameStartLifespan();
    gameController.requestFocusInWindow();
  }

  private void doFrameRemoveGame() {
    gameController.reinitializeGame();
    javarominoes.remove(gameController);
    javarominoes.revalidate();
    javarominoes.repaint();
  }
  
  private void doFrameAddMenu() {
    mainMenuPanel.setMusicHandler(PauseMenuPanel.musicHandler);
    MainMenuPanel.musicHandler.setSpeed(MusicHandler.BASE_SPEED);
    
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
    if (gameController != null) {
      if (e.getSource() == gameController.getPauseMenuPanel().getMainMenuButton()) {
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
      MainMenuPanel.musicHandler.stopMusic();
      javarominoes.dispose();
    }
  }

  public static void main(String[] args) {
    Javarominoes g = new Javarominoes(); // build before using
    g.startApp();
  }
}
