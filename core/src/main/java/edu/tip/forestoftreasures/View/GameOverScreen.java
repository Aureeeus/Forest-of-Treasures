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
import edu.tip.forestoftreasures.utils.SaveManager;

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
    // Delete save progress on death to prevent checkpoint exploitation
    SaveManager.deleteSave();

    // Safety check: stop any gameplay music that might be leaking
    String[] musicTracks = {
      "audio/bgm/days/Day1 Music.mp3",
      "audio/bgm/days/Day2 Music.mp3",
      "audio/bgm/days/Day3 music.mp3",
      "audio/sfx/Battle_Screen_Music.mp3"
    };
    for (String track : musicTracks) {
      if (game.assets.isLoaded(track)) {
        game.assets.get(track, com.badlogic.gdx.audio.Music.class).stop();
      }
    }

    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);

    table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    textraFont = FontFactory.generateFont("fonts/DotGothic16-Regular.ttf", 24, Color.WHITE);

    String dialogue = "{COLOR=#FF0000}YOU DIED!!!{CLEARCOLOR}\nYour journey ends here. The forest claims another soul once again.";
    typingLabel = new TypingLabel(dialogue, textraFont);
    typingLabel.setWrap(true);

    if (game.settingsConfig.getGameSettings().isReadAloudEnabled()) {
      typingLabel.setTextSpeed(0.08f / 1.1f);
    }
    
    // Narrate the game over dialogue asynchronously (no-op if Read Aloud is disabled)
    game.speechManager.say(dialogue);

    String instructions = "[#FFD700]{FADE}PRESS SPACE TO RETURN TO MENU{ENDFADE}[%]";
    instructionLabel = new TextraLabel(instructions, textraFont);
    instructionLabel.setWrap(true);
    instructionLabel.setVisible(false);

    typingLabel.setTypingListener(new TypingAdapter() {
      @Override
      public void end() {
        showInstructionsIfSpeechFinished();
      }

      private void showInstructionsIfSpeechFinished() {
        if (game.speechManager.isSpeaking()) {
          Gdx.app.postRunnable(this::showInstructionsIfSpeechFinished);
        } else {
          instructionLabel.setVisible(true);
          instructionLabel.addAction(Actions.forever(Actions.sequence(
              Actions.fadeOut(.5f),
              Actions.fadeIn(.5f))));
        }
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
