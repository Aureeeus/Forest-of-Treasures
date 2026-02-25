package edu.tip.forestoftreasures.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;

public final class DrawableFactory {
  private static Texture whiteTexture;
  private static final ObjectMap<String, Drawable> cacheDrawable = new ObjectMap<>();

  private DrawableFactory() {
    // Throw exception for invalid instantiation of utility class
    throw new InstantiationError("Utility class cannot be instantiated.");
  }

  // Creates a drawable with a color. Useful for background and border colors.
  public static Drawable getColoredDrawable(Color color) {
    if (whiteTexture == null) {
      Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      pixmap.setColor(Color.WHITE);
      pixmap.fill();
      
      whiteTexture = new Texture(pixmap);
      pixmap.dispose();
    }

    String hex = color.toString();
    // Check if color drawable is not created
    if (!cacheDrawable.containsKey(hex)) {
      cacheDrawable.put(hex, new TextureRegionDrawable(new TextureRegion(whiteTexture)).tint(color));
    }

    return cacheDrawable.get(hex);
  }

  public static Image createDivider(Color color) {
    Drawable whiteDrawable = getColoredDrawable(color);
    Image divider = new Image(whiteDrawable);

    return divider;
  }

  // Disposes the texture to prevent memory leaks. Must be called only within GameLauncher.
  public static void dispose() {
    if (whiteTexture != null) {
      whiteTexture.dispose();
      whiteTexture = null;
      cacheDrawable.clear();
    }
  }
}
