/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Hashtable;
import javarominoes.model.ChiptuneSynthMusicHandler;
import javarominoes.model.MidiMusicHandler;
import javarominoes.model.MusicHandler;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author dylan
 *
 * All menus provide access to the music controls (mute/unmute, volume)
 */
public abstract class MenuPanel extends JPanel implements ActionListener {

  protected static final String FONTNAME = "Monospaced";
  protected static final float BASE_PT = 10f;
  protected static final float LABEL_FONT_PT = 48f;
  protected static final float BUTTON_FONT_PT = 13f;
  protected static final int BUTTON_WIDTH_PX = 200;
  protected static final int BUTTON_HEIGHT_PX = 40;

  private static final int SOUND_BUTTON_W_PX = 150;
  private static final int SOUND_BUTTON_H_PX = 30;
  private static final float SOUND_BUTTON_FONT_PT = 10f;

  protected static final Font FONTBASE = new Font(FONTNAME, 0, (int) BASE_PT);
  protected static final Insets HEADER_P = new Insets(20, 0, 20, 0);
  protected static final Insets STD_P = new Insets(10, 20, 10, 20);

  private static final String TYPE_TEXT = "Music Type";
  private static final String STOP_TEXT = "Stop";
  private static final String START_TEXT = "Start";

  public static MusicHandler musicHandler;

  protected final JButton swapMusicButton;
  protected final JButton toggleMusicButton;
  protected final VolumeSliderPanel volumeSliderPanel;

  protected MenuPanel() {
    setLayout(new GridBagLayout());

    MenuPanel.musicHandler = null;

    swapMusicButton = buildSoundButton(buildMusicTypeLabel(), 
            Color.WHITE, Color.BLACK);
    toggleMusicButton = buildSoundButton(buildStartStopLabel(), 
            Color.RED, Color.YELLOW);
    
    volumeSliderPanel = new VolumeSliderPanel(0, 100, 100);
    
    volumeSliderPanel.getSlider().addChangeListener((ChangeEvent e) -> {
      double volumeSlider = volumeSliderPanel.getSlider().getValue() / 100.0;
      musicHandler.setVolume(volumeSlider);
    });
    
    swapMusicButton.addActionListener(MenuPanel.this);
    toggleMusicButton.addActionListener(MenuPanel.this);
  }
  
  public void setMusicHandler(MusicHandler music) {
    musicHandler = music;
    refreshMusicTypeText();
    refreshStartStopText();
  }

  private JButton buildButton(String t, Color f, Color b, int w, int h, float ftPt) {
    Dimension buttonSize = new Dimension(w, h);

    JButton menuButton = new JButton(t);
    menuButton.setPreferredSize(buttonSize);
    menuButton.setFont(FONTBASE.deriveFont(Font.ITALIC, ftPt));
    menuButton.setForeground(f);
    menuButton.setBackground(b);
    menuButton.setFocusable(false);

    return menuButton;
  }
  
  protected final JButton buildMenuButton(String label, Color fg, Color bg) {
    return buildButton(label, fg, bg, 
            BUTTON_WIDTH_PX, BUTTON_HEIGHT_PX, BUTTON_FONT_PT);
  }

