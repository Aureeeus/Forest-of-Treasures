package edu.tip.forestoftreasures.Controller;

import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
import com.github.tommyettinger.textra.TypingLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Model.Achievement;
import edu.tip.forestoftreasures.Model.AchievementVerifier;
import edu.tip.forestoftreasures.Model.dialogue.ChoiceNode;
import edu.tip.forestoftreasures.Model.dialogue.DialogueLoader;
import edu.tip.forestoftreasures.Model.dialogue.DialogueLoader.DayData;
import edu.tip.forestoftreasures.Model.dialogue.DialogueNode;
import edu.tip.forestoftreasures.Model.dialogue.DialogueRunner;
import edu.tip.forestoftreasures.Model.dialogue.LineNode;
import edu.tip.forestoftreasures.Model.dialogue.ManualRollNode;
import edu.tip.forestoftreasures.Model.dialogue.MinigameNode;
import edu.tip.forestoftreasures.View.DayScreen;
import edu.tip.forestoftreasures.View.CreditsScreen;
import edu.tip.forestoftreasures.View.EntityBattleScreen;
import edu.tip.forestoftreasures.View.GameOverScreen;
import edu.tip.forestoftreasures.View.mazeBossScreen;
import edu.tip.forestoftreasures.Model.entities.Player;
import edu.tip.forestoftreasures.utils.DialogueUtils;
import edu.tip.forestoftreasures.utils.UIFactory;

public class DayController implements DialogueRunner.DisplayHandler {
  // --- Developer Options ---
  /**
   * Developer flag. Set to true to automatically skip/win minigames.
   * This is useful for testing without having to clear the minigame manually.
   */
  public static final boolean DEV_SKIP_MINIGAMES = false;

  // Data of the Dialogue depending on the day
  private static final String STORY_FILE = "dialogue/story_schema.json";
  private int currentDay = 2;

  private final GameLauncher game;
  private final DayScreen screen;

  // --- Dialogue system ---
  private final DialogueRunner runner;
  private final AchievementVerifier achievementVerifier;
  private DialogueNode storyRoot; // kept for BFS validation in AchievementVerifier
  private List<Achievement> achievements; // loaded from JSON

  private boolean pendingOverflowTrim = false;

  // --- Choice UI state ---
  private int selectedRow = 0;
  private ChoiceNode activeChoiceNode; // the currently displayed choice, null when not choosing
  private ManualRollNode activeManualRollNode;

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
   * Constructs the DayController, wires up UI references, loads the day
   * from JSON, and starts the runner from the loaded root node.
   *
   * @param game   The main game launcher holding the asset manager.
   * @param screen The DayScreen holding all LibGDX stage and table references.
   */
  public DayController(GameLauncher game, DayScreen screen) {
    this.game = game;
    this.screen = screen;

    this.scenarioContentTable = screen.getScenarioContentTable();
    this.textDialogueTable = screen.getTextDialogueTable();
    this.dialogueWidgetTable = screen.getDialogueWidgetTable();
    this.settingsIcon = screen.getSettingsIcon();

    this.selectChoiceIcon = UIFactory.getSelectionArrowIcon(game);
    this.dialogueFont = new Font(Gdx.files.internal("fonts/DotGothic16-Dialogue.fnt"));
    dialogueFont.adjustLineHeight(1.3f);
    this.selectChoiceFont = new Font(Gdx.files.internal("fonts/DotGothic16-Medium.fnt"));

    // Pass this controller as the DisplayHandler — runner calls back into
    // showLine() etc.
    this.runner = new DialogueRunner(this);
    this.achievementVerifier = new AchievementVerifier();

    addListeners();
    loadAndStartDay("day" + currentDay);
  }

