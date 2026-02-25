package edu.tip.forestoftreasures.Model;

public class Player extends Entity {
  // Additional player stats
  private float intelligence;
  private float dexterity;
  private float charisma;

  // Player movesets
  private final String skill1 = "Cry of Misery";
  private final String skill2 = "Intense Aura";
  private final String skill3 = "Lullaby of Obedience";

  public Player(float hp, float strength, float intelligence, float dexterity, float charisma) {
    super(hp, strength);
    this.intelligence = intelligence;
    this.dexterity = dexterity;
    this.charisma = charisma;
  }

  // --- Getter Methods ---
  public float getIntelligence() {
    return this.intelligence;
  }

  public float getDexterity() {
    return this.dexterity;
  }

  public float getCharisma() {
    return this.charisma;
  }

  public String getSkill1() {
    return this.skill1;
  }

  public String getSkill2() {
    return this.skill2;
  }

  public String getSkill3() {
    return this.skill3;
  }
}
