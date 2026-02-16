package edu.tip.forestoftreasures;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import edu.tip.forestoftreasures.Model.SettingsConfiguration;
import edu.tip.forestoftreasures.View.MainMenuScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class GameLauncher extends Game {
  public AssetManager assets;
  public SettingsConfiguration settingsConfig;

  @Override
  public void create() {
      assets = new AssetManager();

      SkinLoader.SkinParameter params = new SkinLoader.SkinParameter("ui/fotskin.atlas");
      assets.load("ui/fotskin.json", Skin.class, params);
      assets.finishLoading();

      settingsConfig = new SettingsConfiguration();

      this.setScreen(new MainMenuScreen(this));
  }


  @Override
  public void dispose() {
    super.dispose();
    assets.dispose();
  }
}