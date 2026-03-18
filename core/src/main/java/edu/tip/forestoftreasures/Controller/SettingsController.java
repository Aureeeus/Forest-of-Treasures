package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.View.MainMenuScreen;
import edu.tip.forestoftreasures.View.SettingsScreen;

public class SettingsController {
  private final GameLauncher game;
  private final SettingsScreen screen;

  private Sound selectSound;

  public SettingsController(GameLauncher game, SettingsScreen screen) {
    this.game = game;
    this.screen = screen;

    this.selectSound = game.assets.get("audio/sfx/main_menu_select_sound.wav", Sound.class);

    addListeners();
  }

  private void addListeners() {
    // Back button listener
    screen.getBackButton().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        selectSound.play(1f);
        handleBack();
      }
    });

    // Music Volume slider listener
    screen.getMusicVolumeSlider().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float musicVolume = screen.getMusicVolumeSlider().getValue();
        System.out.println("Music Volume: " + musicVolume);
        // TODO: Apply music volume to all background music in the game
      }
    });

    // SFX Volume slider listener
    screen.getSfxVolumeSlider().addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float sfxVolume = screen.getSfxVolumeSlider().getValue();
        System.out.println("SFX Volume: " + sfxVolume);
        // TODO: Apply SFX volume to all sound effects in the game
      }
    });
  }

  private void handleBack() {
    screen.dispose();
    game.setScreen(new MainMenuScreen(game));
  }
}
