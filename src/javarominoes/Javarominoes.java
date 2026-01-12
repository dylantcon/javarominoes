/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
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
  private Sequencer sequencer;
  
  public final static int INITIAL_X = 1280;
  public final static int INITIAL_Y = 720;

  public Javarominoes() {
    javarominoes = new JFrame("Javarominoes");
    javarominoes.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    gamePanel = new GamePanel(this);
    // refreshrate: 75hz, layer size geometric series common ratio: 0.8
    parallaxPanel = new ParallaxScrollPanel(75, 0.8f);
    mainMenuPanel = new MainMenuPanel();
    
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
    
    gamePanel.getPauseMenuPanel()
            .getMainMenuButton()
            .addActionListener(Javarominoes.this);

    javarominoes.setSize(INITIAL_X, INITIAL_Y);
    doFrameAddMenu();
  }

  public void startApp() {
    javarominoes.setVisible(true);
    doFrameAddMenu();
    startMusic();
  }
  
  public void restartMusic() {
    stopMusic();
    startMusic();
  }

  private void startMusic() {
    try {
      sequencer = MidiSystem.getSequencer();
      sequencer.open();

      // load MIDI from classpath (put korobeiniki.mid in the javarominoes package folder)
      InputStream midiStream = getClass().getResourceAsStream("korobeiniki.mid");
      if (midiStream != null) {
        sequencer.setSequence(MidiSystem.getSequence(midiStream));
        sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        sequencer.start();
      } else {
        System.err.println("Could not find korobeiniki.mid in resources");
      }
    } catch (IOException | InvalidMidiDataException | MidiUnavailableException e) {
      System.out.println(Arrays.toString(e.getStackTrace()));
    }
  }

  private void stopMusic() {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.stop();
      sequencer.close();
    }
  }
  
  private void doFramePlayGame() {
    if (gamePanel == null)
      gamePanel = new GamePanel(this);
    
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
      restartMusic();
      doFramePlayGame();
      return;
    }
    if (e.getSource() == mainMenuPanel.getExitToDesktopButton()) {
      stopMusic();
      javarominoes.dispose();
    }
  }

  public static void main(String[] args) {
    Javarominoes g = new Javarominoes(); // build before using
    g.startApp();
  }
}
