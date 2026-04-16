package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.View.SettingsScreen;

/** Controller for the settings screen. Handles user input and updates game settings. */
public class SettingsController {
  private final GameLauncher game;
  private final SettingsScreen screen;

  // UI Sound effects
  private final Sound selectSound;

  public SettingsController(GameLauncher game, SettingsScreen screen) {
    this.game = game;
    this.screen = screen;
    this.selectSound = game.assets.get("audio/sfx/select_sound.wav", Sound.class);

    addListeners();
  }

  /**
   * Registers input listeners for all buttons on the settings screen.
   */
  private void addListeners() {
    // Exit button listener
    screen.getExitButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float sfxVolume = game.settingsConfig.getGameSettings().sfxVolume();
        selectSound.play(sfxVolume);
        game.setScreen(game.getMainMenuScreen());
      }
    });

    // Background music volume slider listener
    Slider bgMusicVolumeSlider = screen.getBgMusicVolumeSlider();
    bgMusicVolumeSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float volume = bgMusicVolumeSlider.getValue();
        game.settingsConfig.updateBgMusicVol(volume);

        Music bgMusic = screen.getBgMusic();
        if (bgMusic != null) {
          bgMusic.setVolume(volume);
        }
      }
    });

    // Sound effects volume slider listener
    Slider sfxVolumeSlider = screen.getSfxVolumeSlider();
    sfxVolumeSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float volume = sfxVolumeSlider.getValue();
        game.settingsConfig.updateSfxVol(volume);
      }
    });

    // Ambience volume slider listener
    Slider ambienceVolumeSlider = screen.getAmbienceVolumeSlider();
    ambienceVolumeSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float volume = ambienceVolumeSlider.getValue();
        game.settingsConfig.updateAmbienceVol(volume);
        game.speechManager.updateVolume();
      }
    });

    // Read aloud toggle button listener
    Button readAloudButton = screen.getReadAloudButton();
    readAloudButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        boolean isChecked = readAloudButton.isChecked();
        game.settingsConfig.updateReadAloud(isChecked);
        
        // If the user disables read aloud, stop any ongoing narration immediately
        if (!isChecked) {
          game.speechManager.stop();
        }
        
        syncMutuallyExclusiveSettings();
      }
    });

    // Skip dialogue toggle button listener
    Button skipDialogueButton = screen.getSkipDialogueButton();
    skipDialogueButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        boolean isChecked = skipDialogueButton.isChecked();
        game.settingsConfig.updateSkipDialogue(isChecked);
        
        syncMutuallyExclusiveSettings();
      }
    });

    // Initialize synchronization on startup
    syncMutuallyExclusiveSettings();
  }

  /**
   * Enforces mutual exclusivity between "Read Aloud" and "Skip Dialogue".
   * Enabling one will force the other to false and disable its interaction.
   */
  private void syncMutuallyExclusiveSettings() {
    Button readAloud = screen.getReadAloudButton();
    Button skipDialogue = screen.getSkipDialogueButton();

    // If Read Aloud is enabled, Skip Dialogue must be disabled and Off
    if (readAloud.isChecked()) {
      if (skipDialogue.isChecked()) {
        skipDialogue.setChecked(false);
        game.settingsConfig.updateSkipDialogue(false);
      }
      skipDialogue.setDisabled(true);
    } else {
      skipDialogue.setDisabled(false);
    }

    // If Skip Dialogue is enabled, Read Aloud must be disabled and Off
    if (skipDialogue.isChecked()) {
      if (readAloud.isChecked()) {
        readAloud.setChecked(false);
        game.settingsConfig.updateReadAloud(false);
        game.speechManager.stop();
      }
      readAloud.setDisabled(true);
    } else {
      readAloud.setDisabled(false);
    }
  }
}
