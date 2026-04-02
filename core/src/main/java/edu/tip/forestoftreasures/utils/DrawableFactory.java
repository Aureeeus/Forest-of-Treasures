package edu.tip.forestoftreasures.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Utility factory for creating, caching, and managing {@link Drawable} instances.
 * This class optimizes graphics operations by caching reusable color drawables.
 */
public final class DrawableFactory {
  /** 1x1 white texture used for generating colored drawables. */
  private static Texture whiteTexture;
  
  /** Cache for storing colored drawables, keyed by their hex color string. */
  private static final ObjectMap<String, Drawable> cacheDrawable = new ObjectMap<>();

  /**
   * Private constructor to prevent instantiation of this utility class.
   * @throws InstantiationError if an attempt is made to instantiate this class.
   */
  private DrawableFactory() {
    throw new InstantiationError("Utility class cannot be instantiated.");
  }

  /**
   * Returns a {@link Drawable} of a specific color, generating and caching it if needed.
   * Useful for UI background and border colors.
   *
   * @param color The {@link Color} for the drawable.
   * @return A {@link Drawable} instance of the specified color.
   */
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

  /**
   * Creates an {@link Image} instance that serves as a visual divider with a specific color.
   *
   * @param color The {@link Color} for the divider.
   * @return An {@link Image} styled as a colored divider.
   */
  public static Image createDivider(Color color) {
    Drawable whiteDrawable = getColoredDrawable(color);
    Image divider = new Image(whiteDrawable);

    return divider;
  }

  /**
   * Disposes of the internal texture resources and clears the drawable cache.
   * Must be called in the application's {@code dispose()} method to prevent memory leaks.
   */
  public static void dispose() {
    if (whiteTexture != null) {
      whiteTexture.dispose();
      whiteTexture = null;
      cacheDrawable.clear();
    }
  }
}
