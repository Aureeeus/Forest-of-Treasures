package edu.tip.forestoftreasures.Model.dialogue;

public abstract class DialogueNode {

  /** Unique identifier from JSON, used for save/load position tracking. */
  protected String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the next node to transition to after this node is done.
   *
   * For LineNodes, this is the node set via then().
   * For ChoiceNodes, this returns null because the next node is determined
   * by the player's input — not automatically.
   *
   * @return The next DialogueNode, or null if this is a terminal/choice node.
   */
  public abstract DialogueNode getNext();
}


  
