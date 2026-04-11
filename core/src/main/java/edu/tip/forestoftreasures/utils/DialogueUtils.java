package edu.tip.forestoftreasures.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.TypingAdapter;
import com.github.tommyettinger.textra.TypingLabel;

import edu.tip.forestoftreasures.GameLauncher;

/**
 * Utility class for handling common dialogue-related tasks such as
 * configuring typing labels and trimming overflowing text in tables.
 */
public final class DialogueUtils {

  private DialogueUtils() {
    throw new InstantiationError("Utility class cannot be instantiated.");
  }

  /**
   * Configures a TypingLabel with common settings such as word wrap and
   * skip-dialogue behavior based on game settings.
   *
   * @param label      The TypingLabel to configure.
   * @param game       The GameLauncher for accessing settings.
   * @param onComplete Optional callback to execute when typing finishes.
   */
  public static void configureTypingLabel(TypingLabel label, GameLauncher game, Runnable onComplete) {
    label.setWrap(true);

    boolean isSkipDialogueEnabled = game.settingsConfig.getGameSettings().isSkipDialogueEnabled();
    if (isSkipDialogueEnabled) {
      label.skipToTheEnd();
      if (onComplete != null) {
        Gdx.app.postRunnable(onComplete);
      }
    } else if (onComplete != null) {
      label.setTypingListener(new TypingAdapter() {
        @Override
        public void end() {
          onComplete.run();
        }
      });
    }
  }

  /**
   * Removes old dialogue lines from the top of a table until all content fits
   * inside the table's allocated height.
   *
   * @param table The table containing dialogue lines (actors).
   * @return true if the trim was successful, false if layout wasn't ready (pending trim).
   */
  public static boolean trimDialogueOverflow(Table table) {
    if (table == null) return true;

    // Force immediate table layout so we can query exact allocated dimensions
    table.invalidateHierarchy();
    table.validate();

    float tableHeight = table.getHeight();
    if (tableHeight <= 0) {
      return false; // screen layout not ready yet, retry next frame
    }

    // Validate that all actors have computed their physical heights.
    for (var cell : table.getCells()) {
      if (!cell.hasActor()) continue;
      
      if (cell.getActor() instanceof Widget widget) {
        widget.validate(); 
      }
      
      if (cell.getActorHeight() <= 0) {
        return false; // wait for LibGDX layout frame to settle
      }
    }

    while (true) {
      // Calculate true content height exclusively for active cells
      float contentHeight = table.getPadTop() + table.getPadBottom();
      int activeActorCount = 0;

      for (var cell : table.getCells()) {
        if (!cell.hasActor()) continue;
        activeActorCount++;
        contentHeight += cell.getActorHeight() + cell.getPadTop() + cell.getPadBottom();
      }

      // If everything fits OR we only have 1 active line left, we are done
      if (contentHeight <= tableHeight || activeActorCount <= 1) {
        break; 
      }

      // Find the oldest valid actor and remove it
      Actor oldestActor = null;
      for (var cell : table.getCells()) {
        if (cell.hasActor()) {
          oldestActor = cell.getActor();
          break; // only remove one per loop iteration
        }
      }

      if (oldestActor != null) {
        Array<Actor> activeActors = new Array<>();
        for (var cell : table.getCells()) {
          if (cell.hasActor() && cell.getActor() != oldestActor) {
            activeActors.add(cell.getActor());
          }
        }
        
        table.clearChildren();
        
        for (Actor a : activeActors) {
          var cell = table.add(a).growX().bottom().padBottom(5f);
          if ("dayLabel".equals(a.getName())) {
            cell.center();
          } else {
            cell.left();
          }
          cell.row();
        }
      }

      // Re-layout explicitly after removal to update cell dependencies
      table.invalidateHierarchy();
      table.validate();
    }
    
    return true;
  }
}
