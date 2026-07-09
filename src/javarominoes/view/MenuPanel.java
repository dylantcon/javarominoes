/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import chiptunesynth.music.MusicHandler;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

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
  protected static final float BUTTON_FONT_PT = 18f;
  protected static final int BUTTON_WIDTH_PX = 310;
  protected static final int BUTTON_HEIGHT_PX = 60;

  private static final int SOUND_BUTTON_W_PX = 250;
  private static final int SOUND_BUTTON_H_PX = 30;
  private static final float SOUND_BUTTON_FONT_PT = 8f;
  
  private static final Color MUSIC_MINOR_CL = new Color(150, 10, 10);
  private static final Color MUSIC_MAJOR_CL = Color.GREEN.darker();

  protected static final Font FONTBASE = new Font(FONTNAME, 0, (int) BASE_PT);
  protected static final Insets HEADER_P = new Insets(20, 0, 20, 0);
  protected static final Insets STD_P = new Insets(10, 20, 10, 20);

  private static final String TYPE_TEXT = "Music";
  private static final String STOP_TEXT = "Pause";
  private static final String START_TEXT = "Play";

  public static MusicHandler musicHandler;

  private static int hdlIdx = 0;

  protected final JButton swapMusicButton;
  protected final JButton pausePlayMusicButton;
  protected final VolumeSliderPanel volumeSlider;

  protected MenuPanel() {
    setLayout(new GridBagLayout());

    MenuPanel.musicHandler = null;

    swapMusicButton = buildSoundButton(buildMusicTypeLabel(),
    Color.WHITE, Color.BLACK);
    pausePlayMusicButton = buildSoundButton(buildStartStopLabel(),
    MUSIC_MINOR_CL, MUSIC_MAJOR_CL);

    volumeSlider = new VolumeSliderPanel(0, 100, 50);

    volumeSlider.getSlider().addChangeListener((ChangeEvent e) -> {
      double volSldr = this.volumeSlider.getSlider().getValue() / 100.0;
      musicHandler.setVolume(volSldr);
    });

    swapMusicButton.addActionListener(MenuPanel.this);
    pausePlayMusicButton.addActionListener(MenuPanel.this);
  }

  public void setMusicHandler(MusicHandler music) {
    musicHandler = music;
    refreshMusicTypeText();
    refreshStartStopText();
    updateMusicStartStopColorScheme();
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
    btn.setFont(btn.getFont().deriveFont(Font.BOLD | Font.ITALIC));
    return btn;
  }

  protected JPanel constructAudioPanel() {
    JPanel audioPanel = new JPanel();
    audioPanel.setBackground(Color.CYAN);
    audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));
    audioPanel.setBorder(BorderFactory.createTitledBorder("Audio"));
    audioPanel.add(pausePlayMusicButton);
    audioPanel.add(swapMusicButton);
    audioPanel.add(volumeSlider);
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
    if (musicHandler == null) {
      return "...";
    }
    return TYPE_TEXT + ": " + musicHandler.getMusicType();
  }

  private void refreshMusicTypeText() {
    swapMusicButton.setText(buildMusicTypeLabel());
    swapMusicButton.repaint();
  }

  private String buildStartStopLabel() {
    if (musicHandler == null) {
      return "...";
    }
    return musicHandler.doingPlayback() ? STOP_TEXT : START_TEXT;
  }

  private void refreshStartStopText() {
    pausePlayMusicButton.setText(buildStartStopLabel());
    pausePlayMusicButton.repaint();
  }
  
  private void updateMusicStartStopColorScheme() {
    if (musicHandler != null && musicHandler.doingPlayback()) {
      pausePlayMusicButton.setBackground(MUSIC_MAJOR_CL);
      pausePlayMusicButton.setForeground(MUSIC_MINOR_CL);
    } else {
      pausePlayMusicButton.setBackground(MUSIC_MINOR_CL);
      pausePlayMusicButton.setForeground(MUSIC_MAJOR_CL);
    }
  }
  
  private void processMusicStartStopEvent() {
     if (musicHandler != null)
     {
       if (musicHandler.doingPlayback()) {
          musicHandler.stopMusic();
       } else {
         musicHandler.startMusic();
       }
     }
     refreshStartStopText();
     updateMusicStartStopColorScheme();
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    boolean doPlay = musicHandler != null && musicHandler.doingPlayback();
    double currVol = volumeSlider == null ? 0.5 : volumeSlider.getVolumeDouble();
    double currSpeed = musicHandler == null ? 1.0 : musicHandler.getSpeed();
    if (evt.getSource() == swapMusicButton) {
      musicHandler.stopMusic();
      hdlIdx = (hdlIdx + 1) % MusicHandler.concretized.length;
      try {
        Constructor<?> cnstrct = MusicHandler.concretized[hdlIdx].getConstructor(
        boolean.class, double.class, double.class);
        try {
          musicHandler = (MusicHandler) cnstrct.newInstance(doPlay, currVol, currSpeed);
        } catch (ReflectiveOperationException ex) {
          throw new IllegalStateException(ex);
        }
      } catch (NoSuchMethodException | SecurityException ex) {
        throw new IllegalStateException(ex);
      }
      refreshMusicTypeText();
    }
    if (evt.getSource() == pausePlayMusicButton) {
      processMusicStartStopEvent();
    }
  }
}