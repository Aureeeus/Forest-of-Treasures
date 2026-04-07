package edu.tip.forestoftreasures.Model.dialogue;

import java.util.List;

public class ChoiceNode extends DialogueNode {
  public record Choice(String label, DialogueNode next) {};

  public List<Choice> choices;

  public ChoiceNode(Choice... choices) {
    this.choices = List.of(choices);
  }

  public void setChoices(Choice... choices) {
    this.choices = List.of(choices);
  }

  /**
   * Always returns null because a ChoiceNode does not auto-advance.
   *
   * @return Always null.
   */
  @Override
  public DialogueNode getNext() {
    return null;
  }
}
