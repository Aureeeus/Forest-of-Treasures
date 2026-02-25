package edu.tip.forestoftreasures.Model;

import java.util.List;

public class Achievement {
  // --- Instance Variables ---
  public final String id;
  public final String description;
  public final List<Integer> requiredChoiceSequence;

  /**
   * Constructs a new Achievement definition.
   *
   * @param id                     Unique identifier string for this achievement.
   * @param description            Display text shown to the player when unlocked.
   * @param requiredChoiceSequence Ordered list of 0-based choice indices required,
   *                               matched as an exact prefix against the player's path.
   */
  public Achievement(String id, String description, List<Integer> requiredChoiceSequence) {
    this.id = id;
    this.description = description;
    this.requiredChoiceSequence = List.copyOf(requiredChoiceSequence); // immutable defensive copy
  }
}
