package edu.tip.forestoftreasures.Model.entities;

/**
 * Status effects that can be applied to an entity during battle.
 * Each effect triggers at the start of the afflicted entity's turn.
 *
 * <ul>
 *   <li>BURN — deals 1/16th of max HP per turn</li>
 *   <li>POISON — deals 1/32nd of max HP per turn</li>
 *   <li>SLEEP — forces the entity to skip their turn</li>
 * </ul>
 */
public enum StatusEffect {
  BURN,
  POISON,
  SLEEP;

  /**
   * Processes this status effect on the afflicted entity.
   * Called at the start of the entity's turn by the battle controller.
   *
   * @param entity the entity afflicted by this status
   * @return {@code true} if the entity's turn is skipped (SLEEP), {@code false} otherwise
   */
  public boolean applyPerTurn(Entity entity) {
    switch (this) {
      case BURN -> entity.takeDamage(entity.getMaxHp() / 16f);
      case POISON -> entity.takeDamage(entity.getMaxHp() / 32f);
      case SLEEP -> { return true; }
    }
    return false;
  }

  /**
   * Returns a random status effect (BURN, POISON, or SLEEP).
   *
   * @return a randomly selected StatusEffect
   */
  public static StatusEffect random() {
    StatusEffect[] values = values();
    int index = java.util.concurrent.ThreadLocalRandom.current().nextInt(values.length);
    return values[index];
  }
}
