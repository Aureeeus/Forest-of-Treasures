package edu.tip.forestoftreasures.Model.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Sound;

import edu.tip.forestoftreasures.Model.mechanics.Dice;
import edu.tip.forestoftreasures.Model.mechanics.AttackResult;
import edu.tip.forestoftreasures.Model.mechanics.DamageTier;

/**
 * A wyvern enemy encountered during battle sequences.
 * High HP and Strength compared to standard enemies.
 */
public class Wyvern extends Entity {
  private final Texture texture;
  private final Sound attackSound;

  public Wyvern(float initiative, Texture texture, Sound attackSound) {
    super(60f, 18f, initiative);
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

  @Override
  public float attack(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = strength * tier.multiplier;
    target.takeDamage(damage);
    return damage;
  }

  /**
   * Executes the Wyvern's attack with flavor text for UI display.
   *
   * @param target the entity being attacked
   * @return an AttackResult containing dice roll, tier, damage, and flavor text
   */
  public AttackResult attackWithFlavor(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = strength * tier.multiplier;
    target.takeDamage(damage);

    String flavorText = switch (tier) {
      case MISS          -> "The wyvern dives astray, its shadow passing as talons clutch empty air. {COLOR=RED}[[0%]{ENDCOLOR}";
      case HALF          -> "A shallow rake. Its claws scrape your wards with a harsh, grating screech. {COLOR=#AEE2FF}[[50%]{ENDCOLOR}";
      case THREE_QUARTER -> "It snaps mid-flight—fangs nick you, venom tingling at the wound. {COLOR=#15A3C7}[[75%]{ENDCOLOR}";
      case FULL          -> "A savage plunge! Talons sink deep, tearing through cloth and flesh alike. {COLOR=#66FF00}[[100%]{ENDCOLOR}";
      case CRITICAL      -> "{COLOR=#FFDB51}CRITICAL!{ENDCOLOR} It crashes upon you in fury—fangs clamp down as venom floods your veins! {COLOR=#FFDB51}[[200%]{ENDCOLOR}";
    };

    return new AttackResult(roll, tier, damage, flavorText);
  }
}
