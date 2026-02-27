package edu.tip.forestoftreasures.Model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import edu.tip.forestoftreasures.Model.PlayerPathTracker.PlayerDecision;

public class AchievementVerifier {
  /**
   * Internal BFS state representing a snapshot of traversal at a given moment.
   *
   * @param node       The current node being visited in the BFS traversal.
   * @param choicePath The ordered list of choice indices taken to reach this node.
   */
  private record BfsState(DialogueNode node, List<Integer> choicePath) {}

  // --- Static Variables ---
  // Wildcard value in requiredChoiceSequence — matches any player choice at this position.
  private static final int WILDCARD = -1; 

  private List<List<Integer>> bfsAllPaths(DialogueNode root) {
    List<List<Integer>> completePaths = new ArrayList<>();
    Queue<BfsState> queue = new ArrayDeque<>();
    
    // Add root as the first element of the queue
    queue.add(new BfsState(root, new ArrayList<>()));


    while (!queue.isEmpty()) {
      BfsState state = queue.poll();
      DialogueNode current = state.node();
      List<Integer> currentPath = state.choicePath();
      
      // Null node = end of a story branch; record this as a complete path
      if (current == null) {
        completePaths.add(currentPath);
        continue;
      }

      if (current instanceof LineNode line) {
        // No branching — follow the chain with the same path unchanged
        queue.add(new BfsState(line.getNext(), currentPath));
      } else if (current instanceof ChoiceNode choice) {
        // Branch — create an independent new state for each available choice
        for (int i = 0; i < choice.choices.size(); i++) {
          List<Integer> branchedPath = new ArrayList<>(currentPath); // copy to isolate branches
          branchedPath.add(i);
          queue.add(new BfsState(choice.choices.get(i).next(), branchedPath));
        }
      } else if (current instanceof MinigameNode minigame) {
        // No branching — follow the chain with the same path unchanged
        queue.add(new BfsState(minigame.getNext(), currentPath));
      }
    }

    return completePaths;
  }

  /**
   * Checks whether a target list starts with the exact elements of a required sequence.
   *
   * This is a strict prefix match — every element of the required sequence must
   * align exactly with the target starting from index 0. No gaps or skipping allowed.
   *
   * @param required The exact sequence of choices required by the achievement.
   * @param target   The player's or BFS path to check against.
   * @return true if target starts with every element of required in exact order.
   */
  private boolean isPrefixMatch(List<Integer> required, List<Integer> target) {
    // Player's path size should be greater than the achivement path size
    if (target.size() < required.size()) return false;

    for (int i = 0; i < required.size(); i++) {
      // Ignore wildcards
      if (required.get(i).intValue() == WILDCARD) continue;

      if (!required.get(i).equals(target.get(i))) return false;
    }

    return true;
  }

/**
   * BFS collects all complete paths, then checks if at least one of them starts
   * with the achievement's required sequence via prefix match. If none do, the
   * achievement is unreachable — likely due to a typo or a broken node link.
   *
   * This acts as a safeguard so invalid achievements never reach the player check.
   *
   * @param achievement The achievement definition to validate.
   * @param root        The entry node of the dialogue graph.
   * @return true if at least one reachable BFS path starts with the achievement's
   *         required sequence.
   */
  public boolean isAchievementReachable(Achievement achievement, DialogueNode root) {
    List<List<Integer>> allPaths = bfsAllPaths(root);

    return allPaths.stream()
      .anyMatch(path -> isPrefixMatch(achievement.requiredChoiceSequence, path));
  }

  /**
   * Verifies whether the player has actually unlocked a given achievement.
   *
   * @param achievement The achievement to verify.
   * @param playerPath  The player's full recorded decision history.
   * @param root        The entry node of the dialogue graph (used for BFS validation).
   * @return true if the achievement is reachable AND the player's path starts
   *         with the required sequence exactly.
   */
  public boolean verify(Achievement achievement, List<PlayerDecision> playerPath, DialogueNode root) {
    // Reject achievements that are not reachable in the graph
    if (!isAchievementReachable(achievement, root)) {
      System.err.println("[AchievementVerifier] WARNING: Achievement '" + achievement.id
          + "' is unreachable in the current graph. Check your required choice sequence.");
        return false;
    }

    // Extract the player's raw choice indices in order
    List<Integer> playerChoiceIndices = playerPath.stream()
      .map(PlayerDecision::choiceIndex)
      .collect(Collectors.toList());

    // Player's path must start with the achievement's required sequence exactly
    return isPrefixMatch(achievement.requiredChoiceSequence, playerChoiceIndices);
  }

  /**
   * Batch-checks all defined achievements and returns only the ones the player
   * has unlocked.
   *
   * @param allAchievements The complete list of achievement definitions for this chapter.
   * @param playerPath      The player's full recorded decision history.
   * @param root            The entry node of the dialogue graph (used for BFS validation).
   * @return A list of achievements the player has earned. Empty list if none qualify.
   */
  public List<Achievement> getUnlockedAchievements(
    List<Achievement> allAchievements, 
    List<PlayerDecision> playerPath, 
    DialogueNode root
  ) {
      return allAchievements.stream()
        .filter(achievement -> verify(achievement, playerPath, root))
        .collect(Collectors.toList());
  }
}
