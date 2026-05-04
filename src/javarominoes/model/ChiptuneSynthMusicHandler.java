/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model;

/**
 *
 * @author dylan
 */
public class ChiptuneSynthMusicHandler implements MusicHandler {
  
  private final ChiptuneSynth synth = ChiptuneSynth.getSynthesizer();
  
  @Override
  public void startMusic() {
    synth.start();
  }

  @Override
  public void stopMusic() {
    synth.stop();
  }

  @Override
  public void restartMusic() {
    synth.stop();
    synth.start();
  }
  
  @Override
  public String getMusicType() {
    return "Retro";
  }
  
  @Override
  public boolean doingPlayback() {
    return synth.isRunning();
  }

  @Override
  public void setVolume(double volume) {
    synth.setVolume(volume);
  }

  @Override
  public double getVolume() {
    return synth.getVolume();
  }
}