package edu.tip.forestoftreasures.Model.mechanics;

/**
 * Immutable result of a status-effect skill (e.g., Intense Aura),
 * containing everything the UI needs to display the outcome.
 *
 * @param diceRoll     the raw d20 value that was rolled
 * @param applied      whether the status effect was successfully applied
 * @param statusEffect the status effect that was attempted (null if target already had one)
 * @param flavorText   the narrative string with TextraLabel color tokens
 */
public record SkillResult(int diceRoll, boolean applied, StatusEffect statusEffect, String flavorText) {}
