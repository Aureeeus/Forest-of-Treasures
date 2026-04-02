package edu.tip.forestoftreasures.utils;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.github.tommyettinger.textra.Font;

/**
 * Utility class for generating and caching TextraTypist fonts.
 * Fonts are cached by their configuration (path, size, color) so the same
 * font is only generated once no matter how many screens request it.
 *
 * Call {@link #disposeAll()} once at application shutdown (e.g., in
 * {@code GameLauncher.dispose()}) to release all cached font resources.
 */
public final class FontFactory {
  /** Internal cache for storing generated {@link Font} instances, keyed by a generated configuration string. */
  private static final Map<String, Font> cache = new HashMap<>();

  /**
   * Private constructor to prevent instantiation of the utility class.
   * @throws InstantiationError if an attempt is made to instantiate this class.
   */
  private FontFactory() {
    throw new InstantiationError("Utility class cannot be instantiated");
  }

  /**
   * Returns a cached font for the given configuration, generating it
   * on first request. Subsequent calls with the same parameters return
   * the same Font instance — no duplicate native resources.
   *
   * @param fontPath path to the .ttf font file in assets
   * @param size     pixel size of the generated font
   * @param color    font color
   * @return a cached {@link Font} instance
   */
  public static Font generateFont(String fontPath, int size, Color color) {
    String key = fontPath + "-" + size + "-" + color.toString();

    Font cached = cache.get(key);
    if (cached != null) {
      return cached;
    }

    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontPath));
    FreeTypeFontParameter params = new FreeTypeFontParameter();
    params.size = size;
    params.color = color;

    params.minFilter = Texture.TextureFilter.Nearest;
    params.magFilter = Texture.TextureFilter.Nearest;

    BitmapFont bitmapFont = generator.generateFont(params);
    generator.dispose();

    Font font = new Font(bitmapFont);
    cache.put(key, font);

    return font;
  }

  /**
   * Disposes all cached fonts and clears the cache.
   * Must be called once at application shutdown to prevent memory leaks.
   */
  public static void disposeAll() {
    for (Font font : cache.values()) {
      font.dispose();
    }
    cache.clear();
  }
}
