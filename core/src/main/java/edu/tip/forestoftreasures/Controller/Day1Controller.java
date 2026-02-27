package edu.tip.forestoftreasures.Controller;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingAdapter;
import com.github.tommyettinger.textra.TypingLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Model.Achievement;
import edu.tip.forestoftreasures.Model.AchievementVerifier;
import edu.tip.forestoftreasures.Model.ChoiceNode;
import edu.tip.forestoftreasures.Model.DialogueLoader;
import edu.tip.forestoftreasures.Model.DialogueLoader.DayData;
import edu.tip.forestoftreasures.Model.DialogueNode;
import edu.tip.forestoftreasures.Model.DialogueRunner;
import edu.tip.forestoftreasures.Model.LineNode;
import edu.tip.forestoftreasures.Model.MinigameNode;
import edu.tip.forestoftreasures.View.Day1Screen;
import edu.tip.forestoftreasures.View.MazeBossScreen;

public class Day1Controller implements DialogueRunner.DisplayHandler {
  // Data of thee Dialogue depening on the day
  private static final String STORY_FILE = "dialogue/story_schema.json";
  private static final String DAY_KEY    = "day1";

  private final GameLauncher game;
  private final Day1Screen screen;

  // --- Dialogue system ---
  private final DialogueRunner runner;
  private final AchievementVerifier achievementVerifier;
  private DialogueNode storyRoot;          // kept for BFS validation in AchievementVerifier
  private List<Achievement> achievements;  // loaded from JSON

  private boolean pendingOverflowTrim = false;

  // --- Choice UI state ---
  private int selectedRow = 0;
  private ChoiceNode activeChoiceNode; // the currently displayed choice, null when not choosing

  // UI references
  private Image settingsIcon;
  private final Image selectChoiceIcon;

  private final Table scenarioContentTable;
  private final Table textDialogueTable;
  private final Table dialogueWidgetTable;

  private Container<Actor> dialogueCell0 = new Container<>(null);
  private Container<Actor> dialogueCell1 = new Container<>(null).align(Align.left | Align.center);
  private Container<Actor> dialogueCell2 = new Container<>(null);
  private Container<Actor> dialogueCell3 = new Container<>(null).align(Align.left | Align.center);

  // --- Fonts (disposable) ---
  private final Font dialogueFont;
  private final Font selectChoiceFont;

  /**
   * Constructs the Day1Controller, wires up UI references, loads the day
   * from JSON, and starts the runner from the loaded root node.
   *
   * @param game   The main game launcher holding the asset manager.
   * @param screen The Day1Screen holding all LibGDX stage and table references.
   */
  public Day1Controller(GameLauncher game, Day1Screen screen) {
    this.game = game;
    this.screen = screen;

    this.scenarioContentTable = screen.getScenarioContentTable();
    this.textDialogueTable = screen.getTextDialogueTable();
    this.dialogueWidgetTable = screen.getDialogueWidgetTable();
    this.settingsIcon = screen.getSettingsIcon();
    
    Texture selectIconSheet = game.assets.get("icons/dialogue_ui_sheet.png", Texture.class);
    TextureRegion selectChoiceTexture = new TextureRegion(selectIconSheet, 448, 384, 64, 64);
    this.selectChoiceIcon = new Image(selectChoiceTexture);

    this.dialogueFont = new Font(Gdx.files.internal("fonts/DotGothic16-Dialogue.fnt"));
    dialogueFont.adjustLineHeight(1.3f);
    this.selectChoiceFont = new Font(Gdx.files.internal("fonts/DotGothic16-Medium.fnt"));

    // Pass this controller as the DisplayHandler — runner calls back into showLine() etc.
    this.runner = new DialogueRunner(this);
    this.achievementVerifier = new AchievementVerifier();

    addListeners();
    loadAndStartDay();
  }

  /**
   * Loads the day1 data from story.json via DialogueLoader and starts the runner.
   *
   * DialogueLoader reads the JSON and returns a DayData record containing:
   *   - rootNode    : the first node to pass into runner.start()
   *   - achievements: the list of achievements defined for this day
   *
   * Both are stored so they are available when onDialogueEnd() fires.
   *
   * NOTE: All textures referenced in story.json must already be loaded by
   * AssetManager before this method is called — typically in your LoadingScreen.
   */
  private void loadAndStartDay() {
    DayData day = DialogueLoader.load(STORY_FILE, DAY_KEY);

    this.storyRoot    = day.rootNode();
    this.achievements = day.achievements();

    runner.start(storyRoot);
  }