  /**
   * Loads the data from story.json via DialogueLoader and starts the runner.
   *
   * DialogueLoader reads the JSON and returns a DayData record containing:
   * - rootNode : the first node to pass into runner.start()
   * - achievements: the list of achievements defined for this day
   *
   * Both are stored so they are available when onDialogueEnd() fires.
   *
   * NOTE: All textures referenced in story.json must already be loaded by
   * AssetManager before this method is called — typically in your LoadingScreen.
   *
   * @param dayKey The key for the day in the story_schema.json file.
   */
  private void loadAndStartDay(String dayKey) {
    // Reset player HP back to its original state (max allowed HP)
    screen.getPlayer().restoreFullHp();
    screen.updatePlayerStats();

    DayData day = DialogueLoader.load(STORY_FILE, dayKey);

    this.storyRoot = day.rootNode();
    this.achievements = day.achievements();

    // Add static centered Day label
    TextraLabel dayLabel = new TextraLabel("[#FFDB51][[DAY " + currentDay + "][]", dialogueFont);
    dayLabel.setName("dayLabel");
    dayLabel.setAlignment(Align.center);
    textDialogueTable.add(dayLabel).growX().center().padBottom(10f).row();

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

    if (node.damage > 0) {
      screen.getPlayer().takeDamage((float) node.damage);
      screen.updatePlayerStats();

      // If the damage results in death, reset progress to Day 1 and show Game Over
      if (!screen.getPlayer().isAlive()) {
        currentDay = 1;
        game.setScreen(new GameOverScreen(game));
        return;
      }
    }

    DialogueUtils.configureTypingLabel(typingLabel, game, runner::onLineFinished);

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
   * Removes old dialogue lines from the top until all content fits inside the
   * table.
   */
  private void trimDialogueOverflow() {
    pendingOverflowTrim = !DialogueUtils.trimDialogueOverflow(textDialogueTable);
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
    selectedRow = 0; // reset cursor to first option each time

    renderChoiceWidgets(node);
    screen.getStage().setKeyboardFocus(dialogueWidgetTable);
  }

  @Override
  public void showManualRoll(ManualRollNode node) {
    activeManualRollNode = node;

    if (node.texturePath != null) {
      Texture texture = game.assets.get(node.texturePath, Texture.class);
      showImageScenario(texture);
    }

    TypingLabel typingLabel = new TypingLabel(node.text, dialogueFont);
    DialogueUtils.configureTypingLabel(typingLabel, game, () -> {
      renderManualRollWidget(node);
      screen.getStage().setKeyboardFocus(dialogueWidgetTable);
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
   * Called by DialogueRunner when the next node is a MinigameNode.
   *
   * Switches to the appropriate minigame screen via game.setScreen().
   * DayScreen is hidden but NOT disposed — all dialogue state is preserved.
   * The graph resumes when the minigame screen calls runner.onMinigameFinished().
   *
   * @param node The MinigameNode containing the screenKey to launch.
   */
  @Override
  public void showMinigame(MinigameNode node) {
    if (DEV_SKIP_MINIGAMES) {
      Gdx.app.log("DayController", "[DEV MODE] Auto-skipping/winning minigame: " + node.screenKey);
      createMinigameCompletionCallback(node.screenKey).accept(true);
      return;
    }

    game.setScreen(resolveMinigameScreen(node));
  }

  /**
   * Called by DialogueRunner when the last node in the graph is reached.
   *
   * Triggers end-of-day logic: runs achievement verification and logs results.
   * Replace log statements with your actual unlock UI or save system.
   */
  @Override
  public void onDialogueEnd() {
    Gdx.app.log("DayController", "Day complete.");
    onDayEnd();
  }

  @Override
  public void forceGameEnd() {
    Gdx.app.log("DayController", "Game ending triggered. Transitioning to credits.");
    game.setScreen(new CreditsScreen(game));
  }

  @Override
  public void triggerGameOver() {
    Gdx.app.log("DayController", "Narrative Game Over triggered. Resetting progress.");
    currentDay = 1;
    // Initial stats defined as constants in Player.java
    screen.getPlayer().resetStats(
      Player.STARTING_HP, 
      Player.STARTING_STR, 
      Player.STARTING_INT, 
      Player.STARTING_DEX, 
      Player.STARTING_CHA
    );
    screen.updatePlayerStats();
    game.setScreen(new GameOverScreen(game));
  }

  /**
   * Called to transition logic to the next day.
   */
  private void onDayEnd() {
    currentDay++;
    screen.getTextDialogueTable().clearChildren(); // Clears all text

    switch (currentDay) {
      case 2 -> loadAndStartDay("day" + currentDay);
      // case 3 -> loadAndStartDay("day" + currentDay); // Re-enable when day3 exists in story_schema.json
      default -> {
        Gdx.app.log("DayController", "End of Game! Day: " + currentDay);
        game.setScreen(new CreditsScreen(game));
      }
    }
  }

  // ---------------------------------------------------------------------------
  // MINIGAME SCREEN RESOLUTION
  // ---------------------------------------------------------------------------

  /**
   * Creates the callback executed when a minigame is completed.
   * Handles returning from the minigame screen back to the DayScreen and
   * resuming the dialogue flow based on the result.
   *
   * @param screenKey The identifier for the minigame to resolve specific logic.
   * @return A Consumer accepting a boolean (success) containing the completion logic.
   */
  private Consumer<Boolean> createMinigameCompletionCallback(String screenKey) {
    // Determines whether the minigame involves entity combat, requiring a stat
    // refresh on return. Now includes cavern creature battle.
    boolean isBattleMinigame = switch (screenKey) {
      case "bandit_battle_minigame", 
           "cavern_creature_battle_minigame", 
           "centipede_battle_minigame", 
           "leviathan_battle_minigame",
           "wyvern_battle_minigame",
           "knights_battle_minigame",
           "knight_captain_battle_minigame",
           "goblin_king_battle_minigame",
           "3knights_battle_minigame" -> true;
      default -> false;
    };

    return (success) -> {
      game.setScreen(screen); // return to DayScreen — state is preserved
      setPendingOverflowTrim(true);

      if (isBattleMinigame) {
        screen.updatePlayerStats();
      }

      runner.onMinigameFinished(success); // resume dialogue graph with result
    };
  }

  /**
   * Maps a screenKey string from story.json to its actual minigame Screen.
   *
   * The minigame screen receives a Runnable callback instead of a direct
   * runner reference — it calls onComplete.run() when the player passes,
   * which returns to DayScreen and resumes the dialogue graph.
   *
   * To add a new minigame: add a new case here and create its Screen class.
   *
   * @param node The MinigameNode containing minigame configurations from JSON.
   * @return The minigame Screen to transition to.
   */
  private Screen resolveMinigameScreen(MinigameNode node) {
    Consumer<Boolean> onComplete = createMinigameCompletionCallback(node.screenKey);

    return switch (node.screenKey) {
      // Add new minigame screens here as cases
      case "maze_minigame" -> new mazeBossScreen(game, node, onComplete);
      // Both battle minigame variants use the EntityBattleScreen with specific routing via screenKey
      case "bandit_battle_minigame", 
           "cavern_creature_battle_minigame", 
           "centipede_battle_minigame", 
           "leviathan_battle_minigame",
           "wyvern_battle_minigame",
           "knights_battle_minigame",
           "knight_captain_battle_minigame",
           "goblin_king_battle_minigame",
           "3knights_battle_minigame" -> new EntityBattleScreen(game, node, screen.getPlayer(), onComplete);
      default -> throw new RuntimeException(
          "[DayController] Unknown minigame screenKey: '" + node.screenKey + "'");
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
    UIFactory.tintCell(dialogueCell1, true);

    dialogueWidgetTable.add(dialogueCell0).size(30f).padRight(20f).padBottom(20f);
    dialogueWidgetTable.add(dialogueCell1).growX().align(Align.left | Align.center).padBottom(20f).row();

    // Row 1
    TextraLabel choice1Label = new TextraLabel(node.choices.get(1).label(), selectChoiceFont);
    choice1Label.setAlignment(Align.left);
    dialogueCell3.setActor(choice1Label);
    dialogueCell3.fill();
    UIFactory.tintCell(dialogueCell3, false);

    dialogueWidgetTable.add(dialogueCell2).size(30f).padRight(20f);
    dialogueWidgetTable.add(dialogueCell3).growX().align(Align.left | Align.center);
  }

  /**
   * Renders the single prompt for a manual roll action.
   */
  private void renderManualRollWidget(ManualRollNode node) {
    TextraLabel promptLabel = new TextraLabel(node.label, selectChoiceFont);
    promptLabel.setAlignment(Align.left);

    dialogueCell0.setActor(selectChoiceIcon);
    dialogueCell1.setActor(promptLabel);
    dialogueCell1.fill();
    UIFactory.tintCell(dialogueCell1, true);

    dialogueWidgetTable.add(dialogueCell0).size(30f).padRight(20f).padBottom(20f);
    dialogueWidgetTable.add(dialogueCell1).growX().align(Align.left | Align.center).padBottom(20f).row();
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
    activeManualRollNode = null;
  }

  /**
   * Updates the visual selection state of both rows based on selectedRow.
   * Moves the arrow icon to the selected row and applies color tinting.
   */
  private void refreshSelection() {
    boolean isRow0 = (selectedRow == 0);

    // Update selection arrows
    dialogueCell0.setActor(isRow0 ? selectChoiceIcon : null);
    dialogueCell2.setActor(isRow0 ? null : selectChoiceIcon);

    // Update label tints
    UIFactory.tintCell(dialogueCell1, isRow0);
    UIFactory.tintCell(dialogueCell3, !isRow0);
  }

  /**
   * Called automatically whenever a player selection is finalized.
   * Runs an Exact Match check against all sequence-based achievements.
   */
  @Override
  public void onChoiceFinalized() {
    for (Achievement achievement : achievements) {
      // Automatic detection: Only check achievements that HAVE a sequence
      if (!achievement.requiredChoiceSequence.isEmpty()) {
        // Exact Match Logic: Path length MUST match sequence length
        if (runner.getPlayerPath().size() == achievement.requiredChoiceSequence.size()) {
          if (achievementVerifier.verify(achievement, runner.getPlayerPath(), storyRoot)) {
            Gdx.app.log("DayController", "Achievement unlocked: [" 
                + achievement.id + "] " + achievement.description);
          }
        }
      }
    }
  }

  /**
   * Called immediately when the runner reaches a LineNode tagged with an
   * obtainableAchievement. Only processes Reach-Only achievements.
   *
   * @param achievementId The achievement id declared on the LineNode.
   */
  @Override
  public void onAchievementObtainable(String achievementId) {
    Achievement target = achievements.stream()
        .filter(a -> a.id.equals(achievementId))
        .findFirst()
        .orElse(null);

    if (target == null) {
      Gdx.app.error("DayController",
          "obtainableAchievement '" + achievementId + "' not found in day achievements list.");
      return;
    }

    // Manual detection: Only check achievements that DO NOT have a sequence (Reach-Only)
    if (target.requiredChoiceSequence.isEmpty()) {
      if (achievementVerifier.verify(target, runner.getPlayerPath(), storyRoot)) {
        Gdx.app.log("DayController", "Achievement unlocked: [" + target.id + "] " + target.description);
      }
    }
  }

  @Override
  public void onCharismaIncreased(int amount) {
    screen.getPlayer().increaseCharisma(amount);
    screen.updatePlayerStats();
  }

  @Override
  public Player getPlayer() {
    return screen.getPlayer();
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
        Gdx.app.log("DayController", "Settings clicked!");
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
        if (pointer == -1)
          settingsIcon.setColor(Color.valueOf("#c7c7c7"));
        super.enter(event, x, y, pointer, fromActor);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (pointer == -1)
          settingsIcon.setColor(Color.WHITE);
        super.exit(event, x, y, pointer, toActor);
      }
    });
  }

  /**
   * Adds keyboard navigation and confirmation to the dialogue widget table.
   *
   * UP/DOWN moves the cursor between choice rows.
   * ENTER confirms the highlighted choice:
   * 1. Clears the choice UI from the screen.
   * 2. Calls runner.onChoiceSelected(selectedRow) which records the decision
   * in PlayerPathTracker and advances the graph to the chosen branch.
   *
   * Input is only processed when activeChoiceNode is not null, ensuring
   * keyboard events during normal dialogue lines are safely ignored.
   */
  private void addChoiceInputListener() {
    dialogueWidgetTable.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Input.Keys.UP && activeChoiceNode != null) {
          selectedRow = Math.max(0, selectedRow - 1);
          refreshSelection();
          return true;
        }

        if (keycode == Input.Keys.DOWN && activeChoiceNode != null) {
          selectedRow = Math.min(1, selectedRow + 1);
          refreshSelection();
          return true;
        }

        if (keycode == Input.Keys.ENTER) {
          if (activeChoiceNode != null) {
            clearChoiceWidgets();
            runner.onChoiceSelected(selectedRow);
            return true;
          } else if (activeManualRollNode != null) {
            clearChoiceWidgets();
            runner.onManualRollExecuted();
            return true;
          }
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
   * Must be called from DayScreen.dispose() to prevent memory leaks.
   * Fonts are not managed by AssetManager so they require manual disposal.
   * Textures are NOT disposed here — AssetManager owns and manages them.
   */
  public void dispose() {
    dialogueFont.dispose();
    selectChoiceFont.dispose();
  }
}
