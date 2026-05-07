/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.synth;

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
class PulseChannel {

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
    this.envelopeDecay = decayPerSec * volume / ChiptuneSynth.SAMPLE_RATE;
  }

  void noteOff() {
    this.envelope = 0;
  }

  double sample() {
    if (envelope <= 0 || freq <= 0) {
      return 0;
    }
    phase += freq / ChiptuneSynth.SAMPLE_RATE;
    if (phase >= 1.0) {
      phase -= 1.0;
    }
    double s = (phase < duty ? 1.0 : -1.0) * envelope;
    envelope = Math.max(0, envelope - envelopeDecay);
    return s;
  }
  
}
