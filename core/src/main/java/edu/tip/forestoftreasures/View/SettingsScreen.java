package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.SettingsController;

/** Settings screen for the application. Allows users to configure game settings. */
public class SettingsScreen implements Screen {
  private Stage stage;
  private Table table;
  private Skin skin;
  private Texture backgroundTexture;
  private Music backgroundMusic;
  private final GameLauncher game;

  private Label settingsTitleLabel;
  private Label musicVolumeLabel;
  private Slider musicVolumeSlider;
  private Label sfxVolumeLabel;
  private Slider sfxVolumeSlider;
  private TextButton backButton;

  public SettingsScreen(GameLauncher game, Skin skin) {
    this.game = game;
    this.skin = skin;
  }

  @Override
  public void show() {
    // Prepare your screen here.
    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);

    // Set background music
    backgroundMusic = game.assets.get("audio/bgm/main_menu_bg_music.mp3", Music.class);
    backgroundMusic.setLooping(true);
    backgroundMusic.setVolume(0.5f);
    backgroundMusic.play();

    table = new Table();
    table.defaults().padBottom(15f);
    table.setFillParent(true);
    stage.addActor(table);

    backgroundTexture = new Texture(Gdx.files.internal("images/background.png"));

    Image backgroundImage = new Image(backgroundTexture);
    backgroundImage.setScaling(Scaling.fill);

    table.setBackground(backgroundImage.getDrawable());

    // Settings title
    settingsTitleLabel = new Label("SETTINGS", skin, "title-font");
    table.add(settingsTitleLabel).padTop(40f);
    table.row();

    // Music Volume label
    musicVolumeLabel = new Label("MUSIC VOLUME", skin);
    table.add(musicVolumeLabel).padLeft(50f).padRight(50f).row();

    // Music Volume Slider
    musicVolumeSlider = new Slider(0f, 1f, 0.1f, false, skin, "default-horizontal");
    musicVolumeSlider.setValue(0.5f);
    table.add(musicVolumeSlider).width(300f).row();

    // SFX Volume label
    sfxVolumeLabel = new Label("SFX VOLUME", skin);
    table.add(sfxVolumeLabel).padLeft(50f).padRight(50f).row();

    // SFX Volume Slider
    sfxVolumeSlider = new Slider(0f, 1f, 0.1f, false, skin, "default-horizontal");
    sfxVolumeSlider.setValue(0.5f);
    table.add(sfxVolumeSlider).width(300f).row();

    // Back button
    backButton = new TextButton("BACK", skin, "main-menu-text-button");
    table.add(backButton).size(380f, 60f).padTop(30f);

    new SettingsController(game, this);
  }

  @Override
  public void render(float delta) {
    // Draw your screen here. "delta" is the time since last render in seconds.
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
    // Invoked when your application is paused.
    if (backgroundMusic.isPlaying()) {
      backgroundMusic.pause();
    }
  }

  @Override
  public void resume() {
    // Invoked when your application is resumed after pause.
    backgroundMusic.play();
  }

  @Override
  public void hide() {
    // This method is called when another screen replaces this one.
  }

  @Override
  public void dispose() {
    // Destroy screen's assets here.
    stage.dispose();
    backgroundTexture.dispose();
  }

  public TextButton getBackButton() {
    return backButton;
  }

  public Slider getMusicVolumeSlider() {
    return musicVolumeSlider;
  }

  public Slider getSfxVolumeSlider() {
    return sfxVolumeSlider;
  }
}
