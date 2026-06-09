package javarominoes.model.music.song;

// Generated from btdslv5surf.mid
import chiptunesynth.ChiptuneSong;
import chiptunesynth.Track;

// (Battletoads - Stage 5: Surf City, by David Wise).
/**
 *
 * @author dylan
 */
public class SurfCitySong implements ChiptuneSong {

  private static final int    LOOP_FRAMES           = 3625;
  private static final double ORIGINAL_LOOP_SECONDS = 57.31;

  @Override
  public double getTempoScale() {
    return (LOOP_FRAMES / 60.0) / ORIGINAL_LOOP_SECONDS;
  }

  private static final int LEAD_TRANSPOSE = -12;

  private static final double PAD_FIFTH_VOL = 0.3;

  private static final double SNARE_VOL = 0.70;
  private static final double HIHAT_VOL = 0.30;

  @Override
  public Track getLead() {
    Track t = new Track().withDefaults(LEAD_VOL, LEAD_DUTY)
            .withTranspose(LEAD_TRANSPOSE);
    t.withVibrato(0.4, 5.5, 8);
    t.addNotes(
    C4, 5, R, 11, C4, 7, C4, 8, DS4, 5, R, 3,
    F4, 5, R, 11, G4, 5, R, 10, G4, 8, F4, 5,
    R, 11, DS4, 5, R, 3, F4, 5, R, 18, C4, 5,
    R, 3, C4, 5
    );
    lendFifth(t, DS5);  // chime matches the 3rd: Eb4 top after the -12 lead correction
    t.addNotes(
    C4, 5, R, 11, C4, 7,
    C4, 8, DS4, 5, R, 3, F4, 5, R, 11, G4, 5,
    R, 10, G4, 8, F4, 5, R, 11, DS4, 5, R, 3,
    F4, 5, R, 18, C4, 5, R, 3, C4, 5
    );
    lendFifth(t, DS5);  // chime matches the 3rd: Eb4 top after the -12 lead correction
    t.addNotes(
    F4, 5, R, 11, F4, 7, F4, 8, GS4, 5, R, 3,
    AS4, 5, R, 11, C5, 5, R, 10, C5, 8, AS4, 5,
    R, 11, GS4, 5, R, 3, AS4, 5, R, 18, F4, 5,
    R, 3, F4, 5
    );
    lendFifth(t, DS5);
    t.addNotes(
    C4, 5, R, 11, C4, 7,
    C4, 8, DS4, 5, R, 3, F4, 5, R, 11, G4, 5,
    R, 10, G4, 8, F4, 5, R, 11, DS4, 5, R, 3,
    F4, 5, R, 18, C4, 5, R, 3, C4, 5
    );
    lendFifth(t, DS5);  // chime matches the 3rd: Eb4 top after the -12 lead correction
    t.addNotes(
    AS3, 5, R, 11, AS3, 7, AS3, 8, F4, 21, R, 3,
    DS4, 5, R, 10, DS4, 6, R, 10, DS4, 5, R, 11,
    DS4, 7, F4, 8, F4, 5, R, 3, G3, 5, R, 3,
    C4, 5, R, 3, D4, 7, DS4, 8, G4, 5, R, 3,
    DS4, 5, R, 3, D4, 5, R, 3, DS4, 5, R, 3,
    GS3, 7, C4, 8, D4, 5, R, 3, DS4, 5, R, 3,
    G4, 5, R, 3, DS4, 7, D4, 8, DS4, 5, R, 3,
    AS3, 5, R, 3, C4, 5, R, 3, D4, 7, DS4, 8,
    G4, 5, R, 3, DS4, 5, R, 3, D4, 5, R, 3,
    DS4, 7, GS3, 8, C4, 8, D4, 5, R, 3, DS4, 5,
    R, 3, G4, 5, R, 3, DS4, 7, D4, 8, DS4, 5,
    R, 3, G3, 5, R, 3, C4, 5, R, 3, D4, 7,
    DS4, 8, G4, 5, R, 3, DS4, 5, R, 3, D4, 5,
    R, 3, DS4, 5, R, 3, GS3, 7, C4, 8, D4, 5,
    R, 3, DS4, 5, R, 3, G4, 5, R, 3, DS4, 7,
    D4, 8, DS4, 5, R, 3, AS3, 5, R, 3, C4, 5,
    R, 3, D4, 7, DS4, 8, G4, 5, R, 3, DS4, 5,
    R, 3, D4, 5, R, 3, DS4, 7, GS3, 8, C4, 8,
    D4, 5, R, 3, DS4, 5, R, 3, G4, 5, R, 3,
    DS4, 7, D4, 8, DS4, 5, R, 3, G3, 5, R, 3,
    C4, 5, R, 3, D4, 7, DS4, 8, G4, 5, R, 3,
    DS4, 5, R, 3, D4, 5, R, 3, DS4, 5, R, 3,
    GS3, 7, C4, 8, D4, 5, R, 3, DS4, 5, R, 3,
    G4, 5, R, 3, DS4, 7, D4, 8, DS4, 5, R, 3,
    AS3, 5, R, 3, C4, 5, R, 3, D4, 7, DS4, 8,
    G4, 5, R, 3, DS4, 5, R, 3, D4, 5, R, 3,
    DS4, 7, GS3, 8, C4, 8, D4, 5, R, 3, DS4, 5,
    R, 3, G4, 5, R, 3, DS4, 7, D4, 8, DS4, 5,
    R, 3, G3, 5, R, 3, C4, 5, R, 3, D4, 7,
    DS4, 8, G4, 5, R, 3, DS4, 5, R, 3, D4, 5,
    R, 3, DS4, 7, GS3, 8, C4, 8, D4, 5, R, 3,
    DS4, 5, R, 3, G4, 5, R, 3, DS4, 7, D4, 8,
    DS4, 5, R, 3, AS3, 5, R, 3, C4, 5, R, 3,
    D4, 7, DS4, 8, G4, 5, R, 3, DS4, 5, R, 3,
    D4, 5, R, 3, DS4, 7, GS3, 8, C4, 8, D4, 5,
    R, 3, DS4, 5, R, 3, G4, 5, R, 3, DS4, 7,
    D4, 8, AS3, 5, R, 3, C4, 5, R, 11, C4, 7,
    C4, 8, DS4, 5, R, 3, F4, 5, R, 11, G4, 5,
    R, 10, G4, 8, F4, 5, R, 11, DS4, 5, R, 3,
    F4, 5, R, 143, C4, 5, R, 11, C4, 7, C4, 8,
    DS4, 5, R, 3, F4, 5, R, 11, G4, 5, R, 10,
    G4, 8, F4, 5, R, 11, DS4, 5, R, 3, F4, 5,
    R, 143, C4, 5, R, 11, C4, 7, C4, 8, DS4, 5,
    R, 3, F4, 5, R, 11, G4, 5, R, 10, G4, 8,
    F4, 5, R, 11, DS4, 5, R, 3, F4, 5, R, 143,
    C4, 5, R, 11, C4, 7, C4, 8, DS4, 5, R, 3,
    F4, 5, R, 11, G4, 5, R, 10, G4, 8, F4, 5,
    R, 11, DS4, 5, R, 3, F4, 5, R, 143, C3, 5,
    R, 11, C3, 7, C3, 8, G3, 5, R, 3, AS3, 5,
    R, 11, C4, 5, R, 10, C4, 8, AS3, 5, R, 11,
    F3, 5, R, 3, G3, 7, DS3, 8, F3, 5, R, 3,
    C3, 5, R, 11, C3, 7, C3, 8, G3, 5, R, 3,
    AS3, 5, R, 11, C4, 5, R, 10, C4, 8, AS3, 5,
    R, 11, F3, 5, R, 3, G3, 7, DS3, 8, F3, 5,
    R, 3, C3, 5, R, 11, C3, 7, C3, 8, G3, 5,
    R, 3, AS3, 5, R, 11, C4, 5, R, 10, C4, 8,
    AS3, 5, R, 11, F3, 5, R, 3, G3, 7, DS3, 8,
    F3, 5, R, 3, C3, 5, R, 11, C3, 7, C3, 8,
    G3, 5, R, 3, AS3, 5, R, 11, C4, 5, R, 10,
    C4, 8, AS3, 5, R, 11, F3, 5, R, 3, G3, 7,
    DS3, 8, F3, 5, R, 3
    );
    return t;
  }

