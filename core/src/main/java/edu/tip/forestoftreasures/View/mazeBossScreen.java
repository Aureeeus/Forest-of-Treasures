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
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.mazeBossController;
import edu.tip.forestoftreasures.utils.FontFactory;
import java.util.function.Consumer;
import edu.tip.forestoftreasures.Model.dialogue.MinigameNode;

public class mazeBossScreen implements Screen {
  private Stage stage;
  private Table table;
  private final GameLauncher game;
  private mazeBossController controller;
  private SpriteBatch batch;
  private Texture playerUpTexture;
  private Texture playerDownTexture;
  private Texture playerLeftTexture;
  private Texture playerRightTexture;
  private Texture treantTexture;
  private Texture enemyTexture;
  private Font textraFont;
  private TextraLabel treantLabel;
  private float treantDialogTimer = 0f;
  private final float TREANT_DIALOG_INTERVAL = 8f;

  // Ten separate dialog strings (editable)
  public String treantText1 = " \n I have caught you!";
  public String treantText2 = "\nThere is only one way out of this maze";
  public String treantText3 = "\nVile Tresspaser. This will be your grave!";
  public String treantText4 = "\nWitness the power of the forest!";
  public String treantText5 = "\nThis is the end for you, trespasser!";
  public String treantText6 = "\nSay your prayers, trespasser!";
  public String treantText7 = "\nYou abuse Nature's hospitality";
  public String treantText8 = "\nWe have been kind to you";
  public String treantText9 = "\nYou have no respect for the forest!";
  public String treantText10 = "\nDirty Defiler!";

  private TiledMap mazeMap;
  private OrthogonalTiledMapRenderer mapRenderer;
  private OrthographicCamera camera;
  private ShapeRenderer shapeRenderer;

  private Music mazeMusic;
  private Sound enterSound;
  private boolean hasPlayedEnter = false;

  private final Consumer<Boolean> onComplete;
  private final MinigameNode node;

  public mazeBossScreen(GameLauncher game, MinigameNode node, Consumer<Boolean> onComplete) {
    this.game = game;
    this.node = node;
    this.onComplete = onComplete;
    this.controller = new mazeBossController();
  }

  @Override
  public void show() {
    // Prepare your screen here.
    stage = new Stage(new FitViewport(1920, 1080));

    enterSound = game.assets.get("audio/sfx/maze_enter.mp3", Sound.class);

    batch = new SpriteBatch();
    // directional player textures
    playerUpTexture = new Texture(Gdx.files.internal("images/Player_Up-removebg-preview.png"));
    playerDownTexture = new Texture(Gdx.files.internal("images/player_down-removebg-preview.png"));
    playerLeftTexture = new Texture(Gdx.files.internal("images/Player_Left-removebg-preview.png"));
    playerRightTexture = new Texture(Gdx.files.internal("images/Player_Right-removebg-preview.png"));
    // treant: prefer forest_treant.png; fallback to Diamond-Player.png if missing
    if (Gdx.files.internal("images/forest_treant.png").exists()) {
      treantTexture = new Texture(Gdx.files.internal("images/forest_treant.png"));
    } else {
      treantTexture = new Texture(Gdx.files.internal("images/Diamond-Player.png"));
    }

    enemyTexture = new Texture(Gdx.files.internal("images/Diamond-Player.png"));

    // prepare treant label using the same textra font as IntroductionGameScreen
    textraFont = FontFactory.generateFont("fonts/DotGothic16-Regular.ttf", 24, Color.WHITE);
    treantLabel = new TextraLabel(treantText1, textraFont);
    // position label to the right of the treant image (adjusted after scaling)
    treantLabel.setPosition(500, 860);
    treantLabel.setWrap(true);
    treantLabel.setWidth(300);
    stage.addActor(treantLabel);
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
      if (node != null && node.getFailNext() != null && onComplete != null) {
        onComplete.accept(false); // notify runner of failure
      } else {
        game.setScreen(new GameOverScreen(game));
      }
      return;
    }

    // Check exit BEFORE rendering — no point drawing if we're switching screens
    if (controller.isOnExit(mazeMap)) {
      if (onComplete != null) {
        onComplete.accept(true); // notify runner of success
      } else {
        // Fallback for standalone/test: return to main menu or just close
        game.setScreen(new MainMenuScreen(game));
      }
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
    // choose texture based on facing
    Texture cur = playerDownTexture;
    switch (controller.playerFacing) {
      case 0:
        cur = playerUpTexture;
        break;
      case 1:
        cur = playerRightTexture;
        break;
      case 3:
        cur = playerLeftTexture;
        break;
      default:
        cur = playerDownTexture;
    }
    batch.draw(cur, controller.playerPosition.x, controller.playerPosition.y, 24, 24);

    // Enemy (tinted red)
    if (controller.enemyActive) {
      batch.setColor(Color.RED);
      batch.draw(enemyTexture, controller.enemyPosition.x - 12, controller.enemyPosition.y - 12, 48, 48);
      batch.setColor(Color.WHITE);
    }

    // Treant placeholder above maze (50% larger than previous size, nudged left)
    batch.draw(treantTexture, 420, 790, 327, 327);
    batch.end();

    // Draw UI/Stage (Buttons like FIGHT, ACT, etc.)
    stage.act(delta);
    stage.draw();
    // update treant dialog timer and randomize every interval
    treantDialogTimer += delta;
    if (treantDialogTimer >= TREANT_DIALOG_INTERVAL) {
      treantDialogTimer = 0f;
      int idx = (int) (Math.random() * 10);
      String t;
      switch (idx) {
        default:
        case 0:
          t = treantText1;
          break;
        case 1:
          t = treantText2;
          break;
        case 2:
          t = treantText3;
          break;
        case 3:
          t = treantText4;
          break;
        case 4:
          t = treantText5;
          break;
        case 5:
          t = treantText6;
          break;
        case 6:
          t = treantText7;
          break;
        case 7:
          t = treantText8;
          break;
        case 8:
          t = treantText9;
          break;
        case 9:
          t = treantText10;
          break;
      }
      treantLabel.setText(t);
    }
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
    if (playerUpTexture != null)
      playerUpTexture.dispose();
    if (playerDownTexture != null)
      playerDownTexture.dispose();
    if (playerLeftTexture != null)
      playerLeftTexture.dispose();
    if (playerRightTexture != null)
      playerRightTexture.dispose();
    if (treantTexture != null)
      treantTexture.dispose();
    if (enemyTexture != null)
      enemyTexture.dispose();
    if (treantLabel != null)
      treantLabel.remove();
    mazeMap.dispose();
    mapRenderer.dispose();
    shapeRenderer.dispose();
  }
}
