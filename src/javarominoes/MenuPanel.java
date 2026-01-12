/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author dylan
 */
public abstract class MenuPanel extends JPanel {

  protected static final String FONTNAME = "Monospaced";
  protected static final float BASE_PT = 10f;
  protected static final float LABEL_FONT_PT = 48f;
  protected static final float BUTTON_FONT_PT = 13f;
  protected static final int BUTTON_WIDTH_PX = 200;
  protected static final int BUTTON_HEIGHT_PX = 40;
  protected static final Font FONTBASE = new Font(FONTNAME, 0, (int) BASE_PT);

  protected MenuPanel() {
    setLayout(new GridBagLayout());
  }

  protected JButton buildButton(String label, Color fg, Color bg) {
    Dimension buttonSize = new Dimension(BUTTON_WIDTH_PX, BUTTON_HEIGHT_PX);

    JButton menuButton = new JButton(label);
    menuButton.setPreferredSize(buttonSize);
    menuButton.setFont(FONTBASE.deriveFont(Font.ITALIC, BUTTON_FONT_PT));
    menuButton.setForeground(fg);
    menuButton.setBackground(bg);
    menuButton.setFocusable(false);

    return menuButton;
  }

  protected abstract void initGbl();

  protected void gblAdd(Component c, int gX, int gY, double wX, double wY, int a, Insets i) {
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

}
