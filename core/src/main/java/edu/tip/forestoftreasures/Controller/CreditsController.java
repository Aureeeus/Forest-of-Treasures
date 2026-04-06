package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.View.CreditsScreen;

/**
 * Controller for the credits screen.
 * Handles the exit button and auto-returns to the main menu
 * once the credits have fully scrolled off the top of the screen.
 */
public class CreditsController {

    private final GameLauncher game;
    private final CreditsScreen screen;

    private final Sound selectSound;

    public CreditsController(GameLauncher game, CreditsScreen screen) {
        this.game = game;
        this.screen = screen;
        this.selectSound = game.assets.get("audio/sfx/select_sound.wav", Sound.class);

        addListeners();
    }

    /**
     * Binds the exit button so it returns the player to the main menu.
     */
    private void addListeners() {
        screen.getExitButton().addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float sfxVolume = game.settingsConfig.getGameSettings().sfxVolume();
                selectSound.play(sfxVolume);
                game.setScreen(game.getMainMenuScreen());
            }
        });
    }
}
