package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.tommyettinger.textra.TypingLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.View.DayScreen;
// import edu.tip.forestoftreasures.View.DayScreen;
import edu.tip.forestoftreasures.View.IntroductionGameScreen;

public class IntroductionGameController {
  private final GameLauncher game;
  private final Stage stage;
  private final IntroductionGameScreen screen;
  private final TypingLabel typingLabel;

  /**
   * Initializes the introduction sequence controller and binds
   * interactive listeners to the provided stage and typing label.
   *
   * @param game   The main game instance.
   * @param screen The introduction screen containing the UI stage.
   */
  public IntroductionGameController(GameLauncher game, IntroductionGameScreen screen) {
    this.game = game;
    this.screen = screen;
    this.stage = screen.getStage();
    this.typingLabel = screen.getTypingLabel();

    addListeners();
  }

  /**
   * Registers input listeners to the stage.
   * - Clicking (touchDown) or pressing SPACE skips the typing animation if it's playing.
   * - Pressing SPACE when the animation has already finished transitions the state to Day 1.
   */
  private void addListeners() {
    stage.addListener(new InputListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (!typingLabel.hasEnded()) {
          typingLabel.skipToTheEnd();
        }

        return true;
      }

      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Input.Keys.SPACE) {
          if (!typingLabel.hasEnded()) {
            typingLabel.skipToTheEnd();
          } else {
            game.setScreen(new DayScreen(game));
            screen.dispose();
          }
          return true;
        }

        return false;
      }
    });
  }
}
