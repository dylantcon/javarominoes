/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model;

import java.util.ArrayList;
import javax.sound.sampled.*;
import java.util.List;

/**
 * A small nes-style chiptune synthesizer
 *
 * models the 2A03 APU at a high level: 
 * - two pulse (square) channels with selectable duty cycles 
 * - one triangle channel with a quantized waveform 
 * - one noise channel driven by a 15 bit linear feedback shift register
 *
 * a frame-rate sequencer drives all four channels at approximately 60 hz, the
 * same rate the actual NES ticked its music engine via the NMI handler. note
 * durations are measured in "frames" (60th of second), exactly the unit the
 * original Nintendo Entertainment System drivers used.
 *
 * Audio output is 44.1khz, 16-bit, mono, via javax.sound.sampled.
 *
 * @author dylan
 */
public class ChiptuneSynth implements Runnable {

  // audio constants
  public static final int SAMPLE_RATE = 44100;
  public static final int FRAME_RATE = 60;
  public static final int SAMPLES_PER_FRAME = SAMPLE_RATE / FRAME_RATE;

  // convert a midi note number to frequency in Hz. A4 = 69 = 440Hz.
  public static double midiToFreq(int midi) {
    return 444.0 * Math.pow(2.0, (midi - 69) / 12.0);
  }

  // channels
  /**
   * a pulse/square channel with variable duty cycle and a linear volume-decay
   * envelope
   *
   * the duty cycle controls timbre. 0.125 sounds nasal and thin (the Mario coin
   * sound), 0.25 is the classic NES lead, 0.5 is fuller and more clarinet-like
   *
   * @author dylan
   */
  static class PulseChannel {

    double phase = 0;
    double freq = 0;
    double duty = 0.5;
    double envelope = 0;
    double envelopeDecay = 0;

    // decayPerSec=0 holds the note flat; larger = faster fade
    void noteOn(double frequency, double volume, double dutyCycle, double decayPerSec) {
      this.freq = frequency;
      this.duty = dutyCycle;
      this.envelope = volume;
      this.envelopeDecay = decayPerSec * volume / SAMPLE_RATE;
    }

    void noteOff() {
      this.envelope = 0;
    }

    double sample() {
      if (envelope <= 0 || freq <= 0) {
        return 0;
      }
      phase += freq / SAMPLE_RATE;
      if (phase >= 1.0) {
        phase -= 1.0;
      }
      double s = (phase < duty ? 1.0 : -1.0) * envelope;
      envelope = Math.max(0, envelope - envelopeDecay);
      return s;
    }
  }

  /**
   * A triangle channel.
   *
   * real NES hardware steps through 32 levels; the staircase is part of the
   * distinctive character. approximate that by quantizing a continuous triangle
   * to 8 levels per half-cycle
   *
   * @author dylan
   */
  static class TriangleChannel {

    double phase = 0;
    double freq = 0;
    boolean active = false;

    void noteOn(double frequency) {
      this.freq = frequency;
      this.active = true;
    }

    void noteOff() {
      this.active = false;
    }

    double sample() {
      if (!active || freq <= 0) {
        return 0;
      }
      phase += freq / SAMPLE_RATE;
      if (phase >= 1.0) {
        phase -= 1.0;
      }
      // triangle in -1..+1 phase in 0..1
      double tri = phase < 0.5 ? phase * 4 - 1 : 3 - phase * 4;
      // quantize to 8 levels for NES stair-step character
      return Math.round(tri * 7) / 7.0;
    }
  }

  /**
   * Noise channel using a 15 bit linear feedback shift register, the same
   * design that the Nintendo Entertainment System uses.
   *
   * in "long" mode (def) the feedback bit is XOR of bits 0 and 1, giving
   * broadband noise. in "short" mode it's bits 0 and 6, which shortens the
   * period to 93 samples and produces a metallic pitched tone.
   *
   * @author dylan
   */
  static class NoiseChannel {

    int lfsr = 1;
    double phaseAcc = 0;
    double clockHz = 0;
    double envelope = 0;
    double envelopeDecay = 0;
    boolean shortMode = false;

    void noteOn(double clockRate, double volume, double decayPerSec, boolean shortMode) {
      this.clockHz = clockRate;
      this.envelope = volume;
      this.envelopeDecay = decayPerSec * volume / SAMPLE_RATE;
      this.shortMode = shortMode;
    }

    void noteOff() {
      this.envelope = 0;
    }

