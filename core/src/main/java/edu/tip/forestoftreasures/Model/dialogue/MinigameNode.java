package edu.tip.forestoftreasures.Model.dialogue;

public class MinigameNode extends DialogueNode {
  /**
   * String identifier for which minigame screen to launch.
   * Matched against cases in DayController.resolveMinigameScreen().
   */
  public final String screenKey;
  
  /** 
   * Optional manual override for initiative order. 
   * If true, player goes first. If false, enemy goes first. 
   * If null, uses the default random dice roll. 
   */
  public final Boolean playerTurn;
  
   /** The node to transition to after the player passes the minigame. */
  private DialogueNode next;


  /**
   * Constructs a MinigameNode with the given screen key and turn override.
   *
   * @param screenKey Identifier string for the minigame screen to launch.
   * @param playerTurn Nullable boolean to force who gets the first move.
   */
  public MinigameNode(String screenKey, Boolean playerTurn) {
    this.screenKey = screenKey;
    this.playerTurn = playerTurn;
  }

  /**
   * Links this node to the next node in the graph after the minigame ends.
   *
   * @param next The node to transition to once the minigame is passed.
   * @return This MinigameNode instance, for method chaining.
   */
  public MinigameNode then(DialogueNode next) {
    this.next = next;
    return this;
  }

  /**
   * Returns the next node after the minigame is completed.
   * Returns null if then() was never called (terminal node).
   *
   * @return The next DialogueNode, or null.
   */
  @Override
  public DialogueNode getNext() {
    return next;
  }
}
