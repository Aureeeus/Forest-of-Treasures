package edu.tip.forestoftreasures.Model.entities;

/**
 * Immutable result of a skill-based attack, containing everything
 * the UI needs to display the outcome.
 *
 * @param diceRoll   the raw d20 value that was rolled
 * @param tier       the resolved damage tier (determines multiplier)
 * @param damage     the final damage dealt after applying the multiplier
 * @param flavorText the skill-specific narrative string with TextraLabel color tokens
 */
public record AttackResult(int diceRoll, DamageTier tier, float damage, String flavorText) {}
