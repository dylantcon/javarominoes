/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.music;

import javarominoes.model.music.song.WilySong;

/**
 *
 * @author dylan
 */
public class WilyMusicHandler extends ChiptuneMusicHandler{

  public WilyMusicHandler() {
    super(new WilySong());
  }

  public WilyMusicHandler(boolean pb, double vol, double speed) {
    this();
    
    setVolume(vol);
    setSpeed(speed);
    if (pb)
      startMusic();
  }
  
  public WilyMusicHandler(double vol) {
    this();
    setVolume(vol);
  }
  
  @Override
  public String getMusicType() {
    return "Dr. Wily, Stage 1 - MM2";
  }
  
}
