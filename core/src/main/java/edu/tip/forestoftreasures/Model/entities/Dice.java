package edu.tip.forestoftreasures.Model.entities;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates a 20-sided die (d20) used as the core randomizer
 * for the battle system (e.g., initiative rolls, hit checks).
 */
public class Dice {
  private static final int MAX_FACE = 20;

  /**
   * Rolls the d20 and returns a random value between 1 and 20 (inclusive).
   *
   * @return a random integer in the range [1, 20]
   */
  public static int roll() {
    return ThreadLocalRandom.current().nextInt(1, MAX_FACE + 1);
  }
}
