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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.MazeBossController;

public class MazeBossScreen implements Screen {
  private Stage stage;
  private Table table;
  private final GameLauncher game;
  private MazeBossController controller;
  private SpriteBatch batch;
  private Texture playerTexture;

  private TiledMap mazeMap;
  private OrthogonalTiledMapRenderer mapRenderer;
  private OrthographicCamera camera;
  private ShapeRenderer shapeRenderer;

  private Music mazeMusic;
  private Sound enterSound;
  private boolean hasPlayedEnter = false;

  private final Runnable onComplete;

  public MazeBossScreen(GameLauncher game, Runnable onComplete) {
    this.game = game;
    this.onComplete = onComplete;
    this.controller = new MazeBossController();
  }

  @Override
  public void show() {
    // Prepare your screen here.
    stage = new Stage(new FitViewport(1920, 1080));

    enterSound = game.assets.get("audio/sfx/maze_enter.mp3", Sound.class);
    // mazeMusic = game.assets.get("audio/maze_music.mp3", Music.class);
    // mazeMusic.setLooping(true);
    // mazeMusic.setVolume(0.5f); // Adjust volume as needed

    batch = new SpriteBatch();
    playerTexture = new Texture(Gdx.files.internal("images/Diamond-Player.png"));
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
    batch.draw(playerTexture, controller.playerPosition.x, controller.playerPosition.y, 48, 48);
    batch.end();

    // Draw UI/Stage (Buttons like FIGHT, ACT, etc.)
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    // Handle screen resizing here.
    // If the window is minimized on a desktop (LWJGL3) platform, width and height
    // are 0, which causes problems.
    // In that case, we don't resize anything, and wait for the window to be a
    // normal size before updating.
    stage.getViewport().update(width, height, true);

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
    stage.dispose();
    batch.dispose();
    playerTexture.dispose();
    mazeMap.dispose();
    mapRenderer.dispose();
    shapeRenderer.dispose();
  }
}
