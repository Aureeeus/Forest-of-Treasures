package edu.tip.forestoftreasures.Model;

public record GameSettings(
    float bgMusicVolume, 
    float sfxVolume, 
    float ambienceVolume, 
    boolean isReadAloudEnabled, 
    boolean isSkipDialogueEnabled) {
  public GameSettings {
    if (bgMusicVolume < 0f || bgMusicVolume > 1f) {
      throw new IllegalArgumentException("Background music volume must be between 0 and 1");
    }
    if (sfxVolume < 0f || sfxVolume > 1f) {
      throw new IllegalArgumentException("SFX volume must be between 0 and 1");
    }
    if (ambienceVolume < 0f || ambienceVolume > 1f) {
      throw new IllegalArgumentException("Ambience volume must be between 0 and 1");
    }
  }
}
