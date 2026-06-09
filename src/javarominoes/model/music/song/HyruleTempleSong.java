/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.music.song;

import chiptunesynth.ChiptuneSong;
import chiptunesynth.Track;

/**
 *
 * @author dylan
 */
public class HyruleTempleSong implements ChiptuneSong {

  private static Track getIntro() {
    Track t = new Track().withDefaults(LEAD_VOL, LEAD_DUTY);
    /* INTRO, SIX BARS */
    for (int introBar = 1; introBar <= 4; ++introBar) {
      for (int eighth = 1; eighth <= 8; ++eighth) {
        switch (eighth % 3) {
          case 1:
            if (introBar == 1) t.addNotes(D5, E);
            else if (introBar % 2 == 0) t.addNotes(DS5, E);
            else t.addNotes(E5, E);
            break;
          case 2:
            t.addNotes(AS4, E);
            break;
          default:
            t.addNotes(G4, E);
            break;
        }
      }
    }
    t.withDecay(STACCATO).addNotes(G4, E, G4, Q, G4, Q, G4, DQ);
    t.withDecay(DEFAULT_DECAY).addNotes(AS4, DQ, A4, DQ, GS4, Q);
    /* END INTRO */
    return t;
  }
  
  private static Track getVerse() {
    Track t = new Track().withDefaults(LEAD_VOL, LEAD_DUTY);
    /* VERSE, EIGHT BARS */
    
    int[] verseBarPattern = {
      G4, E*5, A4, E, AS4, E, D5, E,
      DS5, DQ, AS4, DQ, DS5, Q,
      E5, DQ, AS4, DQ, E5, Q,
      DS5, Q, G5, E, F5, Q, DS5, DQ
    };
    
    for (int verseBarChunk = 1; verseBarChunk <= 2; ++verseBarChunk) {
      t.addNotes(verseBarPattern);
    }
    
    /* END VERSE */
    return t;
  }
  
  private static Track getPreChorus() {
    Track t = new Track().withDefaults(LEAD_VOL, LEAD_DUTY);
    /* PRE-CHORUS, EIGHT BARS */
      
    // every in-between eighth-rest is folded into the note before it, so an
    // eighth + eighth-rest (E + E) becomes one quarter (Q). same 96 frames per
    // bar, but the line now sustains through the gap instead of being hard-cut
    // to silence by the rest (see PulseChannel.noteOff). the downbeat rest that
    // opens each even bar is a real syncopation, so it stays a rest.
    int[] barsTwoFourAndSix = {R, Q, D5, Q, FS5, Q, A5, Q};

    int[] oddBarFirstFour = {D5, E, D5, Q, D5, Q, D5, E};

    for (int preChorusBars = 1; preChorusBars <= 8; ++preChorusBars) {

      if (preChorusBars % 2 > 0) {

        t.addNotes(oddBarFirstFour);

        boolean isBar1orBar5 = (preChorusBars - 1) % 4 == 0;
        if (isBar1orBar5)
          t.addNotes(C5, Q);
        else if (preChorusBars == 3) {
          t.addNotes(DS5, Q);
        }
        else {
          t.addNotes(F5, Q);
        }
      } else {
        if (preChorusBars == 8) {
          t.addNotes(R, DQ, DS5, DQ, D5, Q);
        }
        else {
          t.addNotes(barsTwoFourAndSix);
        }
      }
    }
    
    t.withDecay(DEFAULT_DECAY);
    
    /* END PRE-CHORUS */
    return t;
  }
  
  private static Track getChorus() {
    Track t = new Track().withDefaults(LEAD_VOL, LEAD_DUTY);
    /* CHORUS, NINE BARS */
    
    int[] barSeqOneTwoFiveSix = {
      C5, E, A4, E, AS4, E, C5, Q, AS4, E, A4, Q,
      A4, E, FS4, E, G4, E, A4, Q, G4, E, FS4, Q
    };
    
    t.addNotes(barSeqOneTwoFiveSix);
    t.addNotes(F5, E, C5, E, D5, E, DS5, Q, D5, E, C5, Q); // bar 3
    t.addNotes(C5, E, GS4, E, AS4, E, C5, Q, D5, E, DS5, Q); // bar 4
    t.addNotes(barSeqOneTwoFiveSix);
    
    /* bar 7 */
    t.withDecay(STACCATO).addNotes(D4, E, D4, E, R, E);
    t.withDecay(GENTLE_FADE).addNotes(DS4, 5*E);
    /* bar 8 */
    t.withDecay(STACCATO).addNotes(D4, E, D4, E, R, E);
    t.withDecay(GENTLE_FADE).addNotes(FS4, Q, A4, Q, C5, E);
    /* bar 9 */
    t.withDecay(GENTLE_FADE).addNotes(D4, W);
    t.withDecay(DEFAULT_DECAY);
    
    /* END CHORUS */
    return t;
  }
  
