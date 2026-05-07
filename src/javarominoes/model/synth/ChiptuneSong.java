/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package javarominoes.model.synth;

/**
 *
 * @author dylan
 */
public interface ChiptuneSong {
    /* === TEMPO / RHYTHM CONSTANTS === */
    public static final int BPM = 150;
    public static final int Q  = 60 * ChiptuneSynth.FRAME_RATE / BPM;  // quarter (24)
    public static final int H  = Q * 2;                                // half (48)
    public static final int W  = Q * 4;                                // whole (96)
    public static final int E  = Q / 2;                                // eighth (12)
    public static final int S  = Q / 4;                                // sixteenth (6)
    public static final int T  = Q / 8;                                // thirty-second (3)
    public static final int DQ = Q + E;                                // dotted quarter (36)
    public static final int DH = H + Q;                                // dotted half (72)
    public static final int DE = E + S;                                // dotted eighth (18)
    public static final int DS = S + T;                                // dotted sixteenth (9)
 
    /* === PITCH CONSTANTS (MIDI note numbers) === */
 
    // octave 2 - deep bass on triangle
    public static final int C2  = 36, CS2 = 37, D2  = 38, DS2 = 39;
    public static final int E2  = 40, F2  = 41, FS2 = 42, G2  = 43;
    public static final int GS2 = 44, A2  = 45, AS2 = 46, B2  = 47;
 
    // octave 3 - bass and low harmony
    public static final int C3  = 48, CS3 = 49, D3  = 50, DS3 = 51;
    public static final int E3  = 52, F3  = 53, FS3 = 54, G3  = 55;
    public static final int GS3 = 56, A3  = 57, AS3 = 58, B3  = 59;
 
    // octave 4 - harmony, lower melody
    public static final int C4  = 60, CS4 = 61, D4  = 62, DS4 = 63;
    public static final int E4  = 64, F4  = 65, FS4 = 66, G4  = 67;
    public static final int GS4 = 68, A4  = 69, AS4 = 70, B4  = 71;
 
    // octave 5 - main melody range
    public static final int C5  = 72, CS5 = 73, D5  = 74, DS5 = 75;
    public static final int E5  = 76, F5  = 77, FS5 = 78, G5  = 79;
    public static final int GS5 = 80, A5  = 81, AS5 = 82, B5  = 83;
 
    // octave 6 - high decoration
    public static final int C6  = 84, CS6 = 85, D6  = 86, DS6 = 87;
    public static final int E6  = 88, F6  = 89, FS6 = 90, G6  = 91;
 
    // octave 7 - extreme highs (used for cymbal noise pitch)
    public static final int C7  = 96;
 
    /* === DRUM NOISE PITCHES === */
    // These act as frequency selectors for the noise channel - chosen for
    // perceptual contrast rather than musical pitch. Wider spread = more
    // distinguishable kick/snare/hihat.
    public static final int KICK   = 36;   // = C2  - low rumble
    public static final int SNARE  = 60;   // = C4  - sharp crack
    public static final int HIHAT  = 84;   // = C6  - crisp tick
    public static final int CYMBAL = 96;   // = C7  - bright wash
 
    /* === SPECIAL: REST === */
    public static final int R = -1;
 
    /* === VOICE MIXING DEFAULTS === */
    public static final double LEAD_VOL    = 0.7;
    public static final double LEAD_DUTY   = 0.25;   // classic NES square lead
    public static final double HARMONY_VOL = 0.55;
    public static final double HARMONY_DUTY = 0.5;   // fuller for inner voice
    public static final double BASS_VOL    = 1.0;
    public static final double BASS_DUTY   = 0.5;    // ignored by triangle
    public static final double DRUM_VOL    = 0.5;
    public static final double DRUM_DUTY   = 0.5;    // ignored by noise

    /* === METHODS EACH SONG IMPLEMENTS === */
    Track getLead();
    Track getHarmony();
    Track getBass();
    Track getDrums();
}
