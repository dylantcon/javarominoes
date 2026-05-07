/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.synth;

/**
 * a note event. use midi=-1 for a rest. duration is in frames (60th of a
 * second), same as NES. so, at 60fps, a 24 frame note is 0.4 seconds, which
 * is a quarter note at 150 BPM.
 *
 * @author dylan
 */
public class Note {

  public final int midi;
  public final int durationFrames;
  public final double volume;
  public final double duty;

  public Note(int midi, int durationFrames, double volume, double duty) {
    this.midi = midi;
    this.durationFrames = durationFrames;
    this.volume = volume;
    this.duty = duty;
  }

  public static Note rest(int frames) {
    return new Note(-1, frames, 0, 0.5);
  }
  
}
