package edu.tip.forestoftreasures.Model.entities;

import com.badlogic.gdx.graphics.Texture;

/**
 * A bandit enemy encountered during battle sequences.
 * Uses strength as base damage and rolls a d20 to determine damage tier.
 */
public class Bandit extends Entity {
  private final Texture texture;

  public Bandit(float initiative, Texture texture) {
    super(50f, 14f, initiative);
    this.texture = texture;
  }

  public Texture getTexture() {
    return texture;
  }

  /**
   * Executes the Bandit's melee attack against a target.
   * Rolls the dice, applies the damage tier multiplier to strength,
   * and deals damage.
   *
   * @param target the entity being attacked
   * @return the final damage dealt
   */
  @Override
  public float attack(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = strength * tier.multiplier;
    target.takeDamage(damage);
    return damage;
  }

  /**
   * Executes the Bandit's attack with flavor text for UI display.
   * Same dice-roll logic as {@link #attack(Entity)}, but returns
   * an {@link AttackResult} with tier-appropriate narrative text.
   *
   * @param target the entity being attacked
   * @return an {@link AttackResult} containing dice roll, tier, damage, and flavor text
   */
  public AttackResult attackWithFlavor(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = strength * tier.multiplier;
    target.takeDamage(damage);

    String flavorText = switch (tier) {
      case MISS          -> "The Bandit overextends with a clumsy swing! Their blade bites only empty air. {COLOR=RED}[[0%]{ENDCOLOR}";
      case HALF          -> "A glancing blow. The Bandit's rusted dagger scrapes against your wards. {COLOR=#AEE2FF}[[50%]{ENDCOLOR}";
      case THREE_QUARTER -> "\"Keep still, scholar!\" The Bandit lunges, drawing a thin line of red across your arm. {COLOR=#15A3C7}[[75%]{ENDCOLOR}";
      case FULL          -> "A precise strike! The Bandit finds a gap in your robes and sinks their blade deep. {COLOR=#66FF00}[[100%]{ENDCOLOR}";
      case CRITICAL      -> "{COLOR=#FFDB51}CRITICAL!{ENDCOLOR} \"Found your heart!\" The Bandit delivers a brutal, bone-shattering puncture! {COLOR=#FFDB51}[[200%]{ENDCOLOR}";
    };

    return new AttackResult(roll, tier, damage, flavorText);
  }
}
