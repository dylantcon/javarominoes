/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model;

import javarominoes.model.synth.ChiptuneSong;
import javarominoes.model.synth.ChiptuneSynth;

/**
 *
 * @author dylan
 */
public abstract class ChiptuneMusicHandler implements MusicHandler {
  
  protected final ChiptuneSynth synth;
  
  protected ChiptuneMusicHandler(ChiptuneSong song) {
    synth = ChiptuneSynth.getSynthesizer(song);
  }
  
  @Override
  public final void startMusic() {
    synth.start();
  }

  @Override
  public void stopMusic() {
    synth.stop();
  }

  @Override
  public void restartMusic() {
    synth.setSpeed(BASE_SPEED);
    synth.rewind();
  }
  
  @Override
  public boolean doingPlayback() {
    return synth.isRunning();
  }

  @Override
  public final void setVolume(double volume) {
    synth.setVolume(volume);
  }

  @Override
  public void setSpeed(double speed) {
    synth.setSpeed(speed);
  }
  
  @Override
  public double getSpeed() {
    return synth.getSpeed();
  }
}
