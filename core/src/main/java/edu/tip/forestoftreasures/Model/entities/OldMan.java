package edu.tip.forestoftreasures.Model.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

import edu.tip.forestoftreasures.Model.mechanics.AttackResult;
import edu.tip.forestoftreasures.Model.mechanics.DamageTier;
import edu.tip.forestoftreasures.Model.mechanics.Dice;

/**
 * A mysterious old wizard encountered as a near-invincible boss.
 * Uses intelligence as base damage (he is a wizard) rather than strength.
 * The only way to defeat him is to land a Sleep status effect.
 */
public class OldMan extends Entity {
  private final Texture texture;
  private final Sound attackSound;
  private final float intelligence;

  public OldMan(float initiative, Texture texture, Sound attackSound) {
    super(999f, 22f, initiative);
    this.intelligence = 22f;
    this.texture = texture;
    this.attackSound = attackSound;
  }

  @Override
  public Texture getTexture() {
    return texture;
  }

  public void playAttackSound(float volume) {
    if (attackSound != null) {
      attackSound.play(volume);
    }
  }

  /**
   * Executes the Old Man's arcane attack against a target.
   * Damage is based on intelligence rather than strength.
   *
   * @param target the entity being attacked
   * @return the final damage dealt
   */
  @Override
  public float attack(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = intelligence * tier.multiplier;
    target.takeDamage(damage);
    return damage;
  }

  /**
   * Executes the Old Man's arcane attack with narrative flavor text.
   * Same dice-roll logic as {@link #attack(Entity)}, but returns
   * an {@link AttackResult} with tier-appropriate narrative text.
   *
   * @param target the entity being attacked
   * @return an {@link AttackResult} containing dice roll, tier, damage, and flavor text
   */
  public AttackResult attackWithFlavor(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = intelligence * tier.multiplier;
    target.takeDamage(damage);

    String flavorText = switch (tier) {
      case MISS          -> "The Old Man's arcana blast charged at you with roaring might, but was deflected successfully by your own attack. {COLOR=RED}[[0%]{ENDCOLOR}";
      case HALF          -> "A ball of {COLOR=#AEE2FF}pure magic{ENDCOLOR} came shooting at you with an immense radius. You moved away, but it {SHAKE}grazed your skin{ENDSHAKE} ever so slightly. {COLOR=#AEE2FF}[[50%]{ENDCOLOR}";
      case THREE_QUARTER -> "A {COLOR=#9B59B6}purple magical comet{ENDCOLOR} came rushing towards you at great speed. You were not able to react in time, as it {SICK}took a bite off of your flesh{ENDSICK}. {COLOR=#15A3C7}[[75%]{ENDCOLOR}";
      case FULL          -> "An unfamiliar spell {WAVE}enveloped your vision{ENDWAVE}. It was a zone of {COLOR=#9B59B6}pure magic{ENDCOLOR} ready to impact you. {COLOR=#66FF00}[[100%]{ENDCOLOR}";
      case CRITICAL      -> "{COLOR=#FFDB51}CRITICAL!{ENDCOLOR} The Old Man's {SHAKE}eldritch blast{ENDSHAKE} of great speed and size appeared right before your whole body. The blast {COLOR=RED}screams death{ENDCOLOR} to whoever gets hit! {COLOR=#FFDB51}[[200%]{ENDCOLOR}";
    };

    return new AttackResult(roll, tier, damage, flavorText);
  }
}
