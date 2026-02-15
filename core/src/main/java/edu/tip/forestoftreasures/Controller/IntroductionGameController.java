package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.tommyettinger.textra.TypingLabel;

import edu.tip.forestoftreasures.GameLauncher;


public class IntroductionGameController {
  private final GameLauncher game;
  private final Stage stage;
  private final TypingLabel typingLabel;

  public IntroductionGameController(
    GameLauncher game,  
    Stage stage,
    TypingLabel typingLabel) {
    this.game = game; 
    this.stage = stage;
    this.typingLabel = typingLabel;

    addListeners();
  }

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
            System.out.println("Space key pressed, transitioning to the next screen...");
          }
          return true;
        }

        return false;
      }
    });
  }
}
