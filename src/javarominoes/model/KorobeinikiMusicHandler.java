/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model;

import javarominoes.model.synth.KorobeinikiSong;

/**
 *
 * @author dylan
 */
public class KorobeinikiMusicHandler extends ChiptuneMusicHandler {
  public KorobeinikiMusicHandler() {
    super(new KorobeinikiSong());
  }
  
  public KorobeinikiMusicHandler(boolean pb, double vol, double speed) {
    this();
    
    setVolume(vol);
    setSpeed(speed);
    if (pb)
      startMusic();
  }
  
  public KorobeinikiMusicHandler(double vol) {
    this();
    setVolume(vol);
  }
  
  @Override
  public String getMusicType() {
    return "Korobeiniki, Retro";
  }
}