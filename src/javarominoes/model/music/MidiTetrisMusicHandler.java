/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.music;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

/**
 *
 * @author dylan
 */
public class MidiTetrisMusicHandler implements MusicHandler {

  private Sequencer sequencer;
  private Synthesizer synthesizer;
  private double currentVolume = 0.5;

  public MidiTetrisMusicHandler() {
    
    try {
      initializeSequencerAndSynthesizer();
    } catch (MidiUnavailableException ex) {
      System.out.println(Arrays.toString(ex.getStackTrace()));
    }
    try {
      loadMidi();
    } catch (IOException | InvalidMidiDataException  e) {
      System.out.println(Arrays.toString(e.getStackTrace()));
    }
  }
  
  public MidiTetrisMusicHandler(boolean doingPlayback, double volumeLevel, double speed) throws MidiUnavailableException {
    this();
    setVolume(volumeLevel);
    setSpeed(speed);
    if (doingPlayback)
      startMusic();
  }
  
  private void initializeSequencerAndSynthesizer() throws MidiUnavailableException {
    sequencer = MidiSystem.getSequencer(false); // don't connect default
    synthesizer = MidiSystem.getSynthesizer();

    sequencer.open();
    synthesizer.open();
    synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
    
    Receiver synthReceiver = synthesizer.getReceiver();
    Receiver filteringReceiver = new Receiver() {
      @Override
      public void send(MidiMessage message, long timeStamp) {
        // drop cc11 events from the file, as we'll control the volume ourselves
        if (message instanceof ShortMessage sm) {
          if (sm.getCommand() == ShortMessage.CONTROL_CHANGE
                  && sm.getData1() == 11 || sm.getData1() == 7) {
            return;
          }
        }
        synthReceiver.send(message, timeStamp);
      }
      @Override
      public void close() { synthReceiver.close(); }
    };
    sequencer.getTransmitter().setReceiver(filteringReceiver);
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
  public final void startMusic() {
    if (sequencer == null)
    {
      try {
        initializeSequencerAndSynthesizer();
      } catch (MidiUnavailableException e) {
        System.out.println(Arrays.toString(e.getStackTrace()));
      }
    }
    sequencer.start();
    applyVolume();
  }

  @Override
  public void stopMusic() {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.stop();
    }
  }

  @Override
  public void restartMusic() {
    sequencer.setTickPosition(0);
    sequencer.setTempoFactor((float)MusicHandler.BASE_SPEED);
    if (!doingPlayback()) {
      sequencer.start();
    }    
    applyVolume();
  }

  @Override
  public String getMusicType() {
    return "Korobeiniki - Midi";
  }

  @Override
  public boolean doingPlayback() {
    return sequencer.isRunning();
  }

  @Override
  public final void setVolume(double volume) {
    currentVolume = Math.max(0, Math.min(1, volume));
    applyVolume();
  }
  
  private void applyVolume() {
    if (synthesizer == null) return;
    double curved = currentVolume * Math.pow(currentVolume, 0.25);
    int value = (int)(curved * 127);
    for (MidiChannel ch : synthesizer.getChannels()) {
      if (ch != null) ch.controlChange(11, value);
    }
  }

  @Override
  public final void setSpeed(double speed) {
    sequencer.setTempoFactor((float)speed);
  }
  
  @Override
  public double getSpeed() {
    return (double)sequencer.getTempoFactor();
  }

}
