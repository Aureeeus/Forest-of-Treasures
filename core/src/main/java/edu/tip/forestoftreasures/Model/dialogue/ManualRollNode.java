package edu.tip.forestoftreasures.Model.dialogue;

public class ManualRollNode extends DialogueNode {
  public final String text;
  public final String texturePath;
  public final String label;
  public final int threshold;
  
  private DialogueNode successNext;
  private DialogueNode failNext;

  public ManualRollNode(String text, String texturePath, String label, int threshold) {
    this.text = text;
    this.texturePath = texturePath;
    this.label = label;
    this.threshold = threshold;
  }

  public void setNexts(DialogueNode successNext, DialogueNode failNext) {
    this.successNext = successNext;
    this.failNext = failNext;
  }

  /**
   * Evaluates the D20 roll triggered by the player and returns the outcome branch.
   *
   * @param roll the randomized 1-20 value
   * @return The next node reference based on threshold check.
   */
  public DialogueNode evaluate(int roll) {
    if (roll >= threshold) {
      return successNext;
    } else {
      return failNext;
    }
  }

  @Override
  public DialogueNode getNext() {
    return null; // Handled directly via DisplayHandler and User Input (ENTER)
  }
}
