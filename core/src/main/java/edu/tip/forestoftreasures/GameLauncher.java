package edu.tip.forestoftreasures;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import edu.tip.forestoftreasures.View.MainMenuScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class GameLauncher extends Game {
  public Skin skin;

  @Override
  public void create() {
    skin = new Skin(Gdx.files.internal("ui/fotskin.json"), new TextureAtlas("ui/fotskin.atlas"));
    this.setScreen(new MainMenuScreen(this, skin));
  }

  @Override
  public void dispose() {
    super.dispose();
    skin.dispose();
  }

  public Skin getSkin() {
    return skin;
  }
}