  private JButton buildSoundButton(String label, Color fg, Color bg) {
    JButton btn = buildButton(label, fg, bg,
            SOUND_BUTTON_W_PX, SOUND_BUTTON_H_PX, SOUND_BUTTON_FONT_PT);
    // BoxLayout.Y_AXIS clamps each child to its maxWidth; JButton's UI
    // delegate returns preferredSize for max, which would pin width to
    // SOUND_BUTTON_W_PX. Unbound the width so the button stretches to
    // the audioPanel's width (driven by the slider's preferred width).
    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, SOUND_BUTTON_H_PX));
    btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    return btn;
  }

  protected JPanel constructAudioPanel() {
    JPanel audioPanel = new JPanel();
    audioPanel.setBackground(Color.CYAN);
    audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));
    audioPanel.setBorder(BorderFactory.createTitledBorder("Audio"));
    audioPanel.add(toggleMusicButton);
    audioPanel.add(swapMusicButton);
    audioPanel.add(volumeSliderPanel);
    audioPanel.setOpaque(true);
    audioPanel.setDoubleBuffered(true);
    return audioPanel;
  }

  protected abstract void initGbl();

  protected final void gblAdd(Component c, int gX, int gY, double wX, double wY, int a, Insets i) {
    // set up constraints instance
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridx = gX; // populate gridX
    gbc.gridy = gY; // populate gridY

    gbc.weightx = wX; // populate weightX
    gbc.weighty = wY; // populate weightY

    gbc.anchor = a; // populate anchor with supplied static const member
    gbc.insets = i; // populate inset with supplied Inset instance

    add(c, gbc);
  }

  protected void hideButtons(JButton[] excludes) {
    setButtonVisibility(false, excludes);
  }

  protected void showButtons(JButton[] excludes) {
    setButtonVisibility(true, excludes);
  }

  protected void setButtonVisibility(boolean visible, Component[] excludes) {
    boolean mustSkip;
    for (Component current : getComponents()) {
      mustSkip = false;

      // if any components are requested for exclusion, skip them
      for (Component excluded : excludes) {
        if (excluded.equals(current)) {
          mustSkip = true;
          break;
        }
      }

      if (!mustSkip && current.getClass().equals(JButton.class)) {
        current.setVisible(visible);
      }
    }
  }

  private String buildMusicTypeLabel() {
    if (musicHandler == null)
      return "...";
    return TYPE_TEXT + ": " + musicHandler.getMusicType();
  }

  private void refreshMusicTypeText() {
    swapMusicButton.setText(buildMusicTypeLabel());
    swapMusicButton.repaint();
  }

  private String buildStartStopLabel() {
    if (musicHandler == null)
      return "...";
    return musicHandler.doingPlayback() ? STOP_TEXT : START_TEXT;
  }

  private void refreshStartStopText() {
    toggleMusicButton.setText(buildStartStopLabel());
    toggleMusicButton.repaint();
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    double currVol = musicHandler == null ? 1.0 : musicHandler.getVolume();
    if (evt.getSource() == swapMusicButton) {
      musicHandler.stopMusic();
      if (musicHandler.getMusicType().equals("Midi")) {
        musicHandler = new ChiptuneSynthMusicHandler();
      } else {
        try {
        musicHandler = new MidiMusicHandler();
        } catch (MidiUnavailableException e) {
          System.err.println("Could not find korobeiniki.mid in resources.");
          musicHandler = new ChiptuneSynthMusicHandler();
        }
      }
      musicHandler.setVolume(currVol);
      musicHandler.startMusic();
      refreshMusicTypeText();
    }
    if (evt.getSource() == toggleMusicButton) {
      if (musicHandler.doingPlayback()) {
        musicHandler.stopMusic();
        refreshStartStopText();
      }
    }
  }

  protected class VolumeSliderPanel extends JPanel implements ChangeListener {

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
      volSld.setPaintLabels(true);
      volSld.setLabelTable(this.createLabelTable(max, min));

      volumePercent = initial;
      volLabel = new ResizableLabel(volumePercent + "%",
              JLabel.CENTER, this);
      Border lB = BorderFactory.createLineBorder(Color.GRAY, 1);
      Border pB = BorderFactory.createEmptyBorder(3, 3, 3, 3);

      this.initSliderChangeListener();
      this.setBorder(BorderFactory.createCompoundBorder(lB, pB));
      this.setBackground(new Color(214 / 255.0f, 214 / 255.0f, 214 / 255.0f));
      this.add(volSld, BorderLayout.NORTH);
      this.add(volLabel, BorderLayout.SOUTH);
      
      this.setOpaque(true);
      this.setDoubleBuffered(true);
    }

    private Hashtable<Integer, ResizableLabel> createLabelTable(int max, int min) {
      Hashtable<Integer, ResizableLabel> lTab;
      lTab = new Hashtable<>();

      lTab.put(min, new ResizableLabel(Integer.toString(min / 10), JLabel.CENTER, this));
      lTab.put((min + max) / 2,
              new ResizableLabel(Integer.toString(max / 20),
                      JLabel.CENTER, this));
      lTab.put(max, new ResizableLabel(Integer.toString(max / 10), JLabel.CENTER, this));

      return lTab;
    }

    private void initSliderChangeListener() {
      volSld.addChangeListener(this);
    }

    // changelistener callback
    @Override
    public void stateChanged(ChangeEvent e) {
      volumePercent = volSld.getValue();
      volLabel.setText(volumePercent + "%");
    }

    public int getVolumePercent() {
      return this.volumePercent;
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
        if (fontSz <= 0 || fontSz == cachedFontSz)
          return;
        cachedFontSz = fontSz;
        setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSz));
      }

      @Override
      protected void paintComponent(Graphics g) {
        if (cachedFontSz < 0)
          refreshFontIfNeeded(); // first-paint bootstrap
        super.paintComponent(g);
      }
    }
  }
}
