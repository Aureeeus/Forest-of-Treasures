package edu.tip.forestoftreasures.Model;

public record GameSettings(float musicVolume, float sfxVolume) {
  public GameSettings {
    if (musicVolume < 0f || musicVolume > 1f) {
      throw new IllegalArgumentException("Music volume must be between 0 and 1");
    }
    if (sfxVolume < 0f || sfxVolume > 1f) {
      throw new IllegalArgumentException("SFX volume must be between 0 and 1");
    }
  }
}
