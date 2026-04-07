package edu.tip.forestoftreasures.Model.dialogue;

import edu.tip.forestoftreasures.Model.mechanics.Dice;

/**
 * An automatic, silent background node that performs a D20 roll check.
 * This node instantly transitions to the success or failure branch based on the threshold.
 */
public class AutomaticRollNode extends DialogueNode {
  private final int threshold;
  private DialogueNode successNext;
  private DialogueNode failNext;

  public AutomaticRollNode(int threshold) {
    this.threshold = threshold;
  }

  public void setNexts(DialogueNode successNext, DialogueNode failNext) {
    this.successNext = successNext;
    this.failNext = failNext;
  }

  /**
   * Evaluates the automatic roll immediately without UI pausing.
   * Rolls a D20, and branching relies on the threshold.
   *
   * @return The next node to instantly transition into.
   */
  public DialogueNode evaluate() {
    int roll = Dice.roll();
    if (roll >= threshold) {
      return successNext;
    } else {
      return failNext;
    }
  }

  @Override
  public DialogueNode getNext() {
    return null; // Handled directly by DialogueRunner evaluation
  }
}
