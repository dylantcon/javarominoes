package javarominoes;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author dylan
 */
public class ParallaxScrollPanel extends JPanel implements ActionListener, ComponentListener {

  /* CONSTANTS */
  
  private final static int DEFAULT_BACKING_LAYER_NUM = 4;
  private final static int DEFAULT_HZ = 60;
  private final static int SEC_TO_MSECS = 1000;
  
  private final static float DEFAULT_COMMON_RATIO = 0.80f;
  private final static float BASE_BLOCK_SIZE_INCHES = 0.5f;
  
  private final static float SCROLL_SPD_BLOCK_PER_SEC = 2.2f;
  
  /* MEMBER DATA */
  
  private Timer redrawTimer;
  
  private final int fgBlockSzPx;
  
  private ArrayList<Integer> fgBlockPositions;
  
  /* CONSTRUCTORS */
  
 /**
  * Default constructor for ParallaxScrollPanel
  * @param refreshHz The refresh rate of the parallax scroll in Hertz.
  * @param commonRatio A float indicating the intended ratio of block size for
  * each adjacent parallax layer. For 0.8f and a base = 50 px, backing1 = 40 px
  */
  public ParallaxScrollPanel(int refreshHz, float commonRatio) {
    
    setBackground(Color.BLACK);
    addComponentListener(ParallaxScrollPanel.this);
    
    initializeRedrawTimer(refreshHz);
    fgBlockSzPx = getBaseBlockSize();
    
    System.out.println("Calculated base block size of " + fgBlockSzPx + " pixels.");
    
    initializeForegroundPositions(640);
    setVisible(true);
    redrawTimer.start();
  }
  
  /* INITIALIZATION HELPERS */
  
  private void initializeRedrawTimer(int refreshHz) {
    if (refreshHz <= 0)
      refreshHz = DEFAULT_HZ;
    
    int rerenderPeriod = SEC_TO_MSECS / refreshHz;
    System.out.println("Rerender period = " + Integer.toString(rerenderPeriod));
    
    if (redrawTimer != null && redrawTimer.isRunning()) {
      redrawTimer.stop();
      redrawTimer.setDelay(rerenderPeriod);
    }
    else
      redrawTimer = new Timer(rerenderPeriod, ParallaxScrollPanel.this);
  }
  
  private int getBaseBlockSize() {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    
    return (int)(toolkit.getScreenResolution() * BASE_BLOCK_SIZE_INCHES);
  }
  
  private void initializeForegroundPositions(int windowWidth) {
    int listSize = windowWidth / fgBlockSzPx + 1; // one more for scrolling
    int offset = fgBlockSzPx / 2;
    fgBlockPositions = new ArrayList<>(listSize);
    
    if (listSize <= 0)
      return;
    
    // set up all blocks. scrolls R to L
    for (int blockNo = 0; blockNo < listSize; blockNo++) {
      fgBlockPositions.add(blockNo, (blockNo * fgBlockSzPx) - offset);
    }
  }
  
  /* RENDERING HELPERS */
  
  public void paintForeground(Graphics g) {
    
    if (fgBlockPositions == null)
      return;
    
    Color previousColor = g.getColor();
    g.setColor(Color.WHITE);
    
    int yFgTop = getHeight() - fgBlockSzPx;
    
    for (int i = 0; i < fgBlockPositions.size(); i++)
      g.fill3DRect(fgBlockPositions.get(i), yFgTop, fgBlockSzPx, fgBlockSzPx, true);
    
    g.setColor(previousColor);
  }
  
  /* TRANSFORMATION HELPERS */
  
  // this should only be called in actionPerformed or a delegate of actionPerformed
  private void updateForegroundPositions() {
    if (fgBlockPositions == null)
        return;
      
      float blocksMoved = getPeriod() * (SCROLL_SPD_BLOCK_PER_SEC / SEC_TO_MSECS);
      int pixelsMoved = (int)(blocksMoved * fgBlockSzPx);
      
      for (int i = 0; i < fgBlockPositions.size(); i++) {
        fgBlockPositions.set(i, fgBlockPositions.get(i) - pixelsMoved);
      }
      
      /* if first foreground block scrolled fully out of view, remove it, append
          new position exactly one block size higher than the old last index val */
      if (fgBlockPositions.getFirst() <= -fgBlockSzPx) {
        fgBlockPositions.removeFirst();
        fgBlockPositions.add(fgBlockPositions.getLast() + fgBlockSzPx);
      }
  }
  
  /* GETTERS */
  
  public Timer getRedrawTimer() {
    return redrawTimer;
  }
  
  public int getPeriod() {
    if (redrawTimer == null)
      return 0;
    return redrawTimer.getDelay();
  }
  /* OVERRIDES */
  
  @Override
  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == redrawTimer) {
      // update foreground
      updateForegroundPositions();
      // update background
      // updateBackgroundPositions();
      repaint();
    }
  }
  
  @Override
  public void paintComponent(Graphics g) {
    
    super.paintComponent(g);
    
    paintForeground(g);
  }

  @Override
  public void componentResized(ComponentEvent e) {
    
    if (fgBlockPositions == null) {
      initializeForegroundPositions(getWidth());
      return;
    }
    
    int fgMaxWidth = fgBlockPositions.size() * fgBlockSzPx - (fgBlockSzPx / 2);
    int fgNeededWidth = getWidth() + fgBlockSzPx + 10; // +8 px buffer
    int fgDelta = fgNeededWidth - fgMaxWidth;
    int blockDelta = fgDelta / fgBlockSzPx;
    
    if (blockDelta > 0) { // fg delta will be positive
      for (int i = 0; i < blockDelta; i++) {
        fgBlockPositions.add(fgBlockPositions.getLast() + (fgBlockSzPx));
      }
    }
    else if (blockDelta < 0) {
      for (int i = 0; i < -blockDelta; i++) {
        fgBlockPositions.removeLast();
      }
    }
    else {
      
    }
  }

  @Override
  public void componentMoved(ComponentEvent e) {}

  @Override
  public void componentShown(ComponentEvent e) {
  }

  @Override
  public void componentHidden(ComponentEvent e) {
  }
  
}
