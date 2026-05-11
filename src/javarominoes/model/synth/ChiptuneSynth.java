/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.synth;

import javarominoes.model.music.song.ChiptuneSong;
import javax.sound.sampled.*;

/**
 * A small nes-style chiptune synthesizer
 *
 * models the 2A03 APU at a high level: - two pulse (square) channels with
 * selectable duty cycles - one triangle channel with a quantized waveform - one
 * noise channel driven by a 15 bit linear feedback shift register
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

  /* MAIN INSTANCE MEMBER DATA */
  // thread associated with this chiptune synthesizer
  private static Thread synthThread = null;

  // player state
  private final PulseChannel p1 = new PulseChannel();
  private final PulseChannel p2 = new PulseChannel();
  private final TriangleChannel tri = new TriangleChannel();
  private final NoiseChannel noi = new NoiseChannel();

  private Track p1Track, p2Track, triTrack, noiTrack;
  private boolean looping = true;
  private volatile boolean running = false;

  private volatile double targetVolume = 0.5;
  private double volume = 0.5;
  private static final double VOLUME_SLEW = 0.001;

  // music speed factor
  private volatile double speed = 1.0;
  private double sequencerAccumulator = 0;

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
  
  public ChiptuneSynth setSong(ChiptuneSong song) {
    this.setSong(
            song.getLead(),
            song.getHarmony(),
            song.getBass(),
            song.getDrums()
    );
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
    AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
      // buffer of 4 frames, ~67ms latency
      line.open(fmt, SAMPLES_PER_FRAME * 2 * 4);
      line.start();
      byte[] buf = new byte[SAMPLES_PER_FRAME * 2];

      while (running) {
        sequencerAccumulator += speed;
        while (sequencerAccumulator >= 1.0) {
          // decrement sequencer accumulator by 1
          sequencerAccumulator -= 1.0;
          
          // 1. tick the sequencer once per audio frame @ 60 hz
          stepTrack(p1Track, n -> {
            if (n.midi < 0) {
              p1.noteOff();
            } else {
              p1.noteOn(midiToFreq(n.midi), n.volume, n.duty, n.decay);
            }
          });
          stepTrack(p2Track, n -> {
            if (n.midi < 0) {
              p2.noteOff();
            } else {
              p2.noteOn(midiToFreq(n.midi), n.volume, n.duty, n.decay);
            }
          });
          stepTrack(triTrack, n -> {
            if (n.midi < 0) {
              tri.noteOff();
            } else {
              tri.noteOn(midiToFreq(n.midi));
            }
          });
          stepTrack(noiTrack, n -> {
            if (n.midi < 0) {
              noi.noteOff();
            } else {
              // reuse midi field as freq selector for noise
              //  higher value = higher-pitched noise burst.
              double clock = midiToFreq(n.midi) * 8;
              noi.noteOn(clock, n.volume, 30.0, false);
            }
          });
        }

        // 2. render one frame's worth of audio, which is 735 samples
        for (int i = 0; i < SAMPLES_PER_FRAME; i++) {
          if (volume < targetVolume) {
            volume = Math.min(targetVolume, volume + VOLUME_SLEW);
          } else if (volume > targetVolume) {
            volume = Math.max(targetVolume, volume - VOLUME_SLEW);
          }
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
  public synchronized Thread start() {
    if (!running) {
      running = true;
      synthThread = new Thread(this, "ChiptuneSynth");
      synthThread.setDaemon(true);
      synthThread.start();
    }
    return synthThread;
  }

  public synchronized void stop() {
    if (!running) {
      return;
    }
    running = false;
    Thread t = synthThread;
    synthThread = null;
    // wait for thread to actually finish its current frame and exit.
    if (t != null && t != Thread.currentThread()) {
      try {
        t.join(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
  
  public synchronized void rewind() {
    if (p1Track != null) p1Track.reset();
    if (p2Track != null) p2Track.reset();
    if (triTrack != null) triTrack.reset();
    if (noiTrack != null) noiTrack.reset();
  }

  public boolean isRunning() {
    return this.running;
  }

  public void setVolume(double sliderPosition) {
    sliderPosition = Math.max(0, Math.min(1, sliderPosition));
    double gain = (sliderPosition <= 0) ? 0 : Math.pow(10, (sliderPosition - 1) * 2);
    this.targetVolume = gain;
  }

  public void setSpeed(double speed) {
    this.speed = Math.max(0, speed);
  }
  
  public double getSpeed() { return this.speed; }

  public static ChiptuneSynth getSynthesizer(ChiptuneSong s) {
    ChiptuneSynth syn = new ChiptuneSynth()
            .setSong(s)
            .setLooping(true);
    return syn;
  }
}