  private static Track getBridge() {
    Track t = new Track().withDefaults(LEAD_VOL, LEAD_DUTY);
    /* BRIDGE, EIGHT BARS */
    
    t.addNotes(D5, E*5, C5, Q, AS4, E); // bar 1
    t.addNotes(A4, SX, AS4, SX, C5, SX, D5, SX, DS5, SX, F5, SX); // bar 2
    t.addNotes(F5, 5*E, DS5, Q, D5, E); // bar 3
    t.addNotes(DS5, SX, D5, SX, C5, SX, D5, SX, DS5, SX, F5, SX); // bar 4
    t.addNotes(G5, 5*E, G4, Q, A4, E); // bar 5
    t.addNotes(B4, SX, C5, SX, D5, SX, E5, SX, F5, SX, G5, SX); // bar 6
    t.addNotes(F5, 5*E, DS5, Q, D5, E); // bar 7
    t.addNotes(DS5, SX, D5, SX, C5, SX, D5, SX, DS5, SX, F5, SX); // bar 8

    /* END BRIDGE */
    return t;
  }
  
  @Override
  public Track getLead() {
    return new Track()
            .withDefaults(LEAD_VOL, LEAD_DUTY) // enough repeats to hide loop
            .addNotes(getIntro())
            .addNotes(getVerse())
            .addNotes(getPreChorus())
            .addNotes(getVerse())
            .addNotes(getChorus())
            .addNotes(getBridge())
            .addNotes(getBridge())
            .addNotes(getPreChorus())
            .addNotes(getVerse())
            .addNotes(getChorus())
            .addNotes(getBridge())
            .addNotes(getBridge())
            .addNotes(getPreChorus())
            .addNotes(getVerse())
            .addNotes(getChorus())
            .addNotes(getBridge())
            .addNotes(getBridge());
  }

  /* ===================================================================
   * ACCOMPANIMENT
   *
   * Per-bar chord map taken from the actual harmonic analysis (bass note
   * = the chord's slash/bass note, harmony pad = a chord tone chosen to
   * define quality without clashing the lead). Key center is G minor.
   *
   *   intro      : gm  Eb/G  e°/G  Eb/G  gm  | Bb Am Ab   (6 bars)
   *   verse      : gm  Eb/G  e°/G  Eb/G        (4-bar cell x2, G pedal)
   *   pre-chorus : D ... D7 ... D7b9           (dominant pedal, 8 bars)
   *   chorus     : D7 D7  fm7 fm7  D7 D7  D/F# D/F# D   (9 bars)
   *   bridge     : gm gm  fm fm  em em  fm fm           (8 bars)
   *
   * intro+verse are all first-inversion chords over a G pedal, and gm /
   * Eb / e-dim every one contains Bb -- so a Bb pad never clashes and
   * lets the lead spell the changes (D vs Eb vs E-natural).
   *
   * Every section is a whole number of 96-frame bars, so all four
   * channels share one frame length and loop together.
   * =================================================================== */

  // Per-bar bass notes (octave 2) and harmony pad tones (octave 3-4).
  private static final int[] VERSE_BASS  = {G2, G2, G2, G2, G2, G2, G2, G2}; // G pedal
  private static final int[] VERSE_HARM  = {AS3, AS3, AS3, AS3, AS3, AS3, AS3, AS3}; // Bb pad

  private static final int[] PRECH_BASS  = {D2, D2, D2, D2, D2, D2, D2, D2};
  private static final int[] PRECH_HARM  = {FS3, FS3, FS3, FS3, FS3, FS3, FS3, FS3};

  // D7 D7 fm7 fm7 D7 D7 D/F# D/F# D
  private static final int[] CHORUS_BASS = {D2, D2, F2, F2, D2, D2, FS2, FS2, D2};
  private static final int[] CHORUS_HARM = {FS3, FS3, GS3, GS3, FS3, FS3, C4, A3, FS3};

  // gm gm fm fm em em fm fm
  private static final int[] BRIDGE_BASS = {G2, G2, F2, F2, E2, E2, F2, F2};
  private static final int[] BRIDGE_HARM = {AS3, AS3, GS3, GS3, G3, G3, GS3, GS3};

  // Drum mix. KICK is intercepted by the synth's KickVoice and ignores
  // this level; SNARE/HIHAT play on the noise channel.
  private static final double KICK_VOL  = 0.9;
  private static final double SNARE_VOL = 0.55;
  private static final double HIHAT_VOL = 0.28;

  /* --- builders shared by every section --- */

  // one bar of driving eighth-note octave bass on the given root
  private static void octaveBar(Track t, int root) {
    int hi = root + 12;
    t.addNotes(root, E, hi, E, root, E, hi, E,
               root, E, hi, E, root, E, hi, E);
  }

