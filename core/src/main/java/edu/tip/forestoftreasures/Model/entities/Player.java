package edu.tip.forestoftreasures.Model.entities;

import com.badlogic.gdx.graphics.Texture;

import edu.tip.forestoftreasures.Model.mechanics.Dice;
import edu.tip.forestoftreasures.Model.mechanics.AttackResult;
import edu.tip.forestoftreasures.Model.mechanics.DamageTier;
import edu.tip.forestoftreasures.Model.mechanics.SkillResult;
import edu.tip.forestoftreasures.Model.mechanics.StatusEffect;

/**
 * Represents the player character in the game.
 * Extends {@link Entity} with RPG stats (intelligence, dexterity, charisma)
 * and a fixed set of combat skills. Unlike enemies, the Player persists
 * across the game, so initiative can be re-rolled via setter at each battle start.
 */
public class Player extends Entity {
  // Default starting stats
  public static final float STARTING_HP = 48f;
  public static final float STARTING_STR = 10f;
  public static final float STARTING_INT = 30f;
  public static final float STARTING_DEX = 12f;
  public static final float STARTING_CHA = 12f;

  /**
   * Determines how frequently the player is healed after completing a knight battle.
   * HIGH heals after every fight; LOW heals after every 3 fights.
   */
  public enum BlessingTier { NONE, LOW, HIGH }

  private float intelligence;
  private float dexterity;
  private float charisma;

  private BlessingTier blessingTier = BlessingTier.NONE;
  private int knightBattlesCompleted = 0;

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

  public void increaseCharisma(int amount) {
    this.charisma += amount;
  }

