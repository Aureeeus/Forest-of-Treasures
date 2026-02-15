package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.View.IntroductionGameScreen;
import edu.tip.forestoftreasures.View.MainMenuScreen;
import edu.tip.forestoftreasures.View.SettingsScreen;
import edu.tip.forestoftreasures.View.mazeBossScreen;

public class MainMenuController {
  private final GameLauncher game;
  private final MainMenuScreen screen;
  private Skin skin;

  private Sound startSound;
  private Sound selectSound;

  private float sfxVolume;

  public MainMenuController(GameLauncher game, MainMenuScreen screen) {
    this.game = game;
    this.screen = screen;
    this.skin = game.assets.get("ui/fotskin.json", Skin.class);

    this.startSound = Gdx.audio.newSound(Gdx.files.internal("audio/main_menu_start_sound.wav"));
    this.selectSound = Gdx.audio.newSound(Gdx.files.internal("audio/main_menu_select_sound.wav"));

    sfxVolume = game.settingsConfig.getGameSettings().sfxVolume();

    addListeners();
  }

  private void addListeners() {

    // Quit button listener
    screen.getStartButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        startSound.play(sfxVolume);
        game.setScreen(new IntroductionGameScreen(game));
        screen.dispose();
      }
    });

    screen.getAchievementsButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(sfxVolume);
        System.out.println("Achievements button clicked!");
      }
    });

    screen.getSettingsButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(sfxVolume);
        game.setScreen(new SettingsScreen(game, skin));
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
        game.setScreen(new mazeBossScreen(game));
      }

    });
  }

  private void handleQuit() {
    game.getScreen().dispose();
    Gdx.app.exit();
  }
}
