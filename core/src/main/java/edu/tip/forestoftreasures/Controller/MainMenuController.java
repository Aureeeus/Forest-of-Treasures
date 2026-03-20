package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Model.entities.Player;
import edu.tip.forestoftreasures.View.EntityBattleScreen;
import edu.tip.forestoftreasures.View.IntroductionGameScreen;
import edu.tip.forestoftreasures.View.MainMenuScreen;
import edu.tip.forestoftreasures.View.SettingsScreen;
import edu.tip.forestoftreasures.View.mazeBossScreen;

public class MainMenuController {
  private final GameLauncher game;
  private final MainMenuScreen screen;
  private Skin skin;

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
    this.skin = game.assets.get("ui/fotskin.json", Skin.class);

    this.startSound = game.assets.get("audio/sfx/main_menu_start_sound.wav", Sound.class);
    this.selectSound = game.assets.get("audio/sfx/main_menu_select_sound.wav", Sound.class);

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
        screen.dispose();
      }
    });

    screen.getAchievementsButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(sfxVolume);
        game.setScreen(
            new EntityBattleScreen(game, "bandit_battle_minigame", new Player(48f, 10f, 0f, 15f, 12f, 12f), null));
        screen.dispose();
      }
    });

    screen.getSettingsButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(sfxVolume);
        game.setScreen(new SettingsScreen(game, skin));
        screen.dispose();
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
        screen.dispose();
      }
    });
  }

  /**
   * Safely disposes the current screen and exits the application.
   */
  private void handleQuit() {
    game.getScreen().dispose();
    Gdx.app.exit();
  }

  /**
   * Frees up memory allocated by native audio resources when the controller
   * is no longer needed. Must be called by the screen's dispose method.
   */
  public void dispose() {
    // Audio resources are managed by AssetManager
  }
}
