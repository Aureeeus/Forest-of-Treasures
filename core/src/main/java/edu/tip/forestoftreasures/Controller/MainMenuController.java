package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.View.IntroductionGameScreen;
import edu.tip.forestoftreasures.View.MainMenuScreen;
import edu.tip.forestoftreasures.View.SettingsScreen;

public class MainMenuController {
  private final GameLauncher game;
  private final MainMenuScreen screen;
  private Skin skin;

  private Sound startSound;
  private Sound selectSound;

  public MainMenuController(GameLauncher game, MainMenuScreen screen) {
    this.game = game;
    this.screen = screen;
    this.skin = game.assets.get("ui/fotskin.json", Skin.class);

    this.startSound = Gdx.audio.newSound(Gdx.files.internal("audio/main_menu_start_sound.wav"));
    this.selectSound = Gdx.audio.newSound(Gdx.files.internal("audio/main_menu_select_sound.wav"));

    addListeners();
  }

  private void addListeners() {

    // Quit button listener
    screen.getStartButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        startSound.play(1f);
        game.setScreen(new IntroductionGameScreen(game));
        screen.dispose();
      }
    });

    screen.getAchievementsButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(1f);
        System.out.println("Achievements button clicked!");
      }
    });

    screen.getSettingsButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(1f);
        game.setScreen(new SettingsScreen(game, skin));
      }
    });

    screen.getQuitButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(1f);
        handleQuit();
      }
    });
  }

  private void handleQuit() {
    game.getScreen().dispose();
    Gdx.app.exit();
  }
}
