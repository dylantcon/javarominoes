/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.synth;

/**
 * A triangle channel.
 *
 * real NES hardware steps through 32 levels; the staircase is part of the
 * distinctive character. approximate that by quantizing a continuous triangle
 * to 8 levels per half-cycle
 *
 * @author dylan
 */
class TriangleChannel {

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
    phase += freq / ChiptuneSynth.SAMPLE_RATE;
    if (phase >= 1.0) {
      phase -= 1.0;
    }
    // triangle in -1..+1 phase in 0..1
    double tri = phase < 0.5 ? phase * 4 - 1 : 3 - phase * 4;
    // quantize to 8 levels for NES stair-step character
    return Math.round(tri * 7) / 7.0;
  }
  
}