  private void lendFifth(Track t, int fifthMidi) {
    t.addNotes(R, 18);
    // the swelled power-chord fifth is part of the lead's voice, so it needs the
    // same octave correction (-12) as the rest of the lead — the rip was an
    // octave high. (Originally withTranspose(0), which left this twang stranded
    // an octave above the melody: airy and far too high.)
    t.withTranspose(LEAD_TRANSPOSE).withVolume(PAD_FIFTH_VOL).withDuty(HARMONY_DUTY)
     .withNoFx().withSwell().withDecay(SUSTAINED).addNotes(fifthMidi, 52);
    t.withTranspose(LEAD_TRANSPOSE).withVolume(LEAD_VOL).withDuty(LEAD_DUTY)
     .withNoFx().withVibrato(0.4, 5.5, 8).withDecay(DEFAULT_DECAY).addNotes(R, 42);
  }

  @Override
  public Track getHarmony() {
    Track t = new Track().withDefaults(HARMONY_VOL, HARMONY_DUTY);

    t.addNotes(R, 156);

    t.withSwell().withDecay(SUSTAINED).addNotes(GS3, 52);  // chime matches the 3rd: Ab over C bass
    t.withNoFx().withDecay(DEFAULT_DECAY).addNotes(
    C4, 2, R, 9, DS4, 5, R, 3, DS4, 5, R, 174
    );

    t.withSwell().withDecay(SUSTAINED).addNotes(GS3, 52);  // chime matches the 3rd: Ab over C bass
    t.withNoFx().withDecay(DEFAULT_DECAY).addNotes(
    C4, 2, R, 9, DS4, 5, R, 3, DS4, 5, R, 174
    );

    t.withSwell().withDecay(SUSTAINED).addNotes(GS3, 52);  // octave-corrected twang
    t.withNoFx().withDecay(DEFAULT_DECAY).addNotes(
    F4, 2, R, 9, GS4, 5, R, 3, GS4, 5, R, 174
    );

    t.withSwell().withDecay(SUSTAINED).addNotes(GS3, 52);  // chime matches the 3rd: Ab over C bass
    t.withNoFx().withDecay(DEFAULT_DECAY).addNotes(
    C4, 2, R, 9, DS4, 5, R, 3, DS4, 5, R, 1284
    );

    t.addNotes(
    C5, 5, R, 10, AS4, 5, R, 19, G4, 5, R, 10,
    G4, 8, F4, 5, R, 11, DS4, 5, R, 3, F4, 5, R, 159,
    C5, 5, R, 10, AS4, 5, R, 19, G4, 5, R, 10,
    G4, 8, F4, 5, R, 11, DS4, 5, R, 3, F4, 5, R, 159,
    C5, 5, R, 10, AS4, 5, R, 19, G4, 5, R, 10,
    G4, 8, F4, 5, R, 11, DS4, 5, R, 3, F4, 5, R, 159,
    C5, 5, R, 10, AS4, 5, R, 19, G4, 5, R, 10,
    G4, 8, F4, 5, R, 11, DS4, 5, R, 3, F4, 5, R, 268
    );

    t.addNotes(
    C4, 5, R, 11, C4, 7, C4, 8, G4, 5, R, 3, AS4, 5, R, 11,
    C5, 5, R, 10, C5, 8, AS4, 5, R, 11, F4, 5, R, 3,
    G4, 7, DS4, 8, F4, 5, R, 3,
    C4, 5, R, 11, C4, 7, C4, 8, G4, 5, R, 3, AS4, 5, R, 11,
    C5, 5, R, 10, C5, 8, AS4, 5, R, 11, F4, 5, R, 3,
    G4, 7, DS4, 8, F4, 5, R, 3
    );

    return t;
  }

