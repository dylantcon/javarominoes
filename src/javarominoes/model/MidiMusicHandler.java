/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

/**
 *
 * @author dylan
 */
public class MidiMusicHandler implements MusicHandler {

  private final Sequencer sequencer;
  private final Synthesizer synthesizer;
  private double currentVolume = 0.5;

  public MidiMusicHandler() throws MidiUnavailableException {
    sequencer = MidiSystem.getSequencer(false); // don't connect default
    synthesizer = MidiSystem.getSynthesizer();

    sequencer.open();
    synthesizer.open();

    sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
  }

  private void loadMidi() throws IOException, InvalidMidiDataException {
    // load MIDI from classpath (put korobeiniki.mid in the javarominoes package folder)
    InputStream midiStream = getClass().getResourceAsStream("korobeiniki.mid");
    if (midiStream != null) {
      sequencer.setSequence(MidiSystem.getSequence(midiStream));
      sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
    } else {
      System.err.println("Could not find korobeiniki.mid in resources");
    }
  }

  @Override
  public void startMusic() {
    try {
      loadMidi();
      sequencer.start();
    } catch (IOException | InvalidMidiDataException e) {
      System.out.println(Arrays.toString(e.getStackTrace()));
    }
    applyVolume();
  }

  @Override
  public void stopMusic() {
    if (sequencer != null && sequencer.isOpen()) {
      try (sequencer) {
        sequencer.stop();
        sequencer.setTickPosition(0);
      }
    }
  }

  @Override
  public void restartMusic() {
    stopMusic();
    startMusic();
  }

  @Override
  public String getMusicType() {
    return "Midi";
  }

  @Override
  public boolean doingPlayback() {
    return sequencer.isRunning();
  }

  @Override
  public void setVolume(double volume) {
    currentVolume = Math.max(0, Math.min(1, volume));
    applyVolume();
  }

  @Override
  public double getVolume() {
    return currentVolume;
  }
  
  private void applyVolume() {
    if (synthesizer == null) return;
    int value = (int)(currentVolume * 127);
    for (MidiChannel ch : synthesizer.getChannels()) {
      if (ch != null) ch.controlChange(11, value);
    }
  }

}
