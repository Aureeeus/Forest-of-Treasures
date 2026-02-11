package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.MainMenuController;
/** First screen of the application. Displayed after the application is created. */
public class MainMenuScreen implements Screen {
  private Stage stage;
  private Table table;
  private Skin skin;
  private Texture backgroundTexture;
  private Music backgroundMusic;
  private final GameLauncher game;

  private TextButton startButton;
  private TextButton achievementsButton;
  private TextButton settingsButton;
  private TextButton quitButton;

  public MainMenuScreen(GameLauncher game, Skin skin) {
    this.game = game;
    this.skin = skin;
  }

    @Override
    public void show() {
        // Prepare your screen here.
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Set background music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/main_menu_bg_music.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(1f);
        backgroundMusic.play();

        table = new Table();
        table.defaults().size(380, 60).spaceBottom(20f);
        table.setFillParent(true);
        stage.addActor(table);

        backgroundTexture = new Texture(Gdx.files.internal("images/background.png"));

        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setScaling(Scaling.fill);

        table.setBackground(backgroundImage.getDrawable());

        // Adding UI elements to the table
        startButton = new TextButton("START GAME", skin, "main-menu-text-button");
        table.add(startButton);
        table.row();

        achievementsButton = new TextButton("ACHIEVEMENTS", skin, "main-menu-text-button");
        table.add(achievementsButton);
        table.row();

        settingsButton = new TextButton("SETTINGS", skin, "main-menu-text-button");
        table.add(settingsButton);
        table.row();

        quitButton = new TextButton("QUIT", skin, "main-menu-text-button");
        table.add(quitButton);
        table.row();

        new MainMenuController(game, this);
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
        skin.dispose();
        backgroundMusic.dispose();
    }

    public TextButton getStartButton() {
        return startButton;
    }

    public TextButton getAchievementsButton() {
        return achievementsButton;
    }

    public TextButton getSettingsButton() {
        return settingsButton;
    }

    public TextButton getQuitButton() {
        return quitButton;
    }
}