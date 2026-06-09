/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.music;

import javarominoes.model.music.song.DuckTalesMoonSong;

/**
 *
 * @author dylan
 */
public class DuckTalesMoonMusicHandler extends ChiptuneMusicHandler {

  public DuckTalesMoonMusicHandler() {
    super(new DuckTalesMoonSong());
  }

  public DuckTalesMoonMusicHandler(boolean pb, double vol, double speed) {
    this();

    setVolume(vol);
    setSpeed(speed);
    if (pb)
      startMusic();
  }

  public DuckTalesMoonMusicHandler(double d) {
    this();
    setVolume(d);
  }

  @Override
  public String getMusicType() {
    return "The Moon - DuckTales";
  }

}
