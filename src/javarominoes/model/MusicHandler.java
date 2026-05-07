/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package javarominoes.model;

/**
 * Implemented by MidiMusicHandler and ChiptuneSynthMusicHandler
 * @author dylan
 */
public interface MusicHandler {
  
  public final static double BASE_SPEED = 1.0;
  
  public void startMusic();
  public void stopMusic();
  public void restartMusic();
  public void setVolume(double volume);
  public void setSpeed(double speed);
  public double getSpeed();
  public abstract String getMusicType();
  public boolean doingPlayback();
}