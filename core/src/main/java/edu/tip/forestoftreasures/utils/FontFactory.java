package edu.tip.forestoftreasures.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.github.tommyettinger.textra.Font;

public final class FontFactory {
  private FontFactory() {
    // Throw exception for invalid instantiation of utility class
    throw new InstantiationError("Utility class cannot be instantiated"); 
  }

  public static Font generateFont(String fontPath, int size, Color color) {
    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontPath));
    FreeTypeFontParameter params = new FreeTypeFontParameter();
    params.size = size;
    params.color = color;

    params.minFilter = Texture.TextureFilter.Nearest;
    params.magFilter = Texture.TextureFilter.Nearest;

    BitmapFont bitmapFont = generator.generateFont(params);
    generator.dispose();

    return new Font(bitmapFont);
  }
}
