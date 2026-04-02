package edu.tip.forestoftreasures.utils;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import edu.tip.forestoftreasures.GameLauncher;

/**
 * Utility factory for creating and configuring common UI components.
 * This class provides helper methods to ensure consistent styling and
 * rendering settings across the application's user interface.
 */
public final class UIFactory {
  /** Internal cache for storing generated component styles, keyed by the component name. */
  private static final Map<String, Object> styleCache = new HashMap<>();

  /**
   * Private constructor to prevent instantiation of the utility class.
   * @throws InstantiationError if an attempt is made to instantiate this class.
   */
  private UIFactory() {
    throw new InstantiationError("Utility class cannot be instantiated.");
  }

  /**
   * Creates and configures a UI component by name, leveraging a style cache 
   * to avoid recreating expensive resources (like textures and drawables).
   *
   * @param componentName The registered identifier for the required component.
   * @param game The GameLauncher reference to access the AssetManager.
   * @param <T> The expected Actor type.
   * @return A newly instantiated component with a shared, cached style.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Actor> T createComponent(String componentName, GameLauncher game) {
    return (T) switch (componentName) {
      case "wood_toggle_button" -> {
        ImageButton.ImageButtonStyle style = (ImageButton.ImageButtonStyle) styleCache.computeIfAbsent(
          componentName, key -> mapWoodToggleButtonStyle(game)
        );

        ImageButton toggleButton = new ImageButton(style);
        toggleButton.getImage().setScaling(Scaling.fit);
        toggleButton.getImageCell().expand().fill();
        
        yield toggleButton;
      }
      case "wood_slider" -> {
        // We do not cache the style for wood_slider because the drawables are uniquely bound
        // to this specific slider instance to dynamically read its layout size.
        Slider slider = new Slider(0f, 1f, 0.01f, false, new Slider.SliderStyle());
        slider.setStyle(mapWoodSliderStyle(game, slider));
        yield slider;
      }
      default -> throw new IllegalArgumentException("Unknown UI component: " + componentName);
    };
  }

  /**
   * Generates the shared ImageButtonStyle for the wood toggle button.
   *
   * @param game The GameLauncher reference with the AssetManager.
   * @return A styled configuration object that can be safely shared across instances.
   */
  private static ImageButton.ImageButtonStyle mapWoodToggleButtonStyle(GameLauncher game) {
    Texture offTexture = game.assets.get("images/ui/wood_checkbox_unchecked.png", Texture.class);
    Texture onTexture = game.assets.get("images/ui/wood_checkbox_checked.png", Texture.class);
              
    offTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    onTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    ImageButton.ImageButtonStyle newStyle = new ImageButton.ImageButtonStyle();
    newStyle.imageUp = new TextureRegionDrawable(new TextureRegion(offTexture));
    newStyle.imageChecked = new TextureRegionDrawable(new TextureRegion(onTexture));

    return newStyle;
  }

  /**
   * Generates a unique SliderStyle for a wood slider, directly bound to the slider instance.
   * This provides dynamic sizing that gracefully stretches to fill expanding Table layout cells.
   *
   * @param game The GameLauncher reference with the AssetManager.
   * @param slider The Slider instance these drawables will be bound to.
   * @return A styled configuration explicitly tied to the provided Slider's dimensions.
   */
  private static Slider.SliderStyle mapWoodSliderStyle(GameLauncher game, Slider slider) {
    Texture emptyTexture = game.assets.get("images/ui/wood_slider_empty.png", Texture.class);
    Texture filledTexture = game.assets.get("images/ui/wood_slider_filled.png", Texture.class);
    Texture grabberNormalTexture = game.assets.get("images/ui/wood_grabber_normal.png", Texture.class);
    Texture grabberPressedTexture = game.assets.get("images/ui/wood_grabber_pressed.png", Texture.class);

    emptyTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    filledTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    grabberNormalTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    grabberPressedTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    Slider.SliderStyle newStyle = new Slider.SliderStyle();
    
    newStyle.background = new DynamicHeightDrawable(new TextureRegion(emptyTexture), slider);
    newStyle.knobBefore = new DynamicHeightDrawable(new TextureRegion(filledTexture), slider);
    newStyle.knob = new DynamicHeightDrawable(new TextureRegion(grabberNormalTexture), slider);
    newStyle.knobDown = new DynamicHeightDrawable(new TextureRegion(grabberPressedTexture), slider);

    return newStyle;
  }

  /**
   * A tailored TextureRegionDrawable that dynamically sets its minWidth/Height 
   * to match its parent bounded Actor's layout height instead of forcing a fixed texture dimension.
   */
  private static class DynamicHeightDrawable extends TextureRegionDrawable {
      private final Slider boundActor;
      private final float originalAspect;

      public DynamicHeightDrawable(TextureRegion region, Slider boundActor) {
          super(region);
          this.boundActor = boundActor;
          this.originalAspect = (float) region.getRegionWidth() / region.getRegionHeight();
      }

      @Override
      public float getMinHeight() {
          // If the layout has not provided space yet, provide a base 36px hint to not collapse entirely.
          return boundActor == null || boundActor.getHeight() <= 0 ? 36f : boundActor.getHeight();
      }

      @Override
      public float getMinWidth() {
          float height = getMinHeight();
          // Scale width proportionally so knobs keep their pixel-art shape without skewing horizontally.
          return height * originalAspect;
      }
  }

  /**
   * Disposes of the internal cache. Must be called in the application's {@code dispose()} method.
   * Note: The actual textures are disposed by the AssetManager.
   */
  public static void dispose() {
    styleCache.clear();
  }
}
