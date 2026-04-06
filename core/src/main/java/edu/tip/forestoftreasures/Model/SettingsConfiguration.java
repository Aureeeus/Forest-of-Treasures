package edu.tip.forestoftreasures.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Manages the client-side settings map and guarantees disk persistence 
 * using LibGDX's native cross-platform Preferences API.
 */
public class SettingsConfiguration {
  private static final String PREFS_NAME = "FOT_Settings";
  private GameSettings gameSettings;

  public SettingsConfiguration() {
    loadSettings();
  }

  /**
   * Loads settings from the device's local storage.
   * If the data is missing, invalid, or corrupted, it falls back to defaults.
   */
  private void loadSettings() {
    Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
    float bgMusicVolume = prefs.getFloat("bgMusicVolume", 1f);
    float sfxVolume = prefs.getFloat("sfxVolume", 1f);
    float ambienceVolume = prefs.getFloat("ambienceVolume", 1f);
    boolean isReadAloudEnabled = prefs.getBoolean("isReadAloudEnabled", false);
    boolean isSkipDialogueEnabled = prefs.getBoolean("isSkipDialogueEnabled", false);

    try {
      gameSettings = new GameSettings(bgMusicVolume, sfxVolume, ambienceVolume, isReadAloudEnabled, isSkipDialogueEnabled);
    } catch (IllegalArgumentException e) {
      Gdx.app.error("SettingsConfiguration", "Invalid settings found. Reverting to safe defaults.", e);
      gameSettings = new GameSettings(1f, 1f, 1f, false, false);
      saveSettings(); // Overwrite corrupted data safely
    }
  }

  /**
   * Persists the current configuration to local storage.
   * MUST be called whenever any setting is updated.
   */
  private void saveSettings() {
    Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
    prefs.putFloat("bgMusicVolume", gameSettings.bgMusicVolume());
    prefs.putFloat("sfxVolume", gameSettings.sfxVolume());
    prefs.putFloat("ambienceVolume", gameSettings.ambienceVolume());
    prefs.putBoolean("isReadAloudEnabled", gameSettings.isReadAloudEnabled());
    prefs.putBoolean("isSkipDialogueEnabled", gameSettings.isSkipDialogueEnabled());
    
    // Explicitly flush to make sure changes are written to disk
    prefs.flush();
  }

  public void updateBgMusicVol(float newVolume) {
    gameSettings = new GameSettings(
      newVolume, 
      gameSettings.sfxVolume(), 
      gameSettings.ambienceVolume(), 
      gameSettings.isReadAloudEnabled(),
      gameSettings.isSkipDialogueEnabled());
    saveSettings();
  }

  public void updateSfxVol(float newVolume) {
    gameSettings = new GameSettings(
      gameSettings.bgMusicVolume(), 
      newVolume, 
      gameSettings.ambienceVolume(), 
      gameSettings.isReadAloudEnabled(), 
      gameSettings.isSkipDialogueEnabled());
    saveSettings();
  } 

  public void updateAmbienceVol(float newVolume) {
    gameSettings = new GameSettings(
      gameSettings.bgMusicVolume(), 
      gameSettings.sfxVolume(), 
      newVolume, 
      gameSettings.isReadAloudEnabled(), 
      gameSettings.isSkipDialogueEnabled());
    saveSettings();
  } 

  public void updateReadAloud(boolean isReadAloudEnabled) {
    gameSettings = new GameSettings(
      gameSettings.bgMusicVolume(), 
      gameSettings.sfxVolume(), 
      gameSettings.ambienceVolume(), 
      isReadAloudEnabled, 
      gameSettings.isSkipDialogueEnabled());
    saveSettings();
  } 

  public void updateSkipDialogue(boolean isSkipDialogueEnabled) {
    gameSettings = new GameSettings(
      gameSettings.bgMusicVolume(), 
      gameSettings.sfxVolume(), 
      gameSettings.ambienceVolume(), 
      gameSettings.isReadAloudEnabled(), 
      isSkipDialogueEnabled);
    saveSettings();
  } 

  public GameSettings getGameSettings() {
    return gameSettings;
  }
}
