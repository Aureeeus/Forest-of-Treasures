package edu.tip.forestoftreasures.Model.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Sound;

import edu.tip.forestoftreasures.Model.mechanics.Dice;
import edu.tip.forestoftreasures.Model.mechanics.AttackResult;
import edu.tip.forestoftreasures.Model.mechanics.DamageTier;

/**
 * A Knight enemy encountered during battle sequences.
 * Has high strength and armor-themed flavor text.
 */
public class Knight extends Entity {
  private final Texture texture;
  private final Sound attackSound;

  public Knight(float initiative, Texture texture, Sound attackSound) {
    super(35f, 16f, initiative);
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

  @Override
  public float attack(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = strength * tier.multiplier;
    target.takeDamage(damage);
    return damage;
  }

  public AttackResult attackWithFlavor(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = strength * tier.multiplier;
    target.takeDamage(damage);

    String flavorText = switch (tier) {
      case MISS          -> "The knight's blade catches on a thicket of thorns, his swing losing all momentum in the tangled brush. {COLOR=RED}[[0%]{ENDCOLOR}";
      case HALF          -> "A clumsy haft-strike. The Knight's gauntlet rings against bone, leaving only a shallow bruise behind. {COLOR=#AEE2FF}[[50%]{ENDCOLOR}";
      case THREE_QUARTER -> "Steel bites through bark and hide alike, the Knight's heavy blow drawing a spray of dark, mossy blood. {COLOR=#15A3C7}[[75%]{ENDCOLOR}";
      case FULL          -> "A disciplined thrust! The blade pierces through the center, the weight of the Knight's armor driving the point home. {COLOR=#66FF00}[[100%]{ENDCOLOR}";
      case CRITICAL      -> "{COLOR=#FFDB51}CRITICAL!{ENDCOLOR} With a guttural shout, the Knight cleaves downward—a ruinous arc of cold steel that shatters spirit and flesh. {COLOR=#FFDB51}[[200%]{ENDCOLOR}";
    };

    return new AttackResult(roll, tier, damage, flavorText);
  }
}
