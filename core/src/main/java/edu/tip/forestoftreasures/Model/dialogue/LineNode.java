package edu.tip.forestoftreasures.Model.dialogue;

/**
 * Represents a single line of dialogue in the story's narrative flow.
 * LineNodes are the primary building blocks of the story, capable of displaying text,
 * changing backgrounds, dealing damage to the player, granting achievements,
 * and increasing player stats like charisma.
 *
 * <p>They follow a sequential pattern and can be linked to other nodes
 * using the {@link #then(DialogueNode)} method.</p>
 */
public class LineNode extends DialogueNode {
  /** The dialogue string to be displayed to the player. Supports TextraTypist tags. */
  public final String text;

  /** Asset path of the background image to display, or null to retain the current image. */
  public final String texturePath;

  /** HP points to deduct from the player when this line is reached. 0 for no damage. */
  public final int damage;

  /** If true, the game transitions to the credits/ending screen after this line. */
  public final boolean triggerGameEnd;

  /** If true, the game transitions to the game over screen after this line and resets progress. */
  public final boolean triggerGameOver;

  /** The ID of an achievement to check for when this line is reached. Null for none. */
  public final String obtainableAchievement;

  /** Points to add to the player's charisma stat. Null if no change occurs. */
  public final Integer increaseCharisma;

  /** The node to transition to after this line finishes typing/playing. */
  private DialogueNode next;

  /**
   * Constructs a fully-configured LineNode.
   *
   * @param text                  The narrative text to display.
   * @param texturePath           The asset path for the background (e.g., "scenarios/forest.png").
   * @param damage                Amount of HP the player loses upon reaching this node.
   * @param triggerGameEnd        Whether this is the final line of the game.
   * @param triggerGameOver       Whether this line leads to a game over.
   * @param obtainableAchievement The ID of an achievement to verify.
   * @param increaseCharisma      Amount to increment the player's charisma stat.
   */
  public LineNode(String text, String texturePath, int damage, boolean triggerGameEnd, boolean triggerGameOver,
                  String obtainableAchievement, Integer increaseCharisma) {
    this.text = text;
    this.texturePath = texturePath;
    this.damage = damage;
    this.triggerGameEnd = triggerGameEnd;
    this.triggerGameOver = triggerGameOver;
    this.obtainableAchievement = obtainableAchievement;
    this.increaseCharisma = increaseCharisma;
  }

  /**
   * Constructs a simple LineNode for standard dialogue without side effects.
   * Default values: 0 damage, no game end, no game over, no achievement, and no charisma gain.
   *
   * @param text        The narrative text to display.
   * @param texturePath The asset path for the background, or null to retain current.
   */
  public LineNode(String text, String texturePath) {
    this(text, texturePath, 0, false, false, null, null);
  }
  
  /**
   * Links this node to the next step in the story sequence.
   * This is typically used during dialogue loading to build the story graph.
   *
   * @param next The DialogueNode that follows this one.
   * @return This LineNode instance, allowing for builder-style method chaining.
   */
  public LineNode then(DialogueNode next) {
    this.next = next;
    return this;  
  }

  /**
   * Retrieves the next node in the narrative sequence.
   *
   * @return The next {@link DialogueNode}, or {@code null} if this is a terminal node.
   */
  @Override
  public DialogueNode getNext() { 
    return next;
  }
}
