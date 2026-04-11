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
  
  /** 
   * Optional manual override for the blessing mechanic. 
   * If true, player receives HIGH blessing (heals every battle) without a roll. 
   */
  public final Boolean healBlessing;
  
   /** The node to transition to after the player passes the minigame. */
  private DialogueNode next;

  /** The node to transition to if the player fails the minigame. */
  private DialogueNode failNext;


  /**
   * Constructs a MinigameNode with the given configuration.
   *
   * @param screenKey    Identifier string for the minigame screen to launch.
   * @param playerTurn   Nullable boolean to force who gets the first move.
   * @param healBlessing Nullable boolean to force the blessing tier to HIGH.
   */
  public MinigameNode(String screenKey, Boolean playerTurn, Boolean healBlessing) {
    this.screenKey = screenKey;
    this.playerTurn = playerTurn;
    this.healBlessing = healBlessing;
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
   * Returns the next node after the minigame is completed successfully.
   * Returns null if then() was never called (terminal node).
   *
   * @return The next DialogueNode, or null.
   */
  @Override
  public DialogueNode getNext() {
    return next;
  }

  /**
   * Links this node to the failure node in the graph after the minigame ends in a loss.
   *
   * @param failNext The node to transition to if the minigame is failed.
   * @return This MinigameNode instance, for method chaining.
   */
  public MinigameNode linkFailNext(DialogueNode failNext) {
    this.failNext = failNext;
    return this;
  }

  /**
   * Returns the node to transition to if the minigame is failed.
   *
   * @return The failNext DialogueNode, or null.
   */
  public DialogueNode getFailNext() {
    return failNext;
  }
}
