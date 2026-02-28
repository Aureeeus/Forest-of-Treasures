package edu.tip.forestoftreasures.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.tip.forestoftreasures.Model.dialogue.ChoiceNode;


public class PlayerPathTracker {

  /**
   * Immutable record of a single decision made by the player during dialogue.
   *
   * Stores which ChoiceNode the decision was made at, and which index (0-based)
   * the player selected. This is the atomic unit of the player's story path.
   *
   * @param node        The ChoiceNode where the decision was made.
   * @param choiceIndex The 0-based index of the option the player selected.
   */
  public record PlayerDecision(ChoiceNode node, int choiceIndex) {}

  // --- Instance variables ---
  private final List<PlayerDecision> path = new ArrayList<>();

  /**
   * Clears all recorded decisions.
   *
   * Useful when restarting a chapter or the full game, so the tracker
   * starts fresh without needing to create a new instance.
   */
  public void reset() {
    path.clear();
  }

  /**
   * Records a player's decision at a given ChoiceNode.
   *
   * @param node        The ChoiceNode the decision was made at.
   * @param choiceIndex The index of the choice the player selected.
   */
  public void record(ChoiceNode choiceNode, int choiceIndex) {
    path.add(new PlayerDecision(choiceNode, choiceIndex));
  }

  /**
   * Returns the player's full decision path as an unmodifiable list.
   *
   * @return An unmodifiable view of the player's decision history.
   */
  public List<PlayerDecision> getPath() {
    return Collections.unmodifiableList(path);
  }
}
