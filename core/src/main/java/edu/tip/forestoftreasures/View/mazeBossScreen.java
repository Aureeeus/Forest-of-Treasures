package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.mazeBossController;
import edu.tip.forestoftreasures.View.GameOverScreen;

public class mazeBossScreen implements Screen {
  private Stage stage;
  private Table table;
  private final GameLauncher game;
  private mazeBossController controller;
  private SpriteBatch batch;
  private Texture playerTexture;
  private Texture treantTexture;

  private TiledMap mazeMap;
  private OrthogonalTiledMapRenderer mapRenderer;
  private OrthographicCamera camera;
  private ShapeRenderer shapeRenderer;

  private Music mazeMusic;
  private Sound enterSound;
  private boolean hasPlayedEnter = false;

  private final Runnable onComplete;

  public mazeBossScreen(GameLauncher game, Runnable onComplete) {
    this.game = game;
    this.onComplete = onComplete;
    this.controller = new mazeBossController();
  }

  @Override
  public void show() {
    // Prepare your screen here.
    stage = new Stage(new FitViewport(1920, 1080));

    enterSound = game.assets.get("audio/sfx/maze_enter.mp3", Sound.class);

    batch = new SpriteBatch();
    playerTexture = new Texture(Gdx.files.internal("images/Diamond-Player.png"));
    treantTexture = new Texture(Gdx.files.internal("images/Diamond-Player.png"));
    shapeRenderer = new ShapeRenderer();

    mazeMap = new TmxMapLoader().load("Maps/testmap.tmx");
    mapRenderer = new OrthogonalTiledMapRenderer(mazeMap, 0.6f);

    camera = new OrthographicCamera();
    camera.setToOrtho(false, 1920, 1080);

    camera.position.set(576, 540, 0);
    camera.update();

    Gdx.input.setInputProcessor(stage);

  }

  @Override
  public void render(float delta) {
    controller.movement(delta, mazeMap);

    // If the player was killed by the enemy, go to Game Over
    if (controller.playerDead) {
      game.setScreen(new GameOverScreen(game));
      return;
    }

    // Check exit BEFORE rendering — no point drawing if we're switching screens
    if (controller.isOnExit(mazeMap)) {
      onComplete.run();
      return;
    }

    ScreenUtils.clear(Color.BLACK);
    camera.update();

    // Draw the White Combat Box (The Border)
    // We do this BEFORE the batch to keep layers clean
    Gdx.gl.glLineWidth(4); // Set border thickness
    shapeRenderer.setProjectionMatrix(camera.combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(Color.WHITE);

    // Based on your 30x20 map (1920x1280 pixels)
    // If you want it to be a box at the bottom:
    shapeRenderer.rect(0, 0, 1152, 768);
    shapeRenderer.end();

    // Draw the Maze
    mapRenderer.setView(camera);
    mapRenderer.render();

    // Draw the Player
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    // Player
    batch.draw(playerTexture, controller.playerPosition.x, controller.playerPosition.y, 48, 48);

    // Enemy (tinted red)
    if (controller.enemyActive) {
      batch.setColor(Color.RED);
      batch.draw(playerTexture, controller.enemyPosition.x, controller.enemyPosition.y, 48, 48);
      batch.setColor(Color.WHITE);
    }

    // Treant placeholder above maze
    batch.draw(treantTexture, 512 - 64, 820, 128, 128);
    batch.end();

    // Draw UI/Stage (Buttons like FIGHT, ACT, etc.)
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
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
    stage.dispose();
    batch.dispose();
    playerTexture.dispose();
    treantTexture.dispose();
    mazeMap.dispose();
    mapRenderer.dispose();
    shapeRenderer.dispose();
  }
}
