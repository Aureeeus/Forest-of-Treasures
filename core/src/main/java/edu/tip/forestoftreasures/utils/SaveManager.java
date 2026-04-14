package edu.tip.forestoftreasures.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import edu.tip.forestoftreasures.Model.SaveData;

/**
 * Coordinates saving and loading game data through the encryption layer
 * and libGDX file I/O. Uses a single save slot stored at a cross-platform
 * local path managed by libGDX.
 */
public final class SaveManager {
  private static final String TAG = "SaveManager";
  private static final String SAVE_PATH = "../saves/game_save.dat";
  private static final String ACHIEVEMENTS_PATH = "../saves/achievements.dat";

  private static final Json json = new Json();

  private SaveManager() {
    throw new InstantiationError("Utility class cannot be instantiated.");
  }

  /**
   * Serializes and encrypts the given save data, writing it to disk.
   * Pipeline: SaveData → JSON → AES-256 encrypt → Base64 → file.
   *
   * @param data The game state to persist.
   * @return true if save succeeded, false on error.
   */
  public static boolean save(SaveData data) {
    try {
      String jsonString = json.toJson(data);
      String encrypted = SaveCrypto.encrypt(jsonString);

      FileHandle file = Gdx.files.local(SAVE_PATH);
      file.writeString(encrypted, false);

      Gdx.app.log(TAG, "Game saved successfully.");
      return true;
    } catch (Exception e) {
      Gdx.app.error(TAG, "Failed to save game.", e);
      return false;
    }
  }

  /**
   * Reads, decrypts, and deserializes the save file.
   * Pipeline: file → Base64 → decrypt → JSON → SaveData.
   *
   * @return The loaded SaveData, or null if loading fails.
   * @throws SaveCrypto.SaveTamperedException if the file has been modified.
   */
  public static SaveData load() {
    try {
      FileHandle file = Gdx.files.local(SAVE_PATH);
      if (!file.exists()) {
        Gdx.app.log(TAG, "No save file found.");
        return null;
      }

      String encrypted = file.readString();
      String jsonString = SaveCrypto.decrypt(encrypted);
      SaveData data = json.fromJson(SaveData.class, jsonString);

      Gdx.app.log(TAG, "Game loaded successfully.");
      return data;
    } catch (SaveCrypto.SaveTamperedException e) {
      Gdx.app.error(TAG, "Save file tampered with!", e);
      throw e;
    } catch (Exception e) {
      Gdx.app.error(TAG, "Failed to load game.", e);
      return null;
    }
  }

  /**
   * Checks whether a valid save file exists on disk.
   *
   * @return true if a save file is present.
   */
  public static boolean hasSaveFile() {
    return Gdx.files.local(SAVE_PATH).exists();
  }

  /**
   * Serializes and encrypts the set of unlocked achievements to its own file.
   *
   * @param unlockedAchievements The set of achievement IDs to persist globally.
   * @return true if save succeeded.
   */
  public static boolean saveAchievements(java.util.Set<String> unlockedAchievements) {
    try {
      String jsonString = json.toJson(unlockedAchievements, java.util.HashSet.class, String.class);
      String encrypted = SaveCrypto.encrypt(jsonString);

      FileHandle file = Gdx.files.local(ACHIEVEMENTS_PATH);
      file.writeString(encrypted, false);

      Gdx.app.log(TAG, "Achievements saved successfully.");
      return true;
    } catch (Exception e) {
      Gdx.app.error(TAG, "Failed to save achievements.", e);
      return false;
    }
  }

  /**
   * Loads and decrypts the global achievements file.
   *
   * @return The set of unlocked achievement IDs, or an empty set if not found.
   */
  @SuppressWarnings("unchecked")
  public static java.util.Set<String> loadAchievements() {
    try {
      FileHandle file = Gdx.files.local(ACHIEVEMENTS_PATH);
      if (!file.exists()) {
        Gdx.app.log(TAG, "No achievements file found.");
        return new java.util.HashSet<>();
      }

      String encrypted = file.readString();
      String jsonString = SaveCrypto.decrypt(encrypted);
      java.util.HashSet<String> data = json.fromJson(java.util.HashSet.class, String.class, jsonString);

      Gdx.app.log(TAG, "Achievements loaded successfully.");
      return data != null ? data : new java.util.HashSet<>();
    } catch (SaveCrypto.SaveTamperedException e) {
      Gdx.app.error(TAG, "Achievements file tampered with!", e);
      return new java.util.HashSet<>();
    } catch (Exception e) {
      Gdx.app.error(TAG, "Failed to load achievements.", e);
      return new java.util.HashSet<>();
    }
  }

  /**
   * Checks whether a global achievements file exists.
   */
  public static boolean hasAchievementsFile() {
    return Gdx.files.local(ACHIEVEMENTS_PATH).exists();
  }

  /**
   * Deletes the save file from disk.
   */
  public static void deleteSave() {
    FileHandle file = Gdx.files.local(SAVE_PATH);
    if (file.exists()) {
      file.delete();
      Gdx.app.log(TAG, "Save file deleted.");
    }
  }
}
