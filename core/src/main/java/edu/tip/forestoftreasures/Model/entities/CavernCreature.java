package edu.tip.forestoftreasures.Model.entities;

import com.badlogic.gdx.graphics.Texture;

import edu.tip.forestoftreasures.Model.mechanics.Dice;
import edu.tip.forestoftreasures.Model.mechanics.AttackResult;
import edu.tip.forestoftreasures.Model.mechanics.DamageTier;

import com.badlogic.gdx.audio.Sound;

/**
 * A cavern creature enemy encountered during battle sequences.
 * Uses strength as base damage and rolls a d20 to determine damage tier.
 */
public class CavernCreature extends Entity {
  private final Texture texture;
  private final Sound attackSound;

  /**
   * Constructs a CavernCreature with specified initiative and texture.
   *
   * @param initiative   Determines turn order in battle.
   * @param texture      The visual representation of the creature in combat.
   * @param attackSound  Optional sound effect (currently null).
   */
  public CavernCreature(float initiative, Texture texture, Sound attackSound) {
    super(65f, 18f, initiative);
    this.texture = texture;
    this.attackSound = attackSound;
  }

  /**
   * Retrieves the texture for the creature.
   *
   * @return The Texture object used in the battle screen.
   */
  public Texture getTexture() {
    return texture;
  }

  /**
   * Plays the attack sound if it exists.
   *
   * @param volume  The volume level for playback.
   */
  public void playAttackSound(float volume) {
    if (attackSound != null) {
      attackSound.play(volume);
    }
  }

  /**
   * Executes a standard attack against a target.
   *
   * @param target  The entity taking damage.
   * @return        The amount of damage dealt.
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
   * Executes an attack with specific flavor text for the UI.
   *
   * @param target  The entity being attacked.
   * @return        An AttackResult containing roll, tier, damage, and story text.
   */
  public AttackResult attackWithFlavor(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = strength * tier.multiplier;
    target.takeDamage(damage);

    String flavorText = switch (tier) {
      case MISS          -> "\u201cOAAAAARRRGGHHHHH!\u201d The creature shrieks as it lunges at you, but it misses {COLOR=RED}[[0%]{ENDCOLOR}";
      case HALF          -> "The creature, probably blinded by the darkness, grazes your clothes {COLOR=#AEE2FF}[[50%]{ENDCOLOR}";
      case THREE_QUARTER -> "The creature's fangs sink in, but are lightly deflected by your robes {COLOR=#15A3C7}[[75%]{ENDCOLOR}";
      case FULL          -> "With a deadly swing, the creature\u2019s spines carved deep into your skin {COLOR=#66FF00}[[100%]{ENDCOLOR}";
      case CRITICAL      -> "{COLOR=#FFDB51}CRITICAL!{ENDCOLOR} The creature\u2019s weight crushes your bones. {COLOR=#FFDB51}[[200%]{ENDCOLOR}";
    };

    return new AttackResult(roll, tier, damage, flavorText);
  }
}
