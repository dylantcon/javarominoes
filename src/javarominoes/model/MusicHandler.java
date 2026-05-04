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
  
  public void startMusic();
  public void stopMusic();
  public void restartMusic();
  public void setVolume(double volume);
  public double getVolume();
  public String getMusicType();
  public boolean doingPlayback();
  
}