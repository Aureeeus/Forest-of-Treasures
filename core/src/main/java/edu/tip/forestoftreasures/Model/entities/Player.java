package edu.tip.forestoftreasures.Model.entities;

/**
 * Represents the player character in the game.
 * Extends {@link Entity} with RPG stats (intelligence, dexterity, charisma)
 * and a fixed set of combat skills. Unlike enemies, the Player persists
 * across the game, so initiative can be re-rolled via setter at each battle start.
 */
public class Player extends Entity {
  private float intelligence;
  private float dexterity;
  private float charisma;

  // Player movesets
  private final String skill1 = "Cry of Misery";
  private final String skill2 = "Intense Aura";
  private final String skill3 = "Lullaby of Obedience";

  /**
   * @param hp           starting and maximum hit points
   * @param strength     base physical attack power
   * @param initiative   determines turn order in battle; higher value acts first
   * @param intelligence affects magic-based abilities
   * @param dexterity    affects agility and evasion
   * @param charisma     affects dialogue and persuasion outcomes
   */
  public Player(float hp, float strength, float initiative, float intelligence, float dexterity, float charisma) {
    super(hp, strength, initiative);
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

  /**
   * Updates the player's initiative value.
   * Called at the start of each battle to re-roll turn order,
   * since the Player instance persists across the game.
   *
   * @param initiative the new initiative value (typically from {@link Dice#roll()})
   */
  public void setInitiative(float initiative) {
    this.initiative = initiative;
  }

  @Override
  public float attack(Entity target) {
    // TODO: Implement player attack logic
    return 0;
  }
}