  /**
   * Called by DialogueRunner when the next node is a LineNode.
   *
   * Resolves the node's texture path via game.assets.get() — this is safe
   * because AssetManager already holds the texture from the loading screen.
   * Then creates a TypingLabel. When typing ends, runner.onLineFinished()
   * is called to advance the graph automatically.
   *
   * @param node The LineNode containing the text and texture path to display.
   */
  @Override
  public void showLine(LineNode node) {
    // Resolve texture path → Texture via AssetManager (already pre-loaded)
    if (node.texturePath != null) {
      Texture texture = game.assets.get(node.texturePath, Texture.class);
      showImageScenario(texture);
    }

    TypingLabel typingLabel = new TypingLabel(node.text, dialogueFont);
    typingLabel.setWrap(true);

    // ! Developer condition to skip lines
    // if (!typingLabel.hasEnded()) typingLabel.skipToTheEnd();

    // Notify the runner when the typing animation finishes so the graph advances
    typingLabel.setTypingListener(new TypingAdapter() {
      @Override
      public void end() {
        runner.onLineFinished();
      }
    });

    textDialogueTable.add(typingLabel)
      .growX()
      .bottom()
      .left()
      .padBottom(5f)
      .row();

    textDialogueTable.invalidateHierarchy();

    Gdx.app.postRunnable(this::trimDialogueOverflow);
  }
  
  /**
   * Removes old dialogue lines from the top until all content fits inside the table.
   */
  private void trimDialogueOverflow() {
    textDialogueTable.validate();

    float tableHeight = textDialogueTable.getHeight();
    if (tableHeight <= 0) {
      pendingOverflowTrim = true; // layout not ready yet, retry next frame //
      return;
    }

    // Check if any cell actor height is still unresolved — layout not fully settled
    for (var cell : textDialogueTable.getCells()) {
      if (cell.getActorHeight() <= 0) {
        pendingOverflowTrim = true; // defer until all actors are properly measured
        return;
      }
    }

    pendingOverflowTrim = false;

    while (textDialogueTable.getChildren().size > 1) {
      float contentHeight = textDialogueTable.getPadTop() + textDialogueTable.getPadBottom();

      for (var cell : textDialogueTable.getCells()) {
        contentHeight += cell.getActorHeight() + cell.getPadTop() + cell.getPadBottom();
      }

      if (contentHeight <= tableHeight) break; // fits, stop trimming 

      textDialogueTable.removeActorAt(0, true); // remove oldest line
      textDialogueTable.invalidateHierarchy();
      textDialogueTable.validate();
    }
  }

  /**
   * Retries a deferred overflow trim if the table height wasn't ready
   * on the frame the minigame returned. Clears the flag once trim runs.
   */
  public void update() {
    if (pendingOverflowTrim) {
      trimDialogueOverflow();
    }
  }

  /**
   * Allows external triggers (e.g. resize, minigame return) to re-queue a trim
   * 
   * @param value New flag value to be set for possible trimming.
   */
  public void setPendingOverflowTrim(boolean value) {
    pendingOverflowTrim = value;
  }

  /**
   * Called by DialogueRunner when the next node is a ChoiceNode.
   *
   * Stores the active choice node so the input listener knows which node to
   * resolve on confirmation. Renders choices into the widget table and transfers
   * keyboard focus. The runner is paused here — it will not advance until
   * onChoiceSelected() is called by the input listener.
   *
   * @param node The ChoiceNode containing the list of choices to display.
   */
  @Override
  public void showChoices(ChoiceNode node) {
    activeChoiceNode = node;
    selectedRow      = 0; // reset cursor to first option each time

    renderChoiceWidgets(node);
    screen.getStage().setKeyboardFocus(dialogueWidgetTable);
  }

