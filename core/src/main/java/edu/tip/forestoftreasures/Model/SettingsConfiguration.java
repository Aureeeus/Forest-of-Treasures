package edu.tip.forestoftreasures.Model;

public class SettingsConfiguration {
  // Default game settings values
  private GameSettings gameSettings = new GameSettings(1f, 1f, 1f, false, false);

  public void updateBgMusicVol(float newVolume) {
    gameSettings = new GameSettings(
      newVolume, 
      gameSettings.sfxVolume(), 
      gameSettings.ambienceVolume(), 
      gameSettings.isReadAloudEnabled(),
      gameSettings.isSkipDialogueEnabled());
  }

  public void updateSfxVol(float newVolume) {
    gameSettings = new GameSettings(
      gameSettings.bgMusicVolume(), 
      newVolume, 
      gameSettings.ambienceVolume(), 
      gameSettings.isReadAloudEnabled(), 
      gameSettings.isSkipDialogueEnabled());
  } 

  public void updateAmbienceVol(float newVolume) {
    gameSettings = new GameSettings(
      gameSettings.bgMusicVolume(), 
      gameSettings.sfxVolume(), 
      newVolume, 
      gameSettings.isReadAloudEnabled(), 
      gameSettings.isSkipDialogueEnabled());
  } 

  public void updateReadAloud(boolean isReadAloudEnabled) {
    gameSettings = new GameSettings(
      gameSettings.bgMusicVolume(), 
      gameSettings.sfxVolume(), 
      gameSettings.ambienceVolume(), 
      isReadAloudEnabled, 
      gameSettings.isSkipDialogueEnabled());
  } 

  public void updateSkipDialogue(boolean isSkipDialogueEnabled) {
    gameSettings = new GameSettings(
      gameSettings.bgMusicVolume(), 
      gameSettings.sfxVolume(), 
      gameSettings.ambienceVolume(), 
      gameSettings.isReadAloudEnabled(), 
      isSkipDialogueEnabled);
  } 

  public GameSettings getGameSettings() {
    return gameSettings;
  }
}
