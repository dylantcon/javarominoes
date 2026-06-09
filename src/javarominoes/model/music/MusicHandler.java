/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package javarominoes.model.music;

/**
 * Implemented by ChiptuneMusicHandler and its per-song subclasses
 *
 * @author dylan
 */
public interface MusicHandler {

  public final static double BASE_SPEED = 1.0;
  public final static double GAME_OVER_SPEED = 0.25;
  
  public static final Class<?>[] concretized = {
    KorobeinikiMusicHandler.class,
    FlashmanMusicHandler.class,
    WilyMusicHandler.class,
    BloodyTearsMusicHandler.class,
    ContraJungleMusicHandler.class,
    SurfCityMusicHandler.class,
    HyruleTempleMusicHandler.class,
    DuckTalesMoonMusicHandler.class
  };

  public void startMusic();

  public void stopMusic();

  public void restartMusic();

  public void setVolume(double volume);

  public void setSpeed(double speed);

  public double getSpeed();

  public abstract String getMusicType();

  public boolean doingPlayback();
}
