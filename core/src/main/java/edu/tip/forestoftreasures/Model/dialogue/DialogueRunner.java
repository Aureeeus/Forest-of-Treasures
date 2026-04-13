package edu.tip.forestoftreasures.Model.dialogue;

import edu.tip.forestoftreasures.Model.entities.Player;

import java.util.List;

import edu.tip.forestoftreasures.Model.PlayerPathTracker;
import edu.tip.forestoftreasures.Model.PlayerPathTracker.PlayerDecision;
import edu.tip.forestoftreasures.Model.mechanics.Dice;

public class DialogueRunner {

  /**
   * Callback interface that connects the runner to the actual LibGDX UI.
   */
  public interface DisplayHandler {

    /**
     * Renders a line of dialogue and its optional background image.
     *
     * @param node The LineNode containing the text and texture to display.
     */
    public void showLine(LineNode node);

    /**
     * Renders the player choice UI and pauses auto-advance.
     *
     * @param node The ChoiceNode containing the list of choices to display.
     */
    public void showChoices(ChoiceNode node);

    /**
     * Renders a manual prompt that requires player interaction to perform a D20 roll check.
     *
     * @param node The ManualRollNode containing the text, texture, and descriptive label.
     */
    public void showManualRoll(ManualRollNode node);

    /**
     * Launches the minigame screen and pauses the dialogue graph.
     * The graph resumes only after onMinigameFinished() is called.
     *
     * @param node The MinigameNode containing the screenKey to launch.
     */
    void showMinigame(MinigameNode node);

    /**
     * Called when a player has selected and finalized a choice.
     * Use this for automatic achievement detection after a decision is made.
     */
    void onChoiceFinalized();

    /**
     * Called when a LineNode with an obtainableAchievement is reached.
     * Fires before showLine() so the achievement is verified immediately.
     *
     * @param achievementId The achievement id declared on the LineNode.
     */
    void onAchievementObtainable(String achievementId);

    /**
     * Called when the entire dialogue graph has been fully traversed.
     * Use this to trigger end-of-chapter logic.
     */
    public void onDialogueEnd();

    /**
     * Called when the game should end via narrative trigger.
     * Transitions the game to the credits/ending screen.
     */
    public void forceGameEnd();

    /**
     * Called when the game should trigger a game over via narrative trigger.
     * Transitions the game to the game over screen and resets player data.
     */
    public void triggerGameOver();

    /**
     * Called when a node or choice grants a charisma increase.
     * The handler should apply the increase to the player and refresh the UI.
     *
     * @param amount The charisma points to add.
     */
    void onCharismaIncreased(int amount);

    /**
     * Provides the current Player instance for stat evaluation nodes.
     * Called when the runner encounters an {@link EvaluateStatsNode}
     * and needs access to the player's stats for threshold comparison.
     *
     * @return The active Player instance.
     */
    Player getPlayer();
  }

  // --- Instance variables ---
  private DialogueNode current;
  private final PlayerPathTracker pathTracker = new PlayerPathTracker();
  private final DisplayHandler displayHandler;

  /**
   * Constructs a DialogueRunner with the given display handler.
   *
   * @param displayHandler The UI implementation that renders lines and choices.
   */
  public DialogueRunner(DisplayHandler displayHandler) {
    this.displayHandler = displayHandler;
  }
  
  public void start(DialogueNode node) {
    pathTracker.reset();
    this.current = node;
    step();
  }

  /**
   * Resumes the dialogue graph from a previously saved node without
   * resetting the path tracker. Used when loading a saved game.
   *
   * @param node The node to resume from.
   */
  public void resumeFrom(DialogueNode node) {
    this.current = node;
    step();
  }

  /**
   * Returns the JSON id of the current dialogue node, or null if
   * the dialogue has ended.
   *
   * @return The current node's id string, or null.
   */
  public String getCurrentNodeId() {
    return current != null ? current.getId() : null;
  }

  /**
   * Inspects the current node and dispatches it to the appropriate display method.
   *
   * Core routing logic:
   *   - null          → dialogue is over, notify the handler.
   *   - LineNode      → display the text line.
   *   - ChoiceNode    → display the choice UI and pause.
   *   - MinigameNode  → launch the minigame screen and pause.
   *
   * Adding a new node type only requires a new branch here.
   */
  private void step() {
    if (current == null) {
      displayHandler.onDialogueEnd();
      return;
    }

    if (current instanceof AutomaticRollNode roll) {
      current = roll.evaluate();
      step();
      return;
    }

    if (current instanceof EvaluateStatsNode eval) {
      current = eval.evaluate(displayHandler.getPlayer());
      step();
      return;
    }

    if (current instanceof LineNode line) {
      if (line.obtainableAchievement != null) {
        displayHandler.onAchievementObtainable(line.obtainableAchievement);
      }
      if (line.increaseCharisma != null) {
        displayHandler.onCharismaIncreased(line.increaseCharisma);
      }
      displayHandler.showLine(line);
    } else if (current instanceof ChoiceNode choice) {
      displayHandler.showChoices(choice);
    } else if (current instanceof MinigameNode minigame) {
      displayHandler.showMinigame(minigame);
    } else if (current instanceof ManualRollNode manualRoll) {
      displayHandler.showManualRoll(manualRoll);
    }
  }


  /**
   * Advances to the next node after a line finishes displaying.
   * If the finished line has triggerGameEnd set, the game ends immediately.
   */
  public void onLineFinished() {
    if (current instanceof LineNode line) {
      if (line.triggerGameEnd) {
        displayHandler.forceGameEnd();
        return;
      }
      if (line.triggerGameOver) {
        displayHandler.triggerGameOver();
        return;
      }
    }
    current = current.getNext();
    step();
  }

  /**
   * Resolves a ChoiceNode by selecting a branch based on the player's input.
   *
   * Records the decision in the path tracker before advancing, so the
   * full history remains accurate even if the game ends immediately after.
   *
   * @param index The 0-based index of the choice the player selected.
   */
  public void onChoiceSelected(int index) {
    ChoiceNode choiceNode = (ChoiceNode) current;
    ChoiceNode.Choice selected = choiceNode.choices.get(index);

    pathTracker.record(choiceNode, index);  // record before advancing
    displayHandler.onChoiceFinalized();     // notify handler for auto-achievement detection

    if (selected.increaseCharisma() != null) {
      displayHandler.onCharismaIncreased(selected.increaseCharisma());
    }

    current = selected.next();
    step();
  }

  /**
   * Evaluates the manual roll triggered via the ENTER key, triggering dice outcome and branching appropriately.
   */
  public void onManualRollExecuted() {
    ManualRollNode manualRollNode = (ManualRollNode) current;
    int rollValue = Dice.roll();
    current = manualRollNode.evaluate(rollValue);
    step();
  }

  public List<PlayerDecision> getPlayerPath() {
    return pathTracker.getPath();
  }

  /**
   * Advances the graph after the player completes a minigame.
   *
   * Called by the minigame screen once the minigame ends.
   *
   * @param success Whether the player passed or failed the minigame.
   */
  public void onMinigameFinished(boolean success) {
    if (success) {
      current = current.getNext();
    } else if (current instanceof MinigameNode minigame && minigame.getFailNext() != null) {
      current = minigame.getFailNext();
    } else {
      // DEFAULT: Transition to Game Over handled by screens if not success 
      // and no failNext provided. But if we are here, we just notify the handler.
      displayHandler.triggerGameOver();
      return;
    }
    step();
  }
}
