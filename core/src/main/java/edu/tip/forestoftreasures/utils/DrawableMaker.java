package edu.tip.forestoftreasures.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public final class DrawableMaker {
  private static Texture whiteTexture;

  private DrawableMaker() {
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


    return new TextureRegionDrawable(new TextureRegion(whiteTexture)).tint(color);
  }

  public static void dispose() {
    if (whiteTexture != null) {
      whiteTexture.dispose();
      whiteTexture = null;
    }
  }

}
