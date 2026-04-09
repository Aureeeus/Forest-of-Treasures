package edu.tip.forestoftreasures.Model.entities;

import com.badlogic.gdx.graphics.Texture;
import edu.tip.forestoftreasures.Model.mechanics.StatusEffect;

/**
 * Abstract base class for all living entities in the game (player, enemies, etc.).
 * Holds shared combat stats and provides common battle behavior such as
 * taking damage and checking alive status. Subclasses must define their own
 * attack behavior.
 */
public abstract class Entity {
  protected float hp;
  protected float maxHp;
  protected float strength;
  protected float initiative;
  private StatusEffect activeStatus;

  /**
   * @param hp        starting and maximum hit points
   * @param strength  base physical attack power
   * @param initiative determines turn order in battle; higher value acts first
   */
  public Entity(float hp, float strength, float initiative) {
    this.hp = hp;
    this.maxHp = hp;
    this.strength = strength;
    this.initiative = initiative;
  }

  /**
   * Reduces this entity's HP by the given damage amount.
   * HP will never drop below zero.
   *
   * @param damage the amount of damage to inflict
   */
  public void takeDamage(float damage) {
    this.hp = Math.max(0, this.hp - damage);
  }

  /**
   * @return {@code true} if this entity still has HP remaining
   */
  public boolean isAlive() {
    return this.hp > 0;
  }

  /**
   * Restores this entity's HP back to maximum.
   */
  public void restoreFullHp() {
    this.hp = this.maxHp;
  }

  /**
   * Executes this entity's attack against a target.
   * Each subclass defines its own damage calculation and effects.
   *
   * @param target the entity being attacked
   * @return the amount of damage dealt
   */
  public abstract float attack(Entity target);

  public float getHp() {
    return this.hp;
  }

  public float getMaxHp() {
    return this.maxHp;
  }

  public float getStrength() {
    return this.strength;
  }

  public float getInitiative() {
    return this.initiative;
  }

  /**
   * @return the currently active status effect, or {@code null} if none
   */
  public StatusEffect getActiveStatus() {
    return this.activeStatus;
  }

  /**
   * @return {@code true} if this entity is afflicted by a status effect
   */
  public boolean hasStatusEffect() {
    return this.activeStatus != null;
  }

  /**
   * Applies a status effect to this entity.
   *
   * @param status the status effect to apply
   */
  public void applyStatusEffect(StatusEffect status) {
    this.activeStatus = status;
  }

  /**
   * Removes any active status effect from this entity.
   */
  public void clearStatusEffect() {
    this.activeStatus = null;
  }

  /**
   * @return the texture representing this entity in battle, or null if none
   */
  public abstract Texture getTexture();
}