  /**
   * Called by DialogueRunner when the next node is a MinigameNode.
   *
   * Switches to the appropriate minigame screen via game.setScreen().
   * Day1Screen is hidden but NOT disposed — all dialogue state is preserved.
   * The graph resumes when the minigame screen calls runner.onMinigameFinished().
   *
   * @param node The MinigameNode containing the screenKey to launch.
   */
  @Override
  public void showMinigame(MinigameNode node) {
    game.setScreen(resolveMinigameScreen(node.screenKey));
  }

  /**
   * Called by DialogueRunner when the last node in the graph is reached.
   *
   * Triggers end-of-day logic: runs achievement verification and logs results.
   * Replace log statements with your actual unlock UI or save system.
   */
  @Override
  public void onDialogueEnd() {
    Gdx.app.log("Day1Controller", "Day complete. Checking achievements...");
    checkAchievements();
  }

  // ---------------------------------------------------------------------------
  // MINIGAME SCREEN RESOLUTION
  // ---------------------------------------------------------------------------

  /**
   * Maps a screenKey string from story.json to its actual minigame Screen.
   *
   * The minigame screen receives a Runnable callback instead of a direct
   * runner reference — it calls onComplete.run() when the player passes,
   * which returns to Day1Screen and resumes the dialogue graph.
   *
   * To add a new minigame: add a new case here and create its Screen class.
   *
   * @param screenKey The key defined in the MinigameNode's JSON.
   * @return The minigame Screen to transition to.
   */
  private Screen resolveMinigameScreen(String screenKey) {
    Runnable onComplete = () -> {
      game.setScreen(screen); // return to Day1Screen — state is preserved
      setPendingOverflowTrim(true);
      runner.onMinigameFinished(); // resume dialogue graph from next node
    };

    // TODO: Fix label overflowing
    return switch (screenKey) {
      // Add new minigame screens here as cases
      case "maze_minigame" -> new MazeBossScreen(game, onComplete);
      default -> throw new RuntimeException(
        "[Day1Controller] Unknown minigame screenKey: '" + screenKey + "'"
      );
    };
  }

  /**
   * Renders the choices from a ChoiceNode into the dialogue widget table.
   *
   * Currently supports up to 2 choices using fixed cell containers.
   * If you need more than 2 in the future, refactor to build cells
   * dynamically from node.choices.size().
   *
   * @param node The ChoiceNode whose choices should be rendered.
   */
  private void renderChoiceWidgets(ChoiceNode node) {
    // Row 0
    TextraLabel choice0Label = new TextraLabel(node.choices.get(0).label(), selectChoiceFont);
    choice0Label.setAlignment(Align.left);
    dialogueCell0.setActor(selectChoiceIcon);
    dialogueCell1.setActor(choice0Label);
    dialogueCell1.fill();
    tintCell(dialogueCell1, true);

    dialogueWidgetTable.add(dialogueCell0).size(30f).padRight(20f).padBottom(20f);
    dialogueWidgetTable.add(dialogueCell1).growX().align(Align.left | Align.center).padBottom(20f).row();

    // Row 1
    TextraLabel choice1Label = new TextraLabel(node.choices.get(1).label(), selectChoiceFont);
    choice1Label.setAlignment(Align.left);
    dialogueCell3.setActor(choice1Label);
    dialogueCell3.fill();
    tintCell(dialogueCell3, false);

    dialogueWidgetTable.add(dialogueCell2).size(30f).padRight(20f);
    dialogueWidgetTable.add(dialogueCell3).growX().align(Align.left | Align.center);
  }

  /**
   * Removes all choice widgets from the dialogue widget table and resets state.
   * Called immediately before forwarding the confirmed choice to the runner.
   */
  private void clearChoiceWidgets() {
    dialogueWidgetTable.clearChildren();
    dialogueCell0.setActor(null);
    dialogueCell1.setActor(null);
    dialogueCell2.setActor(null);
    dialogueCell3.setActor(null);
    activeChoiceNode = null;
  }

  /**
   * Updates the visual selection state of both rows based on selectedRow.
   * Moves the arrow icon to the selected row and applies color tinting.
   */
  private void refreshSelection() {
    if (selectedRow == 0) {
      dialogueCell0.setActor(selectChoiceIcon);
      dialogueCell2.setActor(null);
      tintCell(dialogueCell1, true);
      tintCell(dialogueCell3, false);
    } else {
      dialogueCell0.setActor(null);
      dialogueCell2.setActor(selectChoiceIcon);
      tintCell(dialogueCell3, true);
      tintCell(dialogueCell1, false);
    }
  }

