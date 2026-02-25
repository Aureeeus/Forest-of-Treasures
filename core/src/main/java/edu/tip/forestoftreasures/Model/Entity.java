package edu.tip.forestoftreasures.Model;

public class Entity {
  private float hp;
  private float strength;

  public Entity(float hp, float strength) {
    this.hp = hp;
    this.strength = strength;
  }

  public float getHp() {
    return this.hp;
  }

  public float getStrength() {
    return this.strength;
  }
}
