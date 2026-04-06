package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.View.AchievementsScreen;
import edu.tip.forestoftreasures.View.CreditsScreen;
import edu.tip.forestoftreasures.View.IntroductionGameScreen;
import edu.tip.forestoftreasures.View.MainMenuScreen;
import edu.tip.forestoftreasures.View.mazeBossScreen;

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
        game.setScreen(new AchievementsScreen(game));
      }
    });

    screen.getCreditsButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(sfxVolume);
        screen.stopMusic();
        game.setScreen(new CreditsScreen(game));
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

    screen.getTestButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(sfxVolume);
        game.setScreen(new mazeBossScreen(game, null));
      }
    });
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
