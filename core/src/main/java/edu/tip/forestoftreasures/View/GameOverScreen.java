package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import edu.tip.forestoftreasures.utils.FontFactory;

public class GameOverScreen implements Screen {
  private Stage stage;
  private Table table;
  private TypingLabel typingLabel;
  private TextraLabel instructionLabel;
  private Font textraFont;

  private final GameLauncher game;

  public GameOverScreen(GameLauncher game) {
    this.game = game;
  }

  @Override
  public void show() {
    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);

    table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    textraFont = FontFactory.generateFont("fonts/DotGothic16-Regular.ttf", 24, Color.WHITE);

    String dialogue = "{COLOR=#FF0000}YOU DIED{CLEARCOLOR}\nYou journey ends here, The forest claims another soul once again.";
    typingLabel = new TypingLabel(dialogue, textraFont);
    typingLabel.setWrap(true);

    String instructions = "[#FFD700]{FADE}PRESS SPACE TO RETURN TO MENU{ENDFADE}[%]";
    instructionLabel = new TextraLabel(instructions, textraFont);
    instructionLabel.setWrap(true);
    instructionLabel.setVisible(false);

    typingLabel.setTypingListener(new TypingAdapter() {
      @Override
      public void end() {
        instructionLabel.setVisible(true);
        instructionLabel.addAction(Actions.forever(Actions.sequence(
            Actions.fadeOut(.5f),
            Actions.fadeIn(.5f))));
      }
    });

    table.add(typingLabel)
        .width(Gdx.graphics.getWidth() / 2f)
        .pad(20f)
        .row();
    table.add(instructionLabel)
        .width(Gdx.graphics.getWidth() / 2f)
        .pad(20f);
  }

  @Override
  public void render(float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
      game.setScreen(new MainMenuScreen(game));
      return;
    }

    ScreenUtils.clear(Color.valueOf("#121315"));
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    if (width <= 0 || height <= 0)
      return;
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void pause() {
  }

  @Override
  public void resume() {
  }

  @Override
  public void hide() {
    Gdx.input.setInputProcessor(null);
  }

  @Override
  public void dispose() {
    if (stage != null)
      stage.dispose();
  }
}
