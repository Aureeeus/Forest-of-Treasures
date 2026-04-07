package edu.tip.forestoftreasures.Model.mechanics;

/**
 * Represents the five damage tiers in the d20 battle system.
 * Each tier maps to a range of dice rolls and a damage multiplier
 * applied to the attacker's base stat.
 *
 * <pre>
 *   Roll 1      → MISS           (0%)
 *   Roll 2–9    → HALF           (50%)
 *   Roll 10     → THREE_QUARTER  (75%)
 *   Roll 11–19  → FULL           (100%)
 *   Roll 20     → CRITICAL       (200%)
 * </pre>
 */
public enum DamageTier {
  MISS(0f),
  HALF(0.5f),
  THREE_QUARTER(0.75f),
  FULL(1.0f),
  CRITICAL(2.0f);

  public final float multiplier;

  DamageTier(float multiplier) {
    this.multiplier = multiplier;
  }

  /**
   * Resolves a d20 dice roll into the corresponding damage tier.
   *
   * @param roll the dice result (1–20)
   * @return the DamageTier for that roll
   * @throws IllegalArgumentException if roll is outside [1, 20]
   */
  public static DamageTier fromDiceRoll(int roll) {
    if (roll < 1 || roll > 20) {
      throw new IllegalArgumentException("Dice roll must be between 1 and 20, got: " + roll);
    }

    return switch (roll) {
      case 1          -> MISS;
      case 10         -> THREE_QUARTER;
      case 20         -> CRITICAL;
      default         -> (roll <= 9) ? HALF : FULL;
    };
  }
}
