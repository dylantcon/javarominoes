/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.music.song;

import chiptunesynth.ChiptuneSong;
import chiptunesynth.Note;
import chiptunesynth.Track;

/**
 *
 * @author dylan
 */
public class KorobeinikiSong implements ChiptuneSong {
  
  @Override
  public Track getLead() {
    Track lead = new Track().withDefaults(LEAD_VOL, LEAD_DUTY);
    // Vibrato sings on the sustained half-notes of the B-section (E5/C5/D5/B4
    // held for H); the 10-frame delay keeps the bouncy eighth-note A-section
    // crisp and articulate.
    lead.withVibrato(0.3, 5.5, 10);
    lead.addNotes(
        // bar 1: E5 q | B4 e C5 e | D5 q | C5 e B4 e
        E5, Q, B4, E, C5, E, D5, Q, C5, E, B4, E,
        // bar 2: A4 q | A4 e C5 e | E5 q | D5 e C5 e
        A4, Q, A4, E, C5, E, E5, Q, D5, E, C5, E,
        // bar 3: B4 dq | C5 e | D5 q | E5 q
        B4, DQ, C5, E, D5, Q, E5, Q,
        // bar 4: C5 q | A4 q | A4 h
        C5, Q, A4, Q, A4, H,
        // bar 5: rest e | D5 q | F5 e | A5 q | G5 e | F5 e
        R, E, D5, Q, F5, E, A5, Q, G5, E, F5, E,
        // bar 6: E5 dq | C5 e | E5 q | D5 e C5 e
        E5, DQ, C5, E, E5, Q, D5, E, C5, E,
        // bar 7: B4 dq | C5 e | D5 q | E5 q
        B4, DQ, C5, E, D5, Q, E5, Q,
        // bar 8: C5 q | A4 q A4 q | rest q
        C5, Q, A4, Q, A4, Q, R, Q,
        // bar 9-10: descending phrase starting on e5
        E5, H, C5, H,
        // bar 11-12
        D5, H, B4, H,
        // bar 13-14: continue descending
        C5, H, A4, H,
        // bar 15-16: leading tone resolution back to A
        GS4, Q, B4, Q, C5, E, D5, E, E5, E, GS4, E
    );
    return lead;
  }

  @Override
  public Track getHarmony() {
    Track harmony = new Track().withDefaults(HARMONY_VOL, HARMONY_DUTY);

    // Two sustained chord tones (root, then a colour tone) per 96-frame lead
    // phrase — the plain inner voice the original used, just stretched so each
    // chord fills a whole phrase. NO arpeggio: cycling the chord tones every
    // frame read as a sci-fi "laser" warble, so we hold real tones instead.
    // 12 phrases x (H + H) = 1152 frames, locked to the lead. Progression:
    // Am E7 Am Am Dm Am E7 Am under the A-theme, then Am G F E7 under the
    // descending B-theme — one chord per phrase, finally aligned to the melody.
    harmony.addNotes(
        A3, H, E4, H,    // phrase 1   Am
        E3, H, GS3, H,   // phrase 2   E7
        A3, H, C4, H,    // phrase 3   Am
        A3, H, E4, H,    // phrase 4   Am
        D3, H, A3, H,    // phrase 5   Dm
        A3, H, E4, H,    // phrase 6   Am
        E3, H, GS3, H,   // phrase 7   E7
        A3, H, C4, H,    // phrase 8   Am
        A3, H, E4, H,    // phrase 9   Am
        G3, H, D4, H,    // phrase 10  G
        F3, H, C4, H,    // phrase 11  F
        E3, H, GS3, H    // phrase 12  E7 -> loops to Am
    );

    return harmony;
  }

  @Override
  public Track getBass() {
    Track bass = new Track().withDefaults(BASS_VOL, BASS_DUTY);

    int[] amBass = {A2, E, E3, E, A2, E, E3, E};
    int[] gBass = {G2, E, D3, E, G2, E, D3, E};
    int[] fBass = {F2, E, C3, E, F2, E, C3, E};
    int[] e7Bass = {E2, E, B2, E, E2, E, B2, E};
    int[] dmBass = {D3, E, A3, E, D3, E, A3, E};

    // Each pattern is one 48-frame half-phrase. The A-section chords are doubled
    // (played twice) so every chord spans a full 96-frame lead phrase, matching
    // the stretched harmony; the B-section already ran two patterns per chord.
    // 24 patterns x 48 = 1152 frames, locked to the lead. (See getHarmony's
    // ALIGNMENT note — the old 16-pattern / 768-frame backing drifted 3:2.)
    int[][] barPatterns = {
      amBass, amBass, e7Bass, e7Bass, // phrases 1-2  Am  E7
      amBass, amBass, amBass, amBass, // phrases 3-4  Am  Am
      dmBass, dmBass, amBass, amBass, // phrases 5-6  Dm  Am
      e7Bass, e7Bass, amBass, amBass, // phrases 7-8  E7  Am
      amBass, amBass, // phrase 9   Am
      gBass, gBass,   // phrase 10  G
      fBass, fBass,   // phrase 11  F
      e7Bass, e7Bass  // phrase 12  E7
    };

    for (int[] barPattern : barPatterns) {
        bass.addNotes(barPattern);
    }

    return bass;
  }

  @Override
  public Track getDrums() {
    Track drums = new Track();
    // 11 backbeat bars (96 frames each) + the 96-frame fill below = 1152 frames,
    // matching the lead. The old 7-bar version summed to 768 and looped early.
    for (int bar = 0; bar < 11; bar++) {
      drums.add(new Note(KICK, E, 0.5, 0.5));
      drums.add(Note.rest(E));
      drums.add(new Note(SNARE, E, 0.4, 0.5));
      drums.add(Note.rest(E));
      drums.add(new Note(KICK, E, 0.5, 0.5));
      drums.add(Note.rest(E));
      drums.add(new Note(SNARE, E, 0.4, 0.5));
      drums.add(Note.rest(E));
    }

    drums.add(new Note(KICK, E, 0.5, 0.5));
    drums.add(new Note(SNARE, E, 0.4, 0.5));
    drums.add(new Note(KICK, E, 0.5, 0.5));
    drums.add(new Note(SNARE, E, 0.4, 0.5));
    drums.add(new Note(KICK, E, 0.5, 0.5));
    drums.add(new Note(SNARE, E, 0.4, 0.5));
    drums.add(new Note(KICK, E, 0.5, 0.5));
    drums.add(new Note(SNARE, E, 0.4, 0.5));

    return drums;
  }
}
