/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.synth;

/**
 *
 * @author dylan
 */
public class KorobeinikiSong implements ChiptuneSong {
  
  @Override
  public Track getLead() {
    Track lead = new Track().withDefaults(LEAD_VOL, LEAD_DUTY);
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

    // bar 1: Am (A-C-E) -> root then fifth
    harmony.addNotes(
        A3, Q, E4, Q,
        // bar 2: E7 (E-G#-B-D) -> root then third
        E3, Q, GS3, Q,
        // bar 3: Am
        A3, Q, C4, Q,
        // bar 4: Am
        A3, Q, E4, Q,
        // bar 5: Dm (D-F-A)
        D3, Q, A3, Q,
        // bar 6: Am
        A3, Q, E4, Q,
        // bar 7: E7
        E3, Q, GS3, Q,
        // bar 8: Am (resolve)
        A3, H,
        // bar 9-10: Am
        A3, H, E4, H,
        // bar 11-12: G major
        G3, H, D4, H,
        // bar 13-14: F major
        F3, H, C4, H,
        // bar 15-16: E7 -> Am resolution
        E3, Q, GS3, Q, E3, H
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

    int[][] barPatterns = {
      amBass, e7Bass, amBass, amBass, // bars 1-4
      dmBass, amBass, e7Bass, amBass, // bars 5-8
      amBass, amBass, // bars 9-10
      gBass, gBass, // bars 11-12
      fBass, fBass, // bars 13-14
      e7Bass, e7Bass // bars 15-16
    };

    for (int[] barPattern : barPatterns) {
        bass.addNotes(barPattern);
    }

    return bass;
  }

  @Override
  public Track getDrums() {
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
