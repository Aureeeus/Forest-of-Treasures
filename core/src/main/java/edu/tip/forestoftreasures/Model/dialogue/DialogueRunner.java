package edu.tip.forestoftreasures.Model.dialogue;

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
     * Called when a node or choice grants a charisma increase.
     * The handler should apply the increase to the player and refresh the UI.
     *
     * @param amount The charisma points to add.
     */
    void onCharismaIncreased(int amount);
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
      step(); // instantly transition
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
    if (current instanceof LineNode line && line.triggerGameEnd) {
      displayHandler.forceGameEnd();
      return;
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
   * Advances the graph after the player successfully passes a minigame.
   *
   * Called by the minigame screen once the win condition is met.
   * Transitions to the node linked via MinigameNode.then().
   */
  public void onMinigameFinished() {
    current = current.getNext();
    step();
  }
}
