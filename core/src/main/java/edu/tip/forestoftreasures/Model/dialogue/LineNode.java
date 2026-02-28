package edu.tip.forestoftreasures.Model.dialogue;

public class LineNode extends DialogueNode {
  public final String text;
  public final String texturePath; // null, to retain current image scenario
  private DialogueNode next;

  /**
   * Constructs a new LineNode with the given text and optional texture path.
   *
   * @param text        The dialogue string to display.
   * @param texturePath Asset path of the background image, or null to keep current.
   */
  public LineNode(String text, String texturePath) {
    this.text = text;
    this.texturePath = texturePath;
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
