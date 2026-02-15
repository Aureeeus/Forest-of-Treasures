package edu.tip.forestoftreasures.Model;

public class SettingsConfiguration {
  // Default game settings values
  private GameSettings gameSettings = new GameSettings(1f, 1f);

  public void updateMusicVol(float newVolume) {
    gameSettings = new GameSettings(newVolume, gameSettings.sfxVolume());
  }

  public void updateSfxVol(float newVolume) {
    gameSettings = new GameSettings(gameSettings.musicVolume(), newVolume);
  } 

  public GameSettings getGameSettings() {
    return gameSettings;
  }
}
