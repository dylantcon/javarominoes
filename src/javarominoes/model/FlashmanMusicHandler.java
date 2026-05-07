/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model;
import javarominoes.model.synth.FlashmanSong;

/**
 *
 * @author dylan
 */
public class FlashmanMusicHandler extends ChiptuneMusicHandler {
  public FlashmanMusicHandler() {
    super(new FlashmanSong());
  }
  
  public FlashmanMusicHandler(boolean pb, double vol, double speed) {
    this();
    
    setVolume(vol);
    setSpeed(speed);
    if (pb)
      startMusic();
  }
  
  @Override
  public String getMusicType() {
    return "Flash Man, MM2";
  }
}
