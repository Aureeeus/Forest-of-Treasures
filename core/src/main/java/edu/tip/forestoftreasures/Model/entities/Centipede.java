package edu.tip.forestoftreasures.Model.entities;

import com.badlogic.gdx.graphics.Texture;

import edu.tip.forestoftreasures.Model.mechanics.Dice;
import edu.tip.forestoftreasures.Model.mechanics.AttackResult;
import edu.tip.forestoftreasures.Model.mechanics.DamageTier;

import com.badlogic.gdx.audio.Sound;

/**
 * A centipede enemy encountered during battle sequences.
 * Uses strength as base damage and rolls a d20 to determine damage tier.
 */
public class Centipede extends Entity {
  private final Texture texture;
  private final Sound attackSound;

  public Centipede(float initiative, Texture texture, Sound attackSound) {
    super(50f, 10f, initiative);
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
   * Executes the Centipede's attack against a target.
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
   * Executes the Centipede's attack with flavor text for UI display.
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
      case MISS          -> "The centipede {WAVE}coils too wide{ENDWAVE}, its mandibles {SHAKE}snapping uselessly{ENDSHAKE} at shadows. {COLOR=RED}[[0%]{ENDCOLOR}";
      case HALF          -> "A grazing bite. Chitin scrapes your wards with a dry, {SICK}clicking hiss{ENDSICK}. {COLOR=#AEE2FF}[[50%]{ENDCOLOR}";
      case THREE_QUARTER -> "With a sudden skitter, it {SICK}nips your flesh{ENDSICK}-venom prickling beneath the skin. {COLOR=#15A3C7}[[75%]{ENDCOLOR}";
      case FULL          -> "A vicious strike! Its fangs {SHAKE}pierce deep{ENDSHAKE}, ichor and {SICK}poison seeping{ENDSICK} into your veins. {COLOR=#66FF00}[[100%]{ENDCOLOR}";
      case CRITICAL      -> "{COLOR=#FFDB51}CRITICAL!{ENDCOLOR} It rears and drives both fangs in-{SICK}venom floods{ENDSICK} you as its legs {SHAKE}writhe in frenzy{ENDSHAKE}! {COLOR=#FFDB51}[[200%]{ENDCOLOR}";
    };

    return new AttackResult(roll, tier, damage, flavorText);
  }
}
