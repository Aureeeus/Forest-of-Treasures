package edu.tip.forestoftreasures.Model.dialogue;

import edu.tip.forestoftreasures.Model.entities.Player;

/**
 * An automatic, silent background node that evaluates the player's charisma stat
 * against a threshold. If charisma >= threshold, the success branch is taken;
 * otherwise the failure branch is taken.
 *
 * Structurally mirrors {@link AutomaticRollNode} but replaces the dice roll
 * with a deterministic stat check, making progression dependent on accumulated
 * charisma from prior dialogue choices.
 */
public class EvaluateStatsNode extends DialogueNode {
  private final int threshold;
  private DialogueNode successNext;
  private DialogueNode failNext;

  public EvaluateStatsNode(int threshold) {
    this.threshold = threshold;
  }

  public void setNexts(DialogueNode successNext, DialogueNode failNext) {
    this.successNext = successNext;
    this.failNext = failNext;
  }

  /**
   * Evaluates the player's charisma against this node's threshold.
   *
   * @param player The player whose charisma stat is checked.
   * @return The success branch if charisma >= threshold, otherwise the failure branch.
   */
  public DialogueNode evaluate(Player player) {
    return player.getCharisma() >= threshold ? successNext : failNext;
  }

  @Override
  public DialogueNode getNext() {
    return null; // Handled directly by DialogueRunner evaluation
  }
}