  private static Track bassFrom(int[] roots) {
    Track t = new Track().withDefaults(BASS_VOL, BASS_DUTY);
    for (int root : roots) octaveBar(t, root);
    return t;
  }

  // one sustained, swelling chord tone per bar (a pad-like inner voice)
  private static Track harmonyFrom(int[] tones) {
    Track t = new Track().withDefaults(HARMONY_VOL, HARMONY_DUTY)
                         .withSwell().withDecay(SUSTAINED);
    for (int tone : tones) t.addNotes(tone, W);
    return t;
  }

  // one bar of backbeat: kick 1 & 3, snare 2 & 4, hi-hat on the off-beats
  private static void grooveBar(Track t) {
    final int HIT = T, GAP = E - T;   // 3-frame hit + 9-frame gap = one eighth
    t.withVolume(KICK_VOL ).addNotes(KICK,  HIT, R, GAP)
     .withVolume(HIHAT_VOL).addNotes(HIHAT, HIT, R, GAP)
     .withVolume(SNARE_VOL).addNotes(SNARE, HIT, R, GAP)
     .withVolume(HIHAT_VOL).addNotes(HIHAT, HIT, R, GAP)
     .withVolume(KICK_VOL ).addNotes(KICK,  HIT, R, GAP)
     .withVolume(HIHAT_VOL).addNotes(HIHAT, HIT, R, GAP)
     .withVolume(SNARE_VOL).addNotes(SNARE, HIT, R, GAP)
     .withVolume(HIHAT_VOL).addNotes(HIHAT, HIT, R, GAP);
  }

  private static Track drumsFor(int bars) {
    Track t = new Track().withDefaults(DRUM_VOL, DRUM_DUTY);
    for (int b = 0; b < bars; ++b) grooveBar(t);
    return t;
  }

  // intro bass: held G pedal for bars 1-5 (gm / Eb/G / e°/G all sit on G),
  // then the Bb-Am-Ab descent of bar 6, matching the lead.
  private static Track getBassIntro() {
    Track t = new Track().withDefaults(BASS_VOL, BASS_DUTY).withDecay(SUSTAINED);
    for (int bar = 1; bar <= 5; ++bar) t.addNotes(G2, W);
    t.addNotes(AS2, DQ, A2, DQ, GS2, Q);   // Bb  Am  Ab
    return t;
  }

  // intro pad: a Bb held under the G-pedal bars (common to gm/Eb/e°),
  // then chromatic parallel-4ths under the lead's Bb-A-Ab in bar 6.
  private static Track getHarmonyIntro() {
    Track t = new Track().withDefaults(HARMONY_VOL, HARMONY_DUTY)
                         .withSwell().withDecay(SUSTAINED);
    for (int bar = 1; bar <= 5; ++bar) t.addNotes(AS3, W);
    t.addNotes(F4, DQ, E4, DQ, DS4, Q);    // 4ths below Bb  A  Ab
    return t;
  }

  private static Track silence(int frames) {
    return new Track().addNotes(R, frames);
  }

  @Override
  public Track getHarmony() {
    Track t = new Track().withDefaults(HARMONY_VOL, HARMONY_DUTY);
    t.addNotes(getHarmonyIntro())          // intro
     .addNotes(harmonyFrom(VERSE_HARM));
    for (int rep = 0; rep < 3; ++rep) {
      t.addNotes(harmonyFrom(PRECH_HARM))
       .addNotes(harmonyFrom(VERSE_HARM))
       .addNotes(harmonyFrom(CHORUS_HARM))
       .addNotes(harmonyFrom(BRIDGE_HARM))
       .addNotes(harmonyFrom(BRIDGE_HARM));
    }
    return t;
  }

  @Override
  public Track getBass() {
    Track t = new Track().withDefaults(BASS_VOL, BASS_DUTY);
    t.addNotes(getBassIntro())
     .addNotes(bassFrom(VERSE_BASS));
    for (int rep = 0; rep < 3; ++rep) {
      t.addNotes(bassFrom(PRECH_BASS))
       .addNotes(bassFrom(VERSE_BASS))
       .addNotes(bassFrom(CHORUS_BASS))
       .addNotes(bassFrom(BRIDGE_BASS))
       .addNotes(bassFrom(BRIDGE_BASS));
    }
    return t;
  }

  @Override
  public Track getDrums() {
    Track t = new Track().withDefaults(DRUM_VOL, DRUM_DUTY);
    t.addNotes(silence(576))                // intro
     .addNotes(drumsFor(8));                // verse
    for (int rep = 0; rep < 3; ++rep) {
      t.addNotes(drumsFor(8))               // pre-chorus
       .addNotes(drumsFor(8))               // verse
       .addNotes(drumsFor(9))               // chorus
       .addNotes(drumsFor(8))               // bridge
       .addNotes(drumsFor(8));              // bridge
    }
    return t;
  }

  @Override
  public double getTempoScale() {
    return 1.034;
  }
  
}
