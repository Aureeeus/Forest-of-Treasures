package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import edu.tip.forestoftreasures.GameLauncher;

public class Day1Screen implements Screen {
  private Stage stage;
  private Table table;

  private final GameLauncher game;
  
  public Day1Screen(GameLauncher game) {
    this.game = game;
  }

  @Override
  public void show() {
    // Prepare your screen here.
    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);

    // Add actor to stage
    table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    table.setDebug(true);
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
    // Handle screen resizing here.
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
  }
}