    double sample() {
      if (envelope <= 0 || clockHz <= 0) {
        return 0;
      }
      phaseAcc += clockHz / SAMPLE_RATE;
      while (phaseAcc >= 1.0) {
        phaseAcc -= 1.0;
        int bit0 = lfsr & 1;
        int otherBit = shortMode ? (lfsr >> 6) & 1 : (lfsr >> 1) & 1;
        int feedback = bit0 ^ otherBit;
        lfsr = (lfsr >> 1) | (feedback << 14);
      }
      double s = ((lfsr & 1) == 0 ? 1.0 : -1.0) * envelope;
      envelope = Math.max(0, envelope - envelopeDecay);
      return s;
    }
  }

  // song format
  
  /**
   * a note event. use midi=-1 for a rest. duration is in frames (60th of a
   * second), same as NES. so, at 60fps, a 24 frame note is 0.4 seconds, which
   * is a quarter note at 150 BPM.
   *
   * @author dylan
   */
  public static class Note {

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

  // a monophonic sequence of notes for one channel
  public static class Track {

    public final List<Note> notes = new ArrayList<>();
    int cursor = 0;
    int framesLeft = 0;

    public Track add(Note n) {
      notes.add(n);
      return this;
    }

    void reset() {
      cursor = 0;
      framesLeft = 0;
    }
  }

  // player state
  private final PulseChannel p1 = new PulseChannel();
  private final PulseChannel p2 = new PulseChannel();
  private final TriangleChannel tri = new TriangleChannel();
  private final NoiseChannel noi = new NoiseChannel();

  private Track p1Track, p2Track, triTrack, noiTrack;
  private boolean looping = true;
  private volatile boolean running = false;
  
  private volatile double targetVolume = 1.0;
  private double volume = 1.0;
  private static final double VOLUME_SLEW = 0.001;
  
  // per channel mixing weights
  private final double p1Mix = 0.25;
  private final double p2Mix = 0.25;
  private final double triMix = 0.30;
  private final double noiMix = 0.18;

  public ChiptuneSynth setSong(Track p1, Track p2, Track triangle, Track noise) {
    this.p1Track = p1;
    this.p2Track = p2;
    this.triTrack = triangle;
    this.noiTrack = noise;

    if (p1Track != null) {
      p1Track.reset();
    }
    if (p2Track != null) {
      p2Track.reset();
    }
    if (triTrack != null) {
      triTrack.reset();
    }
    if (noiTrack != null) {
      noiTrack.reset();
    }
    return this;
  }

  public ChiptuneSynth setLooping(boolean loop) {
    this.looping = loop;
    return this;
  }

  // advance one track by one frame, calling the handler when a new note begins
  private void stepTrack(Track t, NoteHandler handler) {
    if (t == null || t.notes.isEmpty()) {
      return;
    }
    if (t.framesLeft <= 0) {
      if (t.cursor >= t.notes.size()) {
        if (looping) {
          t.cursor = 0;
        } else {
          return;
        }
      }
      Note n = t.notes.get(t.cursor++);
      t.framesLeft = n.durationFrames;
      handler.handle(n);
    }
    t.framesLeft--;
  }

  private interface NoteHandler {

    void handle(Note n);
  }

  @Override
  public void run() {
    running = true;
    AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
      // buffer of 4 frames, ~67ms latency
      line.open(fmt, SAMPLES_PER_FRAME * 2 * 4);
      line.start();
      byte[] buf = new byte[SAMPLES_PER_FRAME * 2];

      while (running) {
        // 1. tick the sequencer once per audio frame @ 60 hz
        stepTrack(p1Track, n -> {
          if (n.midi < 0) {
            p1.noteOff();
          } else {
            p1.noteOn(midiToFreq(n.midi), n.volume, n.duty, 0.6);
          }
        });
        stepTrack(p2Track, n -> {
          if (n.midi < 0) {
            p2.noteOff();
          } else {
            p2.noteOn(midiToFreq(n.midi), n.volume, n.duty, 0.6);
          }
        });
        stepTrack(triTrack, n -> {
          if (n.midi < 0) {
            noi.noteOff();
          } else {
            // reuse midi field as freq selector for noise
            //  higher value = higher-pitched noise burst.
            double clock = midiToFreq(n.midi) * 8;
            noi.noteOn(clock, n.volume, 30.0, false);
          }
        });

        // 2. render one frame's worth of audio, which is 735 samples
        for (int i = 0; i < SAMPLES_PER_FRAME; i++) {
          if (volume < targetVolume) 
            volume = Math.min(targetVolume, volume + VOLUME_SLEW);
          else if (volume > targetVolume)
            volume = Math.max(targetVolume, volume - VOLUME_SLEW);
          double mix = (p1.sample() * p1Mix + p2.sample() * p2Mix 
                  + tri.sample() * triMix + noi.sample() * noiMix) * volume;
          if (mix > 1) {
            mix = 1;
          }
          if (mix < -1) {
            mix = -1;
          }
          short s = (short) (mix * 30000);
          buf[i * 2] = (byte) (s & 0xff);
          buf[i * 2 + 1] = (byte) ((s >> 8) & 0xff);
        }
        line.write(buf, 0, buf.length);
      }
      line.drain();
    } catch (LineUnavailableException e) {
      throw new RuntimeException(e);
    }
  }

