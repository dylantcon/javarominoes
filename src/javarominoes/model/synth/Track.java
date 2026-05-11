/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.synth;

import java.util.ArrayList;
import java.util.List;

// a monophonic sequence of notes for one channel
public class Track {

  public final List<Note> notes = new ArrayList<>();
  int cursor = 0;
  int framesLeft = 0;

  private double defaultVolume = 0.7;
  private double defaultDuty = 0.25;
  private double currentDecay = 0.6;     // NEW: state for subsequent addNotes

  public Track withDefaults(double vol, double duty) {
    this.defaultVolume = vol;
    this.defaultDuty = duty;
    return this;
  }

  public Track withDecay(double decay) {  // NEW
    this.currentDecay = decay;
    return this;
  }

  public Track add(Note n) {
    notes.add(n);
    return this;
  }

  /**
   * add notes as alternating (midi, duration) pairs. Use -1 for midi to encode
   * a rest.
   *
   * @author dylan
   * @param pitchesAndDurations alternating pitch, duration pairs
   * @return a list of Note objects in the form of a Track
   */
  public Track addNotes(int... pitchesAndDurations) {
    if (pitchesAndDurations.length % 2 != 0) {
      throw new IllegalArgumentException(
      "addNotes requires alternating pitch, duration pairs");
    }
    for (int i = 0; i < pitchesAndDurations.length; i += 2) {
      int midi = pitchesAndDurations[i];
      int dur = pitchesAndDurations[i + 1];
      notes.add(new Note(midi, dur, defaultVolume, defaultDuty, currentDecay));
    }
    return this;
  }

  void reset() {
    cursor = 0;
    framesLeft = 0;
  }

}
