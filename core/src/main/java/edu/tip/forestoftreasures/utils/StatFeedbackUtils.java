package edu.tip.forestoftreasures.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;

/**
 * Utility for showing animated floating text feedback when stats change.
 */
public class StatFeedbackUtils {

    /**
     * Spawns a floating, shaking, and fading text label at the specified position.
     *
     * @param stage    The stage to add the feedback to.
     * @param position The world position (stage coordinates) to spawn the text.
     * @param amount   The change amount (positive or negative).
     * @param font     The font to use for the label.
     */
    public static void showStatFeedback(Stage stage, Vector2 position, float amount, Font font) {
        if (amount == 0) return;

        String sign = (amount > 0) ? "+" : "";
        Color feedbackColor = (amount > 0) ? Color.valueOf("#66FF00") : Color.RED;
        
        // Format: {SHAKE}+n{ENDSHAKE} - Using the shake token for movement
        String formattedAmount = (amount % 1 == 0) ? String.format("%.0f", amount) : String.format("%.2f", amount);
        String text = String.format("{SHAKE}%s%s{ENDSHAKE}", sign, formattedAmount);

        TextraLabel label = new TextraLabel(text, font);
        
        // Directly apply the color to the label actor as requested
        label.setColor(feedbackColor);
        
        // Set exact position without any offsets
        label.setPosition(position.x, position.y);
        
        // Animation sequence: bubble up while fading out, then remove
        label.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveBy(0, 100f, 2.0f, Interpolation.pow2Out),
                Actions.fadeOut(2.0f, Interpolation.pow2In)
            ),
            Actions.removeActor()
        ));

        stage.addActor(label);
    }
}
