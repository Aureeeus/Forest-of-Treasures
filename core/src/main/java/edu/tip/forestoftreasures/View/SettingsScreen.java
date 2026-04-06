package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.SettingsController;
import edu.tip.forestoftreasures.utils.FontFactory;
import edu.tip.forestoftreasures.utils.UIFactory;

/** Settings screen for the application. Allows users to configure game settings. */
public class SettingsScreen implements Screen {
  private final GameLauncher game;
  private final Music bgMusic;
  
  // Screen-level Components
  private Stage stage;
  private Table table;
  private Texture backgroundTexture;

  // UI Components
  private ImageButton exitButton;
  private Slider bgMusicVolumeSlider;
  private Slider sfxVolumeSlider;
  private Slider ambienceVolumeSlider;
  private Button readAloudButton;
  private Button skipDialogueButton;

  public SettingsScreen(GameLauncher game, Music bgMusic) {
    this.game = game;
    this.bgMusic = bgMusic;

    initialize();
  }

  private void initialize() {
    stage = new Stage(new ScreenViewport());

    backgroundTexture = game.assets.get("images/backgrounds/settings_bg.png", Texture.class);

    // Screen background + table layout
    table = new Table();
    table.setFillParent(true);
    table.setBackground(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
    table.pad(65f, 80f, 65f, 80f); // Internal padding to the table to match the background's border
    stage.addActor(table);

    // =========================================
    // Top Panel (Title and Exit Button)
    // =========================================
    
    // Add title and exit button
    addTitleAndExitButton();

    // =========================================
    // Main Panel
    // =========================================

    // Create background music volume slider
    addVolumeControls(table, "BACKGROUND MUSIC:");

    // Create sound effects volume slider
    addVolumeControls(table, "SOUND EFFECTS:");

    // Create ambience volume slider
    addVolumeControls(table, "AMBIENCE:");

    // Create read aloud toggle button
    addToggleButton(table, "READ ALOUD:");

    // Create skip dialogue toggle button
    addToggleButton(table, "SKIP DIALOGUE:");

    // Link controller to this screen
    new SettingsController(game, this);
  }

  /**
   * Adds the title and exit button to the main table.
   */
  private void addTitleAndExitButton() {
    Table topTable = new Table();
    table.add(topTable)
      .growX()
      .top()
      .colspan(2)
      .row();

    // Title
    Font titleFontXXL= FontFactory.generateFont("fonts/PressStart2P-Regular.ttf", 70, Color.valueOf("#341f17"));
    TextraLabel titleLabel = new TextraLabel("SETTINGS", titleFontXXL);

    // Exit button
    Texture selectIconSheet = game.assets.get("icons/dialogue_ui_sheet.png", Texture.class);
    TextureRegion exitButtonTextureRegion = new TextureRegion(selectIconSheet, 512, 256, 64, 64);
    // Create tinted drawable for the icon
    exitButton = new ImageButton(createTintedDrawable(exitButtonTextureRegion, "#c78861"));
    exitButton.getImageCell().expand().fill();

    // Top table layout: [Spacer] [Title] [Exit Button]
    // Spacer width matches exit button (150f) to keep Title centered
    topTable.add().width(150f);
    topTable.add(titleLabel).expandX().center();
    topTable.add(exitButton).size(150f).padRight(40f).right();
  }

  /**
   * Adds a toggle button row to the main table.
   * @param mainTable The main table to add the toggle button row to.
   * @param label The label for the toggle button.
   */
  private void addToggleButton(Table mainTable, String label) {
    Table toggleTable = new Table();
    Font labelFontXL = FontFactory.generateFont("fonts/PressStart2P-Regular.ttf", 50, Color.valueOf("#341f17"));

    // Toggle label and button
    TextraLabel toggleLabel = new TextraLabel(label, labelFontXL);
    Button toggleButton = UIFactory.createComponent("wood_toggle_button", game);

    // Set the toggle button's checked state based on the label + link to class-level button
    boolean isEnabled = switch (label) {
      case "READ ALOUD:" -> {
        readAloudButton = toggleButton;
        yield game.settingsConfig.getGameSettings().isReadAloudEnabled();
      }
      case "SKIP DIALOGUE:" -> {
        skipDialogueButton = toggleButton;
        yield game.settingsConfig.getGameSettings().isSkipDialogueEnabled();
      }
      default -> false;
    };

    toggleButton.setChecked(isEnabled);

    // Add them to the sub-table
    toggleTable.add(toggleLabel).padLeft(40f);
    toggleTable.add(toggleButton).size(80f).padBottom(20f);

    // Add the grouped row to the main grid
    mainTable.add(toggleTable).colspan(2).left().expand().row();
  }

  /**
   * Adds a volume control row to the main table.
   * @param mainTable The main table to add the volume control row to.
   * @param label The label for the volume control.
   */
  private void addVolumeControls(Table mainTable, String label) {
    Table volumeTable = new Table();
    Font labelFontXL = FontFactory.generateFont("fonts/PressStart2P-Regular.ttf", 50, Color.valueOf("#341f17"));

    // Volume label and slider
    TextraLabel volumeLabel = new TextraLabel(label, labelFontXL);
    Slider volumeSlider = UIFactory.createComponent("wood_slider", game);

    // Link the slider to class-level field AND set the correct initial value per category
    float initialValue = switch (label) {
      case "BACKGROUND MUSIC:" -> {
        bgMusicVolumeSlider = volumeSlider;
        yield game.settingsConfig.getGameSettings().bgMusicVolume();
      }
      case "SOUND EFFECTS:" -> {
        sfxVolumeSlider = volumeSlider;
        yield game.settingsConfig.getGameSettings().sfxVolume();
      }
      case "AMBIENCE:" -> {
        ambienceVolumeSlider = volumeSlider;
        yield game.settingsConfig.getGameSettings().ambienceVolume();
      }
      default -> throw new IllegalArgumentException("Unknown volume control: " + label);
    };
    volumeSlider.setValue(initialValue);

    // Add them to the sub-table
    volumeTable.add(volumeLabel).padLeft(40f);
    volumeTable.add(volumeSlider).width(400f).height(50f).padBottom(20f);

    // Add the grouped row to the main grid
    mainTable.add(volumeTable).colspan(2).left().expand().row();
  }

  /**
   * Helper method to create a tinted TextureRegionDrawable.
   * @param region The texture region to tint.
   * @param hexColor The hex color string (e.g. "#2596be").
   * @return A tinted Drawable.
   */
  private Drawable createTintedDrawable(TextureRegion region, String hexColor) {
    return new TextureRegionDrawable(region).tint(Color.valueOf(hexColor));
  }

  @Override
  public void show() {
    // Prepare your screen here.
    Gdx.input.setInputProcessor(stage);
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
    if (bgMusic != null && bgMusic.isPlaying()) {
      bgMusic.pause();
    }
  }

  @Override
  public void resume() {
    // Invoked when your application is resumed after pause.
    Gdx.input.setInputProcessor(stage);
    if (bgMusic != null && !bgMusic.isPlaying()) {
      bgMusic.play();
    }
  }

  @Override
  public void hide() {
    // This method is called when another screen replaces this one.
    Gdx.input.setInputProcessor(null);
  }

  @Override
  public void dispose() {
    // Destroy screen's assets here.
    if (stage != null) stage.dispose();
  }

  // ============================
  // Getters for controller
  // ============================

  /**
   * Gets the exit button.
   * @return The exit button.
   */
  public ImageButton getExitButton() {
    return exitButton;
  }

  /**
   * Gets the background music track that is currently playing.
   * @return The music track.
   */
  public Music getBgMusic() {
    return bgMusic;
  }

  /**
   * Gets the background music volume slider.
   * @return The background music volume slider.
   */
  public Slider getBgMusicVolumeSlider() {
    return bgMusicVolumeSlider;
  }

  /**
   * Gets the sound effects volume slider.
   * @return The sound effects volume slider.
   */
  public Slider getSfxVolumeSlider() {
    return sfxVolumeSlider;
  }

  /**
   * Gets the ambience volume slider.
   * @return The ambience volume slider.
   */
  public Slider getAmbienceVolumeSlider() {
    return ambienceVolumeSlider;
  }

  /**
   * Gets the read aloud button.
   * @return The read aloud button.
   */
  public Button getReadAloudButton() {
    return readAloudButton;
  }

  /**
   * Gets the skip dialogue button.
   * @return The skip dialogue button.
   */
  public Button getSkipDialogueButton() {
    return skipDialogueButton;
  }
}
