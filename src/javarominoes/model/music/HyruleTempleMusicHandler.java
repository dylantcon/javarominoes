/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.music;

import javarominoes.model.music.song.HyruleTempleSong;

/**
 *
 * @author dylan
 */
public class HyruleTempleMusicHandler extends ChiptuneMusicHandler {

  public HyruleTempleMusicHandler() {
    super(new HyruleTempleSong());
  }
  
  public HyruleTempleMusicHandler(boolean pb, double vol, double speed) {
    this();
    
    setVolume(vol);
    setSpeed(speed);
    if (pb)
      startMusic();
  }

  public HyruleTempleMusicHandler(double d) {
    this();
    setVolume(d);
  }
  
  @Override
  public String getMusicType() {
    return "Hyrule Temple - SSB Melee";
  }
  
}
