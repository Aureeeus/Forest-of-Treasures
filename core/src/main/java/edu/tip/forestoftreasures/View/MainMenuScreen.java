package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.MainMenuController;

/**
 * First screen of the application. Displayed after the application is created.
 */
public class MainMenuScreen implements Screen {
  private Stage stage;
  private Table table;
  private final GameLauncher game;
  private MainMenuController controller;

  // Game designs and sounds
  private Skin skin;
  private Texture backgroundTexture;
  private Music backgroundMusic;

  // Particle effect rendering
  private ParticleEffect leafEffect;
  private SpriteBatch batch;
  
  private TextButton startButton;
  private TextButton achievementsButton;
  private TextButton creditsButton;
  private TextButton settingsButton;
  private TextButton quitButton;
  private TextButton testButton;

  private float musicVolume;

  public MainMenuScreen(GameLauncher game) {
    this.game = game;
    initialize();
  }

  private void initialize() {
    skin = game.assets.get("ui/fotskin.json", Skin.class);
    musicVolume = game.settingsConfig.getGameSettings().bgMusicVolume();

    stage = new Stage(new ScreenViewport());

    // Set background music
    backgroundMusic = game.assets.get("audio/bgm/main_menu_bg_music.mp3", Music.class);
    backgroundMusic.setLooping(true);
    backgroundMusic.setVolume(musicVolume);

    backgroundTexture = game.assets.get("images/backgrounds/main_menu_bg.png", Texture.class);

    table = new Table();
    table.defaults().size(380, 60).spaceBottom(20f);
    table.setFillParent(true);
    stage.addActor(table);

    // Adding UI elements to the table
    startButton = new TextButton("START GAME", skin, "main-menu-text-button");
    table.add(startButton);
    table.row();

    achievementsButton = new TextButton("ACHIEVEMENTS", skin, "main-menu-text-button");
    table.add(achievementsButton);
    table.row();

    creditsButton = new TextButton("CREDITS", skin, "main-menu-text-button");
    table.add(creditsButton);
    table.row();

    settingsButton = new TextButton("SETTINGS", skin, "main-menu-text-button");
    table.add(settingsButton);
    table.row();

    quitButton = new TextButton("QUIT", skin, "main-menu-text-button");
    table.add(quitButton);
    table.row();

    testButton = new TextButton("maze testing", skin, "main-menu-text-button");
    table.add(testButton);
    table.row();

    // Copy the managed particle effect so the AssetManager original stays reusable
    leafEffect = new ParticleEffect(game.assets.get("particles/autumn_leaf.p", ParticleEffect.class));
    leafEffect.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight());
    leafEffect.start();

    batch = new SpriteBatch();

    controller = new MainMenuController(game, this);
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor(stage);
    
    // Synchronize settings upon returning to this screen
    musicVolume = game.settingsConfig.getGameSettings().bgMusicVolume();
    if (backgroundMusic != null) {
      backgroundMusic.setVolume(musicVolume);
    }
    if (controller != null) {
      controller.syncSettings();
    }

    if (!backgroundMusic.isPlaying()) {
      backgroundMusic.play();
    }
  }

  @Override
  public void render(float delta) {
    ScreenUtils.clear(Color.BLACK);

    stage.act(delta);

    // Layer order: background → particles → UI
    batch.begin();
    batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    leafEffect.update(delta);
    leafEffect.draw(batch);
    batch.end();

    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    // If the window is minimized on a desktop (LWJGL3) platform, width and height
    // are 0, which causes problems.
    // In that case, we don't resize anything, and wait for the window to be a
    // normal size before updating.
    if (width <= 0 || height <= 0)
      return;

    stage.getViewport().update(width, height, true);
    leafEffect.setPosition(width / 2f, height);
  }

  @Override
  public void pause() {
    // Invoked when your application is paused.
    if (backgroundMusic != null && backgroundMusic.isPlaying()) {
      backgroundMusic.pause();
    }
  }

  @Override
  public void resume() {
    // Invoked when your application is resumed after pause.
    if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
      backgroundMusic.play();
    }
  }

  @Override
  public void hide() {
    // This method is called when another screen replaces this one.
    Gdx.input.setInputProcessor(null);
    if (backgroundMusic != null) {
      backgroundMusic.stop(); // Ensured to restart from the beginning next time
    }
  }

  @Override
  public void dispose() {
    stage.dispose();
    batch.dispose();
    controller.dispose();
    if (backgroundMusic != null) {
      backgroundMusic.stop();
      // Note: We do not call backgroundMusic.dispose() here because it is managed 
      // by the AssetManager. Manually disposing it would break future lookups.
      backgroundMusic = null;
    }
  }

  public TextButton getStartButton() {
    return startButton;
  }

  public TextButton getAchievementsButton() {
    return achievementsButton;
  }

  public TextButton getCreditsButton() {
    return creditsButton;
  }

  public TextButton getSettingsButton() {
    return settingsButton;
  }

  public TextButton getQuitButton() {
    return quitButton;
  }

  public TextButton getTestButton() {
    return testButton;
  }

  /**
   * Stops the background music if it is currently playing.
   * Can be called by the controller before transitioning to another screen.
   */
  public void stopMusic() {
    if (backgroundMusic != null && backgroundMusic.isPlaying()) {
      backgroundMusic.stop();
    }
  }

  /**
   * Returns the background music.
   * Can be called by the controller to change the music volume.
   */
  public Music getBackgroundMusic() {
    return backgroundMusic;
  }
}