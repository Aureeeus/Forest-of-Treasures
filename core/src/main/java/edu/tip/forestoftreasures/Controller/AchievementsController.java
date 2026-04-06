package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.View.AchievementsScreen;

/**
 * Controller for the achievements screen.
 * Handles the exit button and navigates back to the main menu.
 */
public class AchievementsController {

    private final GameLauncher game;
    private final AchievementsScreen screen;

    private final Sound selectSound;

    public AchievementsController(GameLauncher game, AchievementsScreen screen) {
        this.game = game;
        this.screen = screen;
        this.selectSound = game.assets.get("audio/sfx/select_sound.wav", Sound.class);

        addListeners();
    }

    /**
     * Binds the exit button to navigate back to the main menu.
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
