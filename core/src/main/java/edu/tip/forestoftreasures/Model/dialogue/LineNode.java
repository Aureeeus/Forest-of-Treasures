package edu.tip.forestoftreasures.Model.dialogue;

public class LineNode extends DialogueNode {
  public final String text;
  public final String texturePath; // null, to retain current image scenario
  public final int damage;
  public final boolean triggerGameEnd;
  public final String obtainableAchievement; // null unless this line triggers an achievement check
  private DialogueNode next;

  /**
   * Constructs a new LineNode with the given text, optional texture path, damage,
   * game-end flag, and optional achievement trigger.
   *
   * @param text                  The dialogue string to display.
   * @param texturePath           Asset path of the background image, or null to keep current.
   * @param damage                HP deduction if applicable (0 for none).
   * @param triggerGameEnd        If true, transitions to the credits screen after this line finishes.
   * @param obtainableAchievement Achievement id to verify immediately upon reaching this node, or null.
   */
  public LineNode(String text, String texturePath, int damage, boolean triggerGameEnd,
                  String obtainableAchievement) {
    this.text = text;
    this.texturePath = texturePath;
    this.damage = damage;
    this.triggerGameEnd = triggerGameEnd;
    this.obtainableAchievement = obtainableAchievement;
  }

  /**
   * Constructs a new LineNode with default 0 damage, no game-end trigger, and no achievement.
   */
  public LineNode(String text, String texturePath) {
    this(text, texturePath, 0, false, null);
  }
  
  /**
   * Links this node to the next node in the story sequence.
   *
   * Keeping next private enforces encapsulation; the only way to set it
   * is through this controlled method.
   *
   * @param next The node that follows this one.
   * @return This LineNode instance, for method chaining.
   */
  public LineNode then(DialogueNode next) {
    this.next = next;
    return this;  
  }

  /**
   * Returns the next node this line transitions to after it finishes.
   * Will be null if then() was never called (terminal node).
   *
   * @return The next DialogueNode, or null if this is the last line.
   */
  @Override
  public DialogueNode getNext() { return next;}
}
