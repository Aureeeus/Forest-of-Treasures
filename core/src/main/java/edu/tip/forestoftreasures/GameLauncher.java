package edu.tip.forestoftreasures;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import edu.tip.forestoftreasures.Model.SettingsConfiguration;
import edu.tip.forestoftreasures.View.MainMenuScreen;
import edu.tip.forestoftreasures.utils.DrawableFactory;

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

      // --- Player Stats and Movesets UI ---
      assets.load("icons/stats_icons.png", Texture.class);
      assets.load("icons/Gear.png", Texture.class);
      assets.load("icons/dex_icon.png", Texture.class);
      assets.load("icons/skill1_icon.png", Texture.class);
      assets.load("icons/skill2_icon.png", Texture.class);
      assets.load("icons/skill3_icon.png", Texture.class);

      // --- Spritesheet icons ---
      assets.load("icons/dialogue_ui_sheet.png", Texture.class);
      
      // --- Day 1 UI ---
      assets.load("scenarios/day1/forest_intro.png", Texture.class);
      assets.load("scenarios/day1/forest_sprite.png", Texture.class);
      assets.load("scenarios/day1/deep_in_forest.png", Texture.class);
      assets.load("scenarios/day1/hobgoblin_hurt.png", Texture.class);
      assets.load("scenarios/day1/forest_treant.png", Texture.class);
      assets.load("scenarios/day1/day1_end.png", Texture.class);
      assets.finishLoading();

      settingsConfig = new SettingsConfiguration();

      this.setScreen(new MainMenuScreen(this));
  }


  @Override
  public void dispose() {
    super.dispose();
    assets.dispose();
    DrawableFactory.dispose();
  }
}