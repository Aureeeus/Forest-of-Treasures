package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.mazeBossController;

public class mazeBossScreen implements Screen {
  private Stage stage;
  private Table table;
  private final GameLauncher game;
  private mazeBossController controller;
  private SpriteBatch batch;
  private Texture playerTexture;

  private TiledMap mazeMap;
  private OrthogonalTiledMapRenderer mapRenderer;
  private OrthographicCamera camera;

  public mazeBossScreen(GameLauncher game) {
    this.game = game;
    this.controller = new edu.tip.forestoftreasures.Controller.mazeBossController();
  }

  @Override
  public void show() {
    // Prepare your screen here.
    stage = new Stage(new ScreenViewport());

    batch = new SpriteBatch();
    playerTexture = new Texture(Gdx.files.internal("images/Diamond-Player.png"));

    controller = new mazeBossController();

    mazeMap = new TmxMapLoader().load("Maps/testmap.tmx");
    mapRenderer = new OrthogonalTiledMapRenderer(mazeMap);

    int mapWidth = mazeMap.getProperties().get("width", Integer.class)
        * mazeMap.getProperties().get("tilewidth", Integer.class);
    int mapHeight = mazeMap.getProperties().get("height", Integer.class)
        * mazeMap.getProperties().get("tileheight", Integer.class);
    camera = new OrthographicCamera();
    camera.setToOrtho(false, mapWidth, mapHeight);

    camera.position.set(mapWidth / 2f, mapHeight / 2f, 0);
    camera.update();

    Gdx.input.setInputProcessor(stage);

    // Add actor to stage
    table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    table.setDebug(true);

    float mapW = mazeMap.getProperties().get("width", Integer.class);
    float mapH = mazeMap.getProperties().get("height", Integer.class);
    float tileW = mazeMap.getProperties().get("tilewidth", Integer.class);

    System.out.println("Map Size: " + mapW + "x" + mapH + " tiles");
    System.out.println("Tile Size: " + tileW + "px");
  }

  @Override
  public void render(float delta) {
    // Update and draw your screen here.

    controller.movement(delta, mazeMap);

    ScreenUtils.clear(Color.BLACK);

    camera.update();
    mapRenderer.setView(camera);
    mapRenderer.render();

    batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
    batch.begin();
    batch.draw(playerTexture, controller.playerPosition.x, controller.playerPosition.y);
    batch.end();

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
  }
}
