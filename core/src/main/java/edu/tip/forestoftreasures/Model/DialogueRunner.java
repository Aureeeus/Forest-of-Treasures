package edu.tip.forestoftreasures.Model;

import java.util.List;

import edu.tip.forestoftreasures.Model.PlayerPathTracker.PlayerDecision;

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
     * Called when the entire dialogue graph has been fully traversed.
     * Use this to trigger end-of-chapter logic.
     */
    public void onDialogueEnd();
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
   * <p>Adding a new node type (e.g. CutsceneNode) only requires adding
   * a new branch here — nothing else changes.</p>
   */
  private void step() {
    if (current == null) {
      displayHandler.onDialogueEnd();
      return;
    }

    if (current instanceof LineNode line) {
      displayHandler.showLine(line);
    } else if (current instanceof ChoiceNode choice) {
      displayHandler.showChoices(choice);
    }
  }


  /**
   * Advances to the next node after a line finishes displaying.
   *
   */
  public void onLineFinished() {
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
    pathTracker.record(choiceNode, index);  // record before advancing
    current = choiceNode.choices.get(index).next();
    step();
  }

  public List<PlayerDecision> getPlayerPath() {
    return pathTracker.getPath();
  }
}
