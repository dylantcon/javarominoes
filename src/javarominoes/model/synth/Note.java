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
    public final double decay;          // NEW: per-note envelope decay rate
    
    public Note(int midi, int duration, double volume, double duty) {
        this(midi, duration, volume, duty, DEFAULT_DECAY);
    }
    
    public Note(int midi, int duration, double volume, double duty, double decay) {
        this.midi = midi;
        this.durationFrames = duration;
        this.volume = volume;
        this.duty = duty;
        this.decay = decay;
    }
    
    public static Note rest(int frames) {
        return new Note(-1, frames, 0, 0.5, 0);
    }
    
    private static final double DEFAULT_DECAY = 0.6;
}