  // start the synth on a daemon thread. returns the thread
  public Thread start() {
    Thread t = new Thread(this, "ChiptuneSynth");
    t.setDaemon(true);
    t.start();
    return t;
  }

  public void stop() {
    running = false;
  }
  
  public boolean isRunning() {
    return this.running;
  }
  
  public double getVolume() {
    return volume;
  }
  
  public void setVolume(double sliderPosition) {
    sliderPosition = Math.max(0, Math.min(1, sliderPosition));
    double gain = (sliderPosition <= 0) ? 0 : Math.pow(10, (sliderPosition - 1) * 2);
    this.targetVolume = gain;
  }

  private final static int BPM = 150;
  private final static int Q = 60 * FRAME_RATE / BPM; // quarter
  private final static int E = Q / 2; // eighth
  private final static int DQ = Q + E; // dotted quarter
  private final static int H = Q * 2; // half

  // octave 2, deep bass on triangle
  static final int G2 = 36, E2 = 40, F2 = 41, A2 = 45, B2 = 47;

  // octave 3, bass and low harmony
  static final int C3 = 48, D3 = 50, E3 = 52, F3 = 53;
  static final int G3 = 55, GS3 = 56, A3 = 57, B3 = 59;

  // octave 4, harmony, lower melody)
  static final int C4 = 60, D4 = 62, E4 = 64, F4 = 65;
  static final int G4 = 67, GS4 = 68;  // g#4 - the leading tone of A minor
  static final int A4 = 69, B4 = 71;

  // octave 5, main melody range
  static final int C5 = 72, D5 = 74, E5 = 76, F5 = 77;
  static final int G5 = 79, A5 = 81;

  // drum kit slots
  static final int KICK = 36, SNARE = 38, HIHAT_CLOSED = 42, HIHAT_OPEN = 46;

  private final static double LEAD_VOL = 0.7;
  private final static double LEAD_DUTY = 0.25; //most classic sounding
  
  private final static double HARMONY_VOL = 0.6;
  private final static double HARMONY_DUTY = 0.5; //fuller tone
  
  private final static double BASS_VOL = 1.0;
  private final static double BASS_DUTY = 0.5;

