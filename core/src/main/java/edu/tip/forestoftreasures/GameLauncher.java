package edu.tip.forestoftreasures;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import edu.tip.forestoftreasures.View.MainMenuScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class GameLauncher extends Game {
  public AssetManager assets;

  @Override
  public void create() {
      assets = new AssetManager();
      assets.load("ui/fotskin.json", Skin.class);
      assets.load("ui/fotskin.atlas", TextureAtlas.class);
      assets.finishLoading();

      this.setScreen(new MainMenuScreen(this));
  }


  @Override
  public void dispose() {
    super.dispose();
    assets.dispose();
  }
}