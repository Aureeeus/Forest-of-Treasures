package edu.tip.forestoftreasures.Model.entities;

import com.badlogic.gdx.graphics.Texture;

import edu.tip.forestoftreasures.Model.mechanics.Dice;
import edu.tip.forestoftreasures.Model.mechanics.AttackResult;
import edu.tip.forestoftreasures.Model.mechanics.DamageTier;

import com.badlogic.gdx.audio.Sound;

/**
 * A leviathan enemy encountered during battle sequences.
 * Uses strength as base damage and rolls a d20 to determine damage tier.
 */
public class Leviathan extends Entity {
  private final Texture texture;
  private final Sound attackSound;

  public Leviathan(float initiative, Texture texture, Sound attackSound) {
    super(60f, 20f, initiative);
    this.texture = texture;
    this.attackSound = attackSound;
  }

  public Texture getTexture() {
    return texture;
  }

  public void playAttackSound(float volume) {
    if (attackSound != null) {
      attackSound.play(volume);
    }
  }

  /**
   * Executes the Leviathan's attack against a target.
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
   * Executes the Leviathan's attack with flavor text for UI display.
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
      case MISS          -> "An {WAVE}ember emerged{ENDWAVE} from its throat. The attack fails {COLOR=RED}[[0%]{ENDCOLOR}";
      case HALF          -> "The fireball barely hit, but it was enough to {SHAKE}set you on fire{ENDSHAKE} {COLOR=#AEE2FF}[[50%]{ENDCOLOR}";
      case THREE_QUARTER -> "A {SHAKE}blaze enveloped{ENDSHAKE} your surroundings and was able to incinerate your skin {COLOR=#15A3C7}[[75%]{ENDCOLOR}";
      case FULL          -> "The flamethrower hits! You are being {SHAKE}seared alive{ENDSHAKE}, but you extinguished the flames in time {COLOR=#66FF00}[[100%]{ENDCOLOR}";
      case CRITICAL      -> "{COLOR=#FFDB51}CRITICAL!{ENDCOLOR} The {SHAKE}Inferno blazes{ENDSHAKE} you. You are being {SICK}burned alive{ENDSICK}. {COLOR=#FFDB51}[[200%]{ENDCOLOR}";
    };

    return new AttackResult(roll, tier, damage, flavorText);
  }
}