  /**
   * Applies a yellow highlight to the selected cell or resets it to white.
   *
   * @param cell       The container whose actor should be tinted.
   * @param isSelected True to highlight, false to reset to white.
   */
  private void tintCell(Container<?> cell, boolean isSelected) {
    if (cell.getActor() == null) return;
    cell.getActor().setColor(isSelected ? Color.valueOf("#FFDB51") : Color.WHITE);
  }

    /**
   * Checks all achievements loaded from JSON against the player's recorded path.
   *
   * AchievementVerifier runs BFS on the graph first to validate each achievement
   * is reachable before checking the player's path. Invalid achievements are
   * skipped with a warning log so one broken definition doesn't affect the rest.
   */
  private void checkAchievements() {
    List<Achievement> unlocked = achievementVerifier.getUnlockedAchievements(
      achievements,
      runner.getPlayerPath(),
      storyRoot
    );

    if (unlocked.isEmpty()) {
      Gdx.app.log("Day1Controller", "No achievements unlocked.");
      return;
    }

    for (Achievement achievement : unlocked) {
      Gdx.app.log("Day1Controller", "Unlocked: ["
        + achievement.id + "] " + achievement.description);
    }
  }

  /**
   * Registers all input listeners for the settings icon and choice widget.
   */
  private void addListeners() {
    addSettingsIconListener();
    addChoiceInputListener();
  }

  /**
   * Adds hover and click color feedback to the settings icon.
   */
  private void addSettingsIconListener() {
    screen.getSettingsIcon().addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Gdx.app.log("Day1Controller", "Settings clicked!");
      }

      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        settingsIcon.setColor(Color.valueOf("#808080"));
        return super.touchDown(event, x, y, pointer, button);
      }

      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        settingsIcon.setColor(Color.WHITE);
        super.touchUp(event, x, y, pointer, button);
      }

      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (pointer == -1) settingsIcon.setColor(Color.valueOf("#c7c7c7"));
        super.enter(event, x, y, pointer, fromActor);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (pointer == -1) settingsIcon.setColor(Color.WHITE);
        super.exit(event, x, y, pointer, toActor);
      }
    });
  }

   /**
   * Adds keyboard navigation and confirmation to the dialogue widget table.
   *
   * UP/DOWN moves the cursor between choice rows.
   * ENTER or SPACE confirms the highlighted choice:
   *   1. Clears the choice UI from the screen.
   *   2. Calls runner.onChoiceSelected(selectedRow) which records the decision
   *      in PlayerPathTracker and advances the graph to the chosen branch.
   *
   * Input is only processed when activeChoiceNode is not null, ensuring
   * keyboard events during normal dialogue lines are safely ignored.
   */
  private void addChoiceInputListener() {
    dialogueWidgetTable.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Input.Keys.UP) {
          selectedRow = Math.max(0, selectedRow - 1);
          refreshSelection();
          return true;
        }

        if (keycode == Input.Keys.DOWN) {
          selectedRow = Math.min(1, selectedRow + 1);
          refreshSelection();
          return true;
        }

        if ((keycode == Input.Keys.ENTER)
            && activeChoiceNode != null) {
          clearChoiceWidgets();
          runner.onChoiceSelected(selectedRow);
          return true;
        }

        return false;
      }
    });
  }

  /**
   * Replaces the current background image with a new texture.
   * Clears any existing image in the scenario table first to prevent stacking.
   *
   * @param scenarioTexture The new background texture to display.
   */
  private void showImageScenario(Texture scenarioTexture) {
    if (scenarioContentTable.getCells().size > 0) {
      scenarioContentTable.clearChildren();
    }

    Image image = new Image(scenarioTexture);
    image.setScaling(Scaling.fit);

    scenarioContentTable.add(image).grow();
  }

  /**
   * Disposes of all font resources held by this controller.
   *
   * Must be called from Day1Screen.dispose() to prevent memory leaks.
   * Fonts are not managed by AssetManager so they require manual disposal.
   * Textures are NOT disposed here — AssetManager owns and manages them.
   */
  public void dispose() {
    dialogueFont.dispose();
    selectChoiceFont.dispose();
  }
}