  @Override
  public Track getBass() {
    Track t = new Track().withDefaults(BASS_VOL, BASS_DUTY);
    t.withSlide(4);
    t.addNotes(
    C3, 5, R, 3, C3, 5, R, 3, C3, 7, C3, 8,
    C3, 5, R, 3, C3, 5, R, 11, C3, 5, R, 10,
    C3, 8, C3, 5, R, 3, C3, 5, R, 3, C3, 5,
    R, 3, C3, 7, C3, 6, R, 41, C3, 52, R, 42, C3, 5, R, 3,
    C3, 5, R, 3, C3, 7, C3, 8, C3, 5, R, 3,
    C3, 5, R, 11, C3, 5, R, 10, C3, 8, C3, 5,
    R, 3, C3, 5, R, 3, C3, 5, R, 3, C3, 7,
    C3, 6, R, 41, C3, 52, R, 42, F3, 5, R, 3, F3, 5, R, 3,
    F3, 7, F3, 8, F3, 5, R, 3, F3, 5, R, 11,
    F3, 5, R, 10, F3, 8, F3, 5, R, 3, F3, 5,
    R, 3, F3, 5, R, 3, F3, 7, F3, 6, R, 41, F3, 52, R, 42,
    C3, 5, R, 3, C3, 5, R, 3, C3, 7, C3, 8,
    C3, 5, R, 3, C3, 5, R, 11, C3, 5, R, 10,
    C3, 8, C3, 5, R, 3, C3, 5, R, 3, C3, 5,
    R, 3, C3, 7, C3, 6, R, 41, C3, 52, R, 42, AS1, 10, R, 6,
    AS1, 7, AS1, 8, F2, 21, R, 323, C3, 5, R, 3,
    C3, 5, R, 10, C3, 6, R, 10, C3, 5, R, 11,
    C3, 5, R, 3, C3, 7, C3, 6, R, 10, C3, 5,
    R, 11, C3, 5, R, 10, C3, 5, R, 3, C3, 5,
    R, 3, C3, 5, R, 10, C3, 6, R, 10, C3, 5,
    R, 11, C3, 7, C3, 8, C3, 6, R, 10, C3, 5,
    R, 11, C3, 5, R, 10, C3, 5, R, 3, C3, 5,
    R, 3, AS2, 5, R, 10, C3, 6, R, 10, C3, 5,
    R, 11, C3, 5, R, 3, C3, 7, AS2, 6, R, 10,
    C3, 5, R, 11, C3, 7, DS3, 8, F3, 5, R, 3,
    C3, 5, R, 3, AS2, 5, R, 10, C3, 6, R, 10,
    C3, 5, R, 11, C3, 7, C3, 8, AS2, 6, R, 10,
    C3, 5, R, 11, C3, 7, DS3, 8, F3, 5, R, 3,
    C3, 5, R, 3, AS2, 5, R, 10, C3, 6, R, 10,
    C3, 5, R, 11, C3, 7, C3, 8, AS2, 6, R, 10,
    C3, 5, R, 11, C3, 7, DS3, 8, F3, 5, R, 3,
    C3, 5, R, 3, AS2, 5, R, 10, C3, 6, R, 10,
    C3, 5, R, 11, C3, 7, C3, 8, AS2, 6, R, 10,
    C3, 5, R, 11, C3, 7, DS3, 8, F3, 5, R, 3,
    C4, 125, C3, 10, R, 45, C3, 5, R, 10, C3, 8,
    C3, 5, R, 42, AS3, 125, GS3, 125, C4, 125, C3, 10,
    R, 45, C3, 5, R, 10, C3, 8, C3, 5, R, 42,
    AS3, 125, GS3, 125, R, 500
    );
    return t;
  }