  /**
   * Resets all player stats and progress data to the specified initial values.
   * Used during a Game Over to restart the player's journey from scratch.
   *
   * @param hp           the initial max and current HP
   * @param strength     the initial strength
   * @param intelligence the initial intelligence
   * @param dexterity    the initial dexterity
   * @param charisma     the initial charisma
   */
  public void resetStats(float hp, float strength, float intelligence, float dexterity, float charisma) {
    this.maxHp = hp;
    this.hp = hp;
    this.strength = strength;
    this.intelligence = intelligence;
    this.dexterity = dexterity;
    this.charisma = charisma;
    this.initiative = 0;
    this.blessingTier = BlessingTier.NONE;
    this.knightBattlesCompleted = 0;
    clearStatusEffect();
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

  public void setBlessingTier(BlessingTier tier) {
    this.blessingTier = tier;
  }

  public BlessingTier getBlessingTier() {
    return blessingTier;
  }

  public int getKnightBattlesCompleted() {
    return knightBattlesCompleted;
  }

  /**
   * Called after completing a knight battle. Increments the internal counter
   * and restores full HP based on the active blessing tier.
   *
   * HIGH tier heals after every knight fight.
   * LOW tier heals only on every 3rd knight fight.
   *
   * @return {@code true} if HP was restored this battle
   */
  public boolean onKnightBattleCompleted() {
    knightBattlesCompleted++;

    boolean shouldHeal = switch (blessingTier) {
      case HIGH -> true;
      case LOW  -> knightBattlesCompleted % 3 == 0;
      case NONE -> false;
    };

    if (shouldHeal) {
      restoreFullHp();
    }

    return shouldHeal;
  }

  /**
   * Not used by Player — the Player attacks via skill methods
   * (e.g., {@link #useCryOfMisery(Entity)}).
   * This override exists only to satisfy the abstract contract from {@link Entity},
   * which is intended for enemy subclasses.
   */
  @Override
  public float attack(Entity target) {
    throw new UnsupportedOperationException("Player attacks via skill methods, not attack().");
  }

  /**
   * Executes the Cry of Misery skill against a target.
   * Rolls the dice to determine damage tier, applies damage based on intelligence,
   * and returns a result with tier-appropriate narrative flavor text.
   *
   * @param target the entity being attacked
   * @return an {@link AttackResult} containing dice roll, tier, damage, and flavor text
   */
  public AttackResult useCryOfMisery(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = intelligence * tier.multiplier;
    target.takeDamage(damage);

    String flavorText = switch (tier) {
      case MISS          -> "Your throat tightens; the Cry dies in your chest. They scoff at your silence. {COLOR=RED}[[0%]{ENDCOLOR}";
      case HALF          -> "A faint wail escapes your lips. They flinch, but their resolve holds. {COLOR=#AEE2FF}[[50%]{ENDCOLOR}";
      case THREE_QUARTER -> "Your shriek echoes through the gloom, rattling the bones of those nearby! {COLOR=#15A3C7}[[75%]{ENDCOLOR}";
      case FULL          -> "A soul-piercing scream erupts! The darkness itself recoils from your agony. {COLOR=#66FF00}[[100%]{ENDCOLOR}";
      case CRITICAL      -> "{COLOR=#FFDB51}CRITICAL!{ENDCOLOR} A catastrophic rift of sorrow shatters the minds of them! {COLOR=#FFDB51}[[200%]{ENDCOLOR}";
    };

    return new AttackResult(roll, tier, damage, flavorText);
  }

  /**
   * Executes the Lullaby of Obedience skill against a target.
   * Rolls the dice to determine damage tier, applies damage based on intelligence,
   * and returns a result with tier-appropriate narrative flavor text.
   *
   * @param target the entity being attacked
   * @return an {@link AttackResult} containing dice roll, tier, damage, and flavor text
   */
  public AttackResult useLullabyOfObedience(Entity target) {
    int roll = Dice.roll();
    DamageTier tier = DamageTier.fromDiceRoll(roll);
    float damage = intelligence * tier.multiplier;
    target.takeDamage(damage);

    String flavorText = switch (tier) {
      case MISS          -> "Your voice cracks. They laughed at your feeble attempt at control. {COLOR=RED}[[0%]{ENDCOLOR}";
      case HALF          -> "They dazed for a moment, but shakes off the rhythm with a growl. {COLOR=#AEE2FF}[[50%]{ENDCOLOR}";
      case THREE_QUARTER -> "Your song weaves around their mind, dulling their senses. {COLOR=#15A3C7}[[75%]{ENDCOLOR}";
      case FULL          -> "Their eyes go vacant. They begin to sway to your dark melody. {COLOR=#66FF00}[[100%]{ENDCOLOR}";
      case CRITICAL      -> "{COLOR=#FFDB51}CRITICAL!{ENDCOLOR} They fall to their knees, a puppet to your every word! {COLOR=#FFDB51}[[200%]{ENDCOLOR}";
    };

    return new AttackResult(roll, tier, damage, flavorText);
  }

  /**
   * Executes the Intense Aura skill, attempting to apply a randomized
   * status effect (Burn, Poison, or Sleep) to the target.
   *
   * Pre-conditions checked before rolling:
   * <ul>
   *   <li>If the target already has a status effect, the skill fails immediately
   *       with a sarcastic message (randomly chosen from two variants).</li>
   * </ul>
   *
   * Dice resolution: 1 = failed, 2–20 = success.
   *
   * @param target the entity to afflict
   * @return a {@link SkillResult} containing the dice roll, success flag, effect, and flavor text
   */
  public SkillResult useIntenseAura(Entity target) {
    // If the target already has a status effect, fail immediately
    if (target.hasStatusEffect()) {
      String existingStatus = target.getActiveStatus().name().toLowerCase();

      // Randomly pick one of two "already afflicted" lines
      String flavorText = (Dice.roll() <= 10)
        ? String.format("The target is already busy dying of %s. Perhaps let them finish one agony before starting the next?", existingStatus)
        : "Your mana fizzes out. Apparently, even the laws of magic have a \"one-catastrophe-at-a-time\" policy.";

      return new SkillResult(0, false, null, flavorText);
    }

    // Randomize which status effect to attempt
    StatusEffect effect = StatusEffect.random();

    // Roll dice: 1 = failed, 2-20 = success
    int roll = Dice.roll();
    boolean success = roll >= 2;

    String flavorText = switch (effect) {
      case BURN -> success
        ? "The atmosphere combusts! They scream as invisible embers sear their flesh. {COLOR=#66FF00}[[Success]{ENDCOLOR}"
        : "A flickering spark dies against their skin. The air remains cold. {COLOR=RED}[[Failed]{ENDCOLOR}";
      case POISON -> success
        ? "A thick, violet rot chokes the air. They stagger, veins turning black with bile. {COLOR=#66FF00}[[Success]{ENDCOLOR}"
        : "A thin, sickly vapor rises but dissipates before they can inhale. {COLOR=RED}[[Failed]{ENDCOLOR}";
      case SLEEP -> success
        ? "The weight of a thousand nights falls upon them. They collapse into a hollow trance. {COLOR=#66FF00}[[Success]{ENDCOLOR}"
        : "They blink against a momentary drowsiness, but their eyes snap back open. {COLOR=RED}[[Failed]{ENDCOLOR}";
    };

    if (success) {
      target.applyStatusEffect(effect);
    }

    return new SkillResult(roll, success, effect, flavorText);
  }

  @Override
  public Texture getTexture() {
    return null; // Player doesn't have a texture on the battle screen
  }
}
