/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.music;

import javarominoes.model.music.song.ChiptuneSong;
import javarominoes.model.music.song.ContraJungleSong;

/**
 *
 * @author dylan
 */
public class ContraJungleMusicHandler extends ChiptuneMusicHandler {

  public ContraJungleMusicHandler() {
    super(new ContraJungleSong());
  }

  public ContraJungleMusicHandler(boolean pb, double vol, double speed) {
    this();

    setVolume(vol);
    setSpeed(speed);
    if (pb) {
      startMusic();
    }
  }

  @Override
  public String getMusicType() {
    return "Jungle - Contra";
  }

}
