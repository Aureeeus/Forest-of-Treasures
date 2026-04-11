package edu.tip.forestoftreasures.Model.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Sound;

import edu.tip.forestoftreasures.Model.mechanics.Dice;
import edu.tip.forestoftreasures.Model.mechanics.AttackResult;
import edu.tip.forestoftreasures.Model.mechanics.DamageTier;

/**
 * A goblin king enemy encountered during battle sequences.
 * High HP and Strength compared to standard enemies.
 */
public class GoblinKing extends Entity {
  private final Texture texture;
  private final Sound attackSound;

  public GoblinKing(float initiative, Texture texture, Sound attackSound) {
    super(75f, 22f, initiative);
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
   * Executes the Goblin King's attack with unique flavor text for UI display.
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
      case MISS          -> "The king's staff {WAVE}wooshes right beside you{ENDWAVE}, it would've been lethal if it hit, but it didn't {COLOR=RED}[[0%]{ENDCOLOR}";
      case HALF          -> "An enraged strike sent a {SHAKE}wave of pure concussive blast{ENDSHAKE} towards you. The staff was left unharmed but you are not. {COLOR=#AEE2FF}[[50%]{ENDCOLOR}";
      case THREE_QUARTER -> "A cold golden staff was enough to {SHAKE}crush a bone or two{ENDSHAKE}. The King spared no mercy on that strike, but it wasn't lethal... yet. {COLOR=#15A3C7}[[75%]{ENDCOLOR}";
      case FULL          -> "A clean hit! The King's attack was strong enough to {SHAKE}dislocate your shoulder{ENDSHAKE}. {COLOR=#66FF00}[[100%]{ENDCOLOR}";
      case CRITICAL      -> "{COLOR=#FFDB51}CRITICAL!{ENDCOLOR} You feel your {SHAKE}skull crack like twigs{ENDSHAKE} and your {SICK}organs scream in pain{ENDSICK}. You absorbed the King's wrath in all its glory. {COLOR=#FFDB51}[[200%]{ENDCOLOR}";
    };

    return new AttackResult(roll, tier, damage, flavorText);
  }
}
