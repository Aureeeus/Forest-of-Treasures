package edu.tip.forestoftreasures.Model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Serializable data transfer object that holds the complete game state
 * for saving and loading. All fields are mutable for JSON deserialization
 * compatibility with libGDX's {@link com.badlogic.gdx.utils.Json}.
 */
public class SaveData {
  private float hp;
  private float strength;
  private float intelligence;
  private float dexterity;
  private float charisma;

  private Set<String> unlockedAchievements = new HashSet<>();

  private int currentDay;
  private String currentNodeId;
  private String lastScenarioTexturePath;
  private List<Integer> playerChoicePath = new ArrayList<>();

  /** No-arg constructor required by libGDX Json deserializer. */
  public SaveData() {}

  public SaveData(float hp, float strength, float intelligence, float dexterity, float charisma,
                  Set<String> unlockedAchievements, int currentDay, String currentNodeId,
                  String lastScenarioTexturePath, List<Integer> playerChoicePath) {
    this.hp = hp;
    this.strength = strength;
    this.intelligence = intelligence;
    this.dexterity = dexterity;
    this.charisma = charisma;
    this.unlockedAchievements = new HashSet<>(unlockedAchievements);
    this.currentDay = currentDay;
    this.currentNodeId = currentNodeId;
    this.lastScenarioTexturePath = lastScenarioTexturePath;
    this.playerChoicePath = new ArrayList<>(playerChoicePath);
  }

  // --- Getters ---
  public float getHp() { return hp; }
  public float getStrength() { return strength; }
  public float getIntelligence() { return intelligence; }
  public float getDexterity() { return dexterity; }
  public float getCharisma() { return charisma; }
  public Set<String> getUnlockedAchievements() { return unlockedAchievements; }
  public int getCurrentDay() { return currentDay; }
  public String getCurrentNodeId() { return currentNodeId; }
  public String getLastScenarioTexturePath() { return lastScenarioTexturePath; }
  public List<Integer> getPlayerChoicePath() { return playerChoicePath; }

  // --- Setters (required by Json deserializer) ---
  public void setHp(float hp) { this.hp = hp; }
  public void setStrength(float strength) { this.strength = strength; }
  public void setIntelligence(float intelligence) { this.intelligence = intelligence; }
  public void setDexterity(float dexterity) { this.dexterity = dexterity; }
  public void setCharisma(float charisma) { this.charisma = charisma; }
  public void setUnlockedAchievements(Set<String> unlockedAchievements) { this.unlockedAchievements = unlockedAchievements; }
  public void setCurrentDay(int currentDay) { this.currentDay = currentDay; }
  public void setCurrentNodeId(String currentNodeId) { this.currentNodeId = currentNodeId; }
  public void setLastScenarioTexturePath(String lastScenarioTexturePath) { this.lastScenarioTexturePath = lastScenarioTexturePath; }
  public void setPlayerChoicePath(List<Integer> playerChoicePath) { this.playerChoicePath = playerChoicePath; }
}
