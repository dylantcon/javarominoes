/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.synth;

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
class NoiseChannel {

  int lfsr = 1;
  double phaseAcc = 0;
  double clockHz = 0;
  double envelope = 0;
  double envelopeDecay = 0;
  boolean shortMode = false;

  void noteOn(double clockRate, double volume, double decayPerSec, boolean shortMode) {
    this.clockHz = clockRate;
    this.envelope = volume;
    this.envelopeDecay = decayPerSec * volume / ChiptuneSynth.SAMPLE_RATE;
    this.shortMode = shortMode;
  }

  void noteOff() {
    this.envelope = 0;
  }

  double sample() {
    if (envelope <= 0 || clockHz <= 0) {
      return 0;
    }
    phaseAcc += clockHz / ChiptuneSynth.SAMPLE_RATE;
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
