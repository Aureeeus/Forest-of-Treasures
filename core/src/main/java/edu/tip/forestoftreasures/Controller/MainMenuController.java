package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Model.SaveData;
import edu.tip.forestoftreasures.View.DayScreen;
import edu.tip.forestoftreasures.View.IntroductionGameScreen;
import edu.tip.forestoftreasures.View.MainMenuScreen;
import edu.tip.forestoftreasures.utils.SaveCrypto;
import edu.tip.forestoftreasures.utils.SaveManager;


public class MainMenuController {
  private final GameLauncher game;
  private final MainMenuScreen screen;

  // Game sounds (disposable)
  private Sound startSound;
  private Sound selectSound;

  private float sfxVolume;

  /**
   * Initializes the main menu controller, setting up necessary audio assets
   * and fetching system settings before binding interactive listeners to the UI.
   *
   * @param game   The main game launcher holding the asset manager and settings.
   * @param screen The main menu screen containing the UI elements.
   */
  public MainMenuController(GameLauncher game, MainMenuScreen screen) {
    this.game = game;
    this.screen = screen;

    this.startSound = game.assets.get("audio/sfx/main_menu_start_sound.wav", Sound.class);
    this.selectSound = game.assets.get("audio/sfx/select_sound.wav", Sound.class);

    sfxVolume = game.settingsConfig.getGameSettings().sfxVolume();

    addListeners();
  }

  /**
   * Registers input listeners for all buttons on the main menu.
   * Handles transitioning between different screens (Intro, Battle, Settings,
   * Maze)
   * and plays interactive sound effects upon pressing.
   */
  private void addListeners() {

    // Quit button listener
    screen.getStartButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        startSound.play(sfxVolume);
        screen.stopMusic();
        game.setScreen(new IntroductionGameScreen(game));
      }
    });

    screen.getAchievementsButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(sfxVolume);
        game.setScreen(game.getAchievementsScreen());
      }
    });

    screen.getSettingsButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(sfxVolume);
        game.setScreen(game.getSettingsScreen());
      }
    });

    screen.getQuitButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(sfxVolume);
        handleQuit();
      }
    });

    screen.getContinueButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (screen.getContinueButton().isDisabled()) return;
        startSound.play(sfxVolume);
        handleContinue();
      }
    });
  }

  /**
   * Loads the saved game data and transitions to the DayScreen,
   * restoring player stats from the save file. The DayController
   * will detect the pending save data and resume from the saved node.
   */
  private void handleContinue() {
    try {
      SaveData saveData = SaveManager.load();
      if (saveData == null) {
        Gdx.app.error("MainMenuController", "No save data found.");
        return;
      }

      // Restore player stats from save data
      game.getPlayer().resetStats(
        saveData.getHp(),
        saveData.getStrength(),
        saveData.getIntelligence(),
        saveData.getDexterity(),
        saveData.getCharisma()
      );

      screen.stopMusic();

      // Pass save data to DayScreen so DayController can resume from saved node
      DayScreen dayScreen = new DayScreen(game);
      dayScreen.setPendingSaveData(saveData);
      game.setScreen(dayScreen);
    } catch (SaveCrypto.SaveTamperedException e) {
      Gdx.app.error("MainMenuController", "Save file has been tampered with!", e);
      SaveManager.deleteSave();
      screen.refreshContinueButton();
    }
  }

  /**
   * Exits the application. All resource cleanup is handled
   * centrally by GameLauncher.dispose() which Gdx.app.exit() triggers.
   */
  private void handleQuit() {
    Gdx.app.exit();
  }

  /**
   * Frees up memory allocated by native audio resources when the controller
   * is no longer needed. Must be called by the screen's dispose method.
   */
  public void dispose() {
    // Audio resources are managed by AssetManager
  }

  /**
   * Synchronizes internal volume caches with the global game settings.
   */
  public void syncSettings() {
    sfxVolume = game.settingsConfig.getGameSettings().sfxVolume();
  }
}
