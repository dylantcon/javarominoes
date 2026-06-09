/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author dylan
 */
public class VolumeSliderPanel extends JPanel implements ChangeListener {
  
  private final JSlider volSld;
  private final ResizableLabel volLabel;
  private int volumePercent;

  public VolumeSliderPanel(int min, int max, int initial) {
    this.setLayout(new BorderLayout());
    // init the slider
    volSld = new JSlider(JSlider.HORIZONTAL, min, max, initial);
    volSld.setMajorTickSpacing(5);
    volSld.setMinorTickSpacing(1);
    volSld.setPaintTicks(true);
    volSld.setForeground(Color.DARK_GRAY.darker());
    volSld.setPaintLabels(true);
    volSld.setLabelTable(this.createLabelTable(max, min));
    volSld.setBackground(Color.CYAN.darker());
    volumePercent = initial;
    volLabel = new ResizableLabel(buildSliderLabel(volumePercent), JLabel.CENTER, this);
    Border lB = BorderFactory.createLineBorder(Color.CYAN.brighter(), 1);
    Border pB = BorderFactory.createEmptyBorder(3, 3, 3, 3);
    this.initSliderChangeListener();
    this.setBorder(BorderFactory.createCompoundBorder(lB, pB));
    this.setBackground(Color.CYAN);
    this.add(volSld, BorderLayout.NORTH);
    this.add(volLabel, BorderLayout.SOUTH);
    this.setOpaque(true);
    this.setDoubleBuffered(true);
  }
  
  private static String buildSliderLabel(int volPer) {
    return "Music Volume: " + volPer + "%";
  }

  private Hashtable<Integer, ResizableLabel> createLabelTable(int max, int min) {
    Hashtable<Integer, ResizableLabel> lTab;
    lTab = new Hashtable<>();
    lTab.put(min, new ResizableLabel(Integer.toString(min), JLabel.CENTER, this));
    lTab.put((min + max) / 2, new ResizableLabel(Integer.toString((min + max) / 2), JLabel.CENTER, this));
    lTab.put(max, new ResizableLabel(Integer.toString(max), JLabel.CENTER, this));
    return lTab;
  }

  private void initSliderChangeListener() {
    volSld.addChangeListener(this);
  }

  // changelistener callback
  @Override
  public void stateChanged(ChangeEvent e) {
    volumePercent = volSld.getValue();
    volLabel.setText(buildSliderLabel(volumePercent));
  }

  public int getVolumePercent() {
    return this.volumePercent;
  }

  public double getVolumeDouble() {
    return this.volumePercent / 100.0;
  }

  public JSlider getSlider() {
    return this.volSld;
  }

  public int getSliderHeight() {
    return this.getHeight();
  }

  public int getSliderWidth() {
    return this.getWidth();
  }

  class ResizableLabel extends JLabel {

    VolumeSliderPanel vsp;
    private int cachedFontSz = -1;

    public ResizableLabel(String txt, int align, VolumeSliderPanel vsp) {
      super(txt, align);
      this.vsp = vsp;
      // listen for slider-panel resizes instead of recomputing in paint
      vsp.addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(java.awt.event.ComponentEvent e) {
          refreshFontIfNeeded();
        }
      });
    }

    private void refreshFontIfNeeded() {
      int panW = vsp.getSliderWidth();
      int panH = vsp.getSliderHeight();
      int fontSz = Math.min(panW / 9, panH / 6);
      if (fontSz <= 0 || fontSz == cachedFontSz) {
        return;
      }
      cachedFontSz = fontSz;
      setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSz));
    }

    @Override
    protected void paintComponent(Graphics g) {
      if (cachedFontSz < 0) {
        refreshFontIfNeeded(); // first-paint bootstrap
      }
      super.paintComponent(g);
    }
  }
  
}