  @Override
  public Track getDrums() {
    Track t = new Track().withDefaults(DRUM_VOL, DRUM_DUTY);
    // Intro + the long hi-hat run. Kicks here route to KickVoice and ignore
    // this level; the hi-hat ticks sit back at HIHAT_VOL.
    t.withVolume(HIHAT_VOL).addNotes(
    KICK, 3, R, 28, HIHAT, 3, R, 28, KICK, 3, R, 29,
    HIHAT, 3, R, 28, KICK, 3, R, 28, HIHAT, 3, R, 29,
    KICK, 3, R, 28, HIHAT, 3, R, 28, KICK, 3, R, 28,
    HIHAT, 3, R, 28, KICK, 3, R, 29, HIHAT, 3, R, 28,
    KICK, 3, R, 28, HIHAT, 3, R, 28, KICK, 3, R, 29,
    HIHAT, 3, R, 28, KICK, 3, R, 28, HIHAT, 3, R, 28,
    KICK, 3, R, 29, HIHAT, 3, R, 28, KICK, 3, R, 28,
    HIHAT, 3, R, 29, KICK, 3, R, 28, HIHAT, 3, R, 28,
    KICK, 3, R, 28, HIHAT, 3, R, 28, KICK, 3, R, 29,
    HIHAT, 3, R, 28, KICK, 3, R, 28, HIHAT, 3, R, 28,
    KICK, 3, R, 29, HIHAT, 3, R, 669, HIHAT, 3, R, 28,
    HIHAT, 3, R, 28, HIHAT, 3, R, 28, HIHAT, 3, R, 29,
    HIHAT, 3, R, 28, HIHAT, 3, R, 28, HIHAT, 3, R, 28,
    HIHAT, 3, R, 13, HIHAT, 3, R, 5, HIHAT, 3, R, 5,
    HIHAT, 3, R, 28, HIHAT, 3, R, 12, HIHAT, 3, R, 5,
    HIHAT, 3, R, 5, HIHAT, 3, R, 28, HIHAT, 3, R, 13,
    HIHAT, 3, R, 5, HIHAT, 3, R, 5, HIHAT, 3, R, 28,
    HIHAT, 3, R, 12, HIHAT, 3, R, 5, HIHAT, 3, R, 5,
    HIHAT, 3, R, 5, HIHAT, 3, R, 5, HIHAT, 3, R, 5,
    HIHAT, 3, R, 4, HIHAT, 3, R, 5, HIHAT, 3, R, 5
    );

    // The KICK/SNARE groove. The snare backbeat carries at SNARE_VOL; kicks
    // again route to KickVoice and ignore this level.
    t.withVolume(SNARE_VOL).addNotes(
    KICK, 3, R, 28, SNARE, 3, R, 36, KICK, 3, R, 5,
    KICK, 3, R, 13, SNARE, 3, R, 28, KICK, 3, R, 28,
    SNARE, 3, R, 36, KICK, 3, R, 5, KICK, 3, R, 13,
    SNARE, 3, R, 28, KICK, 3, R, 28, SNARE, 3, R, 36,
    KICK, 3, R, 5, KICK, 3, R, 13, SNARE, 3, R, 28,
    KICK, 3, R, 28, SNARE, 3, R, 36, KICK, 3, R, 5,
    KICK, 3, R, 13, SNARE, 3, R, 28, KICK, 3, R, 28,
    SNARE, 3, R, 36, KICK, 3, R, 5, KICK, 3, R, 13,
    SNARE, 3, R, 28, KICK, 3, R, 28, SNARE, 3, R, 36,
    KICK, 3, R, 5, KICK, 3, R, 13, SNARE, 3, R, 28,
    KICK, 3, R, 28, SNARE, 3, R, 36, KICK, 3, R, 5,
    KICK, 3, R, 13, SNARE, 3, R, 28, KICK, 3, R, 28,
    SNARE, 3, R, 36, KICK, 3, R, 5, KICK, 3, R, 13,
    SNARE, 3, R, 12, KICK, 3, R, 13, KICK, 3, R, 28,
    SNARE, 3, R, 29, KICK, 3, R, 28, SNARE, 3, R, 28,
    KICK, 3, R, 28, SNARE, 3, R, 28, KICK, 3, R, 29,
    SNARE, 3, R, 28, KICK, 3, R, 28, SNARE, 3, R, 29,
    KICK, 3, R, 28, SNARE, 3, R, 28, KICK, 3, R, 28,
    SNARE, 3, R, 28, KICK, 3, R, 29, SNARE, 3, R, 28
    );
    return t;
  }
}