  public static Track getLead() {
    Track lead = new Track();
    // bar 1: E5 q | B4 e C5 e | D5 q | C5 e B4 e
    lead.add(new Note(E5, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(B4, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(C5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(D5, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(C5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(B4, E, LEAD_VOL, LEAD_DUTY));
    // bar 2: A4 q | A4 e C5 e | E5 q | D5 e C5 e
    lead.add(new Note(A4, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(A4, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(C5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(E5, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(D5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(C5, E, LEAD_VOL, LEAD_DUTY));
    // bar 3: B4 dq | C5 e | D5 q | E5 q
    lead.add(new Note(B4, DQ, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(C5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(D5, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(E5, Q, LEAD_VOL, LEAD_DUTY));
    // bar 4: C5 q | A4 q | A4 h
    lead.add(new Note(C5, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(A4, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(A4, H, LEAD_VOL, LEAD_DUTY));
    // bar 5: rest e | D5 q | F5 e | A5 q | G5 e | F5 e
    lead.add(Note.rest(E));
    lead.add(new Note(D5, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(F5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(A5, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(G5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(F5, E, LEAD_VOL, LEAD_DUTY));
    // bar 6: E5 dq | C5 e | E5 q | D5 e C5 e
    lead.add(new Note(E5, DQ, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(C5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(E5, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(D5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(C5, E, LEAD_VOL, LEAD_DUTY));
    // bar 7: B4 dq | C5 e | D5 q | E5 q
    lead.add(new Note(B4, DQ, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(C5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(D5, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(E5, Q, LEAD_VOL, LEAD_DUTY));
    // bar 8: C5 q | A4 q A4 q | rest q
    lead.add(new Note(C5, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(A4, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(A4, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(Note.rest(Q));
    // bar 9-10: descending phrase starting on e5
    lead.add(new Note(E5, H, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(C5, H, LEAD_VOL, LEAD_DUTY));
    // bar 11-12
    lead.add(new Note(D5, H, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(B4, H, LEAD_VOL, LEAD_DUTY));
    // bar 13-14: continue descending
    lead.add(new Note(C5, H, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(A4, H, LEAD_VOL, LEAD_DUTY));
    // bar 15-16: leading tone resolution back to A
    lead.add(new Note(GS4, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(B4, Q, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(C5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(D5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(E5, E, LEAD_VOL, LEAD_DUTY));
    lead.add(new Note(GS4, E, LEAD_VOL, LEAD_DUTY));
    
    return lead;
  }
  
  public static Track getHarmony() {
    Track harmony = new Track();
    
    // bar 1: Am (A-C-E) -> root then fifth
    harmony.add(new Note(A3, Q, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(E4, Q, HARMONY_VOL, HARMONY_DUTY));
    // bar 2: E7 (E-G#-B-D) -> root then third
    harmony.add(new Note(E3, Q, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(GS3, Q, HARMONY_VOL, HARMONY_DUTY));
    // bar 3: Am
    harmony.add(new Note(A3, Q, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(C4, Q, HARMONY_VOL, HARMONY_DUTY));
    // bar 4: Am
    harmony.add(new Note(A3, Q, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(E4, Q, HARMONY_VOL, HARMONY_DUTY));
    // bar 5: Dm (D-F-A)
    harmony.add(new Note(D3, Q, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(A3, Q, HARMONY_VOL, HARMONY_DUTY));
    // bar 6: Am
    harmony.add(new Note(A3, Q, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(E4, Q, HARMONY_VOL, HARMONY_DUTY));
    // bar 7: E7
    harmony.add(new Note(E3, Q, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(GS3, Q, HARMONY_VOL, HARMONY_DUTY));
    // bar 8: Am (resolve)
    harmony.add(new Note(A3, H, HARMONY_VOL, HARMONY_DUTY));
    // bar 9-10: Am
    harmony.add(new Note(A3, H, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(E4, H, HARMONY_VOL, HARMONY_DUTY));
    // bar 11-12: G major
    harmony.add(new Note(G3, H, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(D4, H, HARMONY_VOL, HARMONY_DUTY));
    // bar 13-14: F major
    harmony.add(new Note(F3, H, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(C4, H, HARMONY_VOL, HARMONY_DUTY));
    // bar 15-16: E7 -> Am resolution
    harmony.add(new Note(E3, Q, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(GS3, Q, HARMONY_VOL, HARMONY_DUTY));
    harmony.add(new Note(E3, H, HARMONY_VOL, HARMONY_DUTY));
    
    return harmony;
  }

  public static Track getBass() {
    Track bass = new Track();
    
    int[] amBass = { A2, E,  E3, E,  A2, E,  E3, E };
    int[] gBass  = { G2, E,  D3, E,  G2, E,  D3, E };
    int[] fBass  = { F2, E,  C3, E,  F2, E,  C3, E };
    int[] e7Bass = { E2, E,  B2, E,  E2, E,  B2, E };
    int[] dmBass = { D3, E,  A3, E,  D3, E,  A3, E };
    
    int[][] barPatterns = {
        amBass, e7Bass, amBass, amBass,   // bars 1-4
        dmBass, amBass, e7Bass, amBass,    // bars 5-8
        amBass, amBass,  // bars 9-10
        gBass,  gBass,   // bars 11-12
        fBass,  fBass,   // bars 13-14
        e7Bass, e7Bass   // bars 15-16
    };
    
    int note;
    int measure;
    
    for (int[] barPattern : barPatterns) {
      for (int j = 0; j < barPattern.length; j += 2) {
        note = barPattern[j];
        measure = barPattern[j+1];
        bass.add(new Note(note, measure, BASS_VOL, BASS_DUTY));
      }
    }
    
    return bass;
  }

  public static Track getDrums() {
    Track drums = new Track();
    for (int bar = 0; bar < 7; bar++) {
      drums.add(new Note(KICK, E, 0.5, 0.5));
      drums.add(Note.rest(E));
      drums.add(new Note(SNARE, E, 0.4, 0.5));
      drums.add(Note.rest(E));
      drums.add(new Note(KICK, E, 0.5, 0.5));
      drums.add(Note.rest(E));
      drums.add(new Note(SNARE, E, 0.4, 0.5));
      drums.add(Note.rest(E));
    }
    
    drums.add(new Note(KICK,  E, 0.5, 0.5));
    drums.add(new Note(SNARE, E, 0.4, 0.5));
    drums.add(new Note(KICK,  E, 0.5, 0.5));
    drums.add(new Note(SNARE, E, 0.4, 0.5));
    drums.add(new Note(KICK,  E, 0.5, 0.5));
    drums.add(new Note(SNARE, E, 0.4, 0.5));
    drums.add(new Note(KICK,  E, 0.5, 0.5));
    drums.add(new Note(SNARE, E, 0.4, 0.5));
    
    return drums;
  }

  public static ChiptuneSynth getSynthesizer() {
    ChiptuneSynth s = new ChiptuneSynth()
            .setSong(getLead(), getHarmony(), getBass(), getDrums())
            .setLooping(true);
    return s;
  }
}
