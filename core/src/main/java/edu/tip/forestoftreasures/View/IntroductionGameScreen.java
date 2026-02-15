package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingAdapter;
import com.github.tommyettinger.textra.TypingLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.IntroductionGameController;

public class IntroductionGameScreen implements Screen {
  private Stage stage;
  private Table table;
  private TypingLabel typingLabel;
  private TextraLabel instructionLabel;

  private final GameLauncher game;

  public IntroductionGameScreen(GameLauncher game) {
    this.game = game;
  }
  
  @Override
  public void show() {
    // Prepare your screen here.
    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);

    table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    Font textraFont = new Font(Gdx.files.internal("fonts/DotGothic16-Medium.fnt"));

    String dialogue = "I have been an unemployed location research mage for some time now until a budding kingdom hired me. I've received a letter asking me to explore this forest 5 kilometers out of their west gate, as they plan to expand their capital. Local rumors have told me that this forest is teeming with life and treasures. I don't know if it was the adventure that enticed me or the treasure, {COLOR=#FFD700}but I digress.{CLEARCOLOR} After preparing 2 weeks' worth of food and materials, I've set out on a journey to this forest. Drakeswood gave me a mana transmitter and a map to help me navigate through this forest.";
    typingLabel = new TypingLabel(dialogue, textraFont);
    typingLabel.setWrap(true);
    

    String instructions = "[#FFD700]{FADE}PRESS SPACE TO BEGIN!{ENDFADE}[%]";
    instructionLabel = new TextraLabel(instructions, textraFont);
    instructionLabel.setWrap(true);
    instructionLabel.setVisible(false);

    // Add listener to show instructions after typing is done
    typingLabel.setTypingListener(new TypingAdapter() {
      @Override
      public void end() {
        instructionLabel.setVisible(true);
        instructionLabel.addAction(Actions.forever(Actions.sequence(
          Actions.fadeOut(.5f), 
          Actions.fadeIn(.5f)
        )));
      }
    });
    
    table.add(typingLabel)
      .width(Gdx.graphics.getWidth() / 2f)
      .pad(20f)
      .row();
    table.add(instructionLabel)
      .width(Gdx.graphics.getWidth() / 2f)
      .pad(20f);

    new IntroductionGameController(game, this);
  }

  @Override
  public void render(float delta) {
    // Update and draw your screen here.
    ScreenUtils.clear(Color.BLACK);

    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
     // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
      // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
      if(width <= 0 || height <= 0) return;

      // Resize your screen here. The parameters represent the new window size.
      stage.getViewport().update(width, height, true);
  }

  @Override
  public void pause() {
    // Handle game pause here.
  }

  @Override
  public void resume() {
    // Handle game resume here.
  }

  @Override
  public void hide() {
    // Handle screen hiding here.
    Gdx.input.setInputProcessor(null);
  }

  @Override
  public void dispose() {
    // Dispose of assets when no longer needed.
    stage.dispose();
  }

  public Stage getStage() {
    return stage;
  }

  public TypingLabel getTypingLabel() {
    return typingLabel;
  }

  public TextraLabel getInstructionLabel() {
    return instructionLabel;
  }

}
