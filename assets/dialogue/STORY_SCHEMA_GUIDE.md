# Story Schema Documentation Guide

The `story_schema.json` file is where all the narrative content for *Forest of Treasures* is defined. This central file contains the script, branches, and game transitions for each day.

## Root Structure

The root level contains "Day" keys. Each day is an object with:
- `entryNode`: The ID of the node where the runner starts.
- `achievements`: An array of achievement objects.
- `nodes`: The core structure consisting of the story's script.

### Example Root Structure
```json
{
  "day1": {
    "entryNode": "d1_intro",
    "achievements": [...],
    "nodes": [...]
  }
}
```

## Node Types

Each node is defined by a `"type"` and unique `"id"`.

### 1. Line Node (`type: "line"`)
Standard text presentation.
- `text`: The string to display. Supports [formatting tags](#text-formatting).
- `texture`: path to the background image in `assets/scenarios/` (or `null` to retain the current image).
- `damage` (Optional): An integer specifying HP deduction when the line appears. Defaults to `0`.
- `triggerGameEnd` (Optional): Boolean. If `true`, transitions to the Credits screen after this line finishes typing. Defaults to `false`.
- `triggerGameOver` (Optional): Boolean. If `true`, transitions to the Game Over screen after this line finishes typing and resets progress. Defaults to `false`.
- `obtainableAchievement` (Optional): A string matching an achievement `id` from the day's `achievements` array. When the runner reaches this line, it immediately runs BFS verification against the player's recorded choices to determine if the achievement is unlocked.
- `increaseCharisma` (Optional): An integer specifying the amount to increase the player's charisma stat. If null or omitted, no change occurs.
- `next`: The ID of the next node (or `null` to end the day).

### 2. Choice Node (`type: "choice"`)
A decision point for the player.
- `choices`: An array of player options:
  - `label`: The text shown on the choice button.
  - `next`: The node ID to advance to if selected.
  - `increaseCharisma` (Optional): An integer specifying the amount to increase the player's charisma stat upon selecting this choice.

### 3. Minigame Node (`type: "minigame"`)
Triggers an external gameplay sequence.
- `screenKey`: The unique key of the screen to launch (e.g., `"maze_minigame"`, `"bandit_battle_minigame"`).
- `playerTurn`: (Optional parameter) A Boolean dictating Minigame initiative. `true` sets Player (20), `false` sets Enemy (20). If omitted/null, it performs a normal D20 random check.
- `next`: The ID of the node to resume from upon minigame completion (Success Path).
- `failNext`: (Optional) The ID of the node to resume from if the player loses the minigame. If omitted, losing a minigame triggers a standard Game Over screen.

### 4. Automatic Roll Node (`type: "automatic_roll"`)
An automatic, background D20 check for branching logic.
- `threshold`: The value (1-20) required for a success.
- `successNext`: The node ID if the roll is `>= threshold`.
- `failNext`: The node ID if the roll is `< threshold`.

### 5. Manual Roll Node (`type: "manual_roll"`)
A prompt demanding player action to perform a D20 check.
- `text`: The string shown leading up to the prompt.
- `texture`: path to the background image.
- `label`: What to display on the action button (e.g., `"ROLL D20 FOR ATHLETICISM"`).
- `threshold`: The value (1-20) required for a success.
- `successNext`: The node ID if roll is `>= threshold`.
- `failNext`: The node ID if roll is `< threshold`.

### 6. Evaluate Stats Node (`type: "evaluate_stats"`)
An automatic, silent background check against the player's accumulated charisma stat.
- `threshold`: The charisma value required for success.
- `successNext`: The node ID if the player's charisma is `>= threshold`.
- `failNext`: The node ID if the player's charisma is `< threshold`.

## Text Formatting

The system uses [TextraTypist](https://github.com/tommyettinger/textra) tags for styling and effects inside the `"text"` field.

- `{COLOR=#HEXCODE}`: Sets the text color. End with `{ENDCOLOR}`.
- `{SHAKE}`: Applies a jitter effect to the text. End with `{ENDSHAKE}`.
- `{WAVE}`: Makes the text float up and down. End with `{ENDWAVE}`.
- `{SICK}`: Applies a sickly, wavering effect. End with `{ENDSICK}`.
- `{WAIT=n}`: Pauses the typing animation for `n` seconds.
- `\n`: Inserts a new line (highly recommended for spacing).

## Template Snippets

### Standard Line with Damage
```json
{
  "id": "danger_zone",
  "type": "line",
  "text": "\nYou felt a sharp sting! The forest doesn't like intruders. {COLOR=RED}[-4 HP]{ENDCOLOR}",
  "damage": 4,
  "texture": "scenarios/danger.png",
  "next": "continue_path"
}
```

### Minigame Sequence (Battle Override)
```json
{
  "id": "ambush_battle",
  "type": "minigame",
  "screenKey": "bandit_battle_minigame",
  "playerTurn": false,
  "next": "d2_ambush_defeated",
  "failNext": "d2_captured_by_bandits"
}
```

### Automatic Roll Sequence (Silent Background Check)
```json
{
  "id": "stealth_check",
  "type": "automatic_roll",
  "threshold": 12,
  "successNext": "stealth_success_line",
  "failNext": "fail_noticed"
}
```

### Manual Roll Sequence (Interactive Player Prompt)
```json
{
  "id": "climb_check",
  "type": "manual_roll",
  "text": "The tree is tall, I might need to jump.",
  "texture": "scenarios/tree.png",
  "label": "ROLL D20 FOR ATHLETICISM",
  "threshold": 12,
  "successNext": "top_of_tree",
  "failNext": "fall_down"
}
```

### Evaluate Stats Sequence (Silent Charisma Check)
```json
{
  "id": "persuasion_gate",
  "type": "evaluate_stats",
  "threshold": 5,
  "successNext": "council_accepts",
  "failNext": "council_rejects"
}
```

### Charisma Increase Patterns
You can reward the player with charisma for certain dialogue lines or choices.

```json
{
  "id": "persuaded_well",
  "type": "choice",
  "choices": [
    {
      "label": "POLITELY ASK FOR HELP",
      "next": "d1_help_line",
      "increaseCharisma": 5
    },
    {
      "label": "DEMAND ASSISTANCE",
      "next": "d1_rude_line"
    }
  ]
}
```

```json
{
  "id": "wisdom_words",
  "type": "line",
  "text": "\n\"You speak with the grace of the elders,\" the sprite whispers.",
  "increaseCharisma": 2,
  "next": "d1_sprite_gift"
}
```

## Achievement Authoring Patterns

With the `obtainableAchievement` property, you can create two types of achievements:

### A. Sequence-Based Achievement (Automatic)
Awarded automatically the moment the player makes the final choice that completes the required sequence.
*   **Definition**: Include a `requiredChoiceSequence` in the day header.
*   **Trigger**: **NONE**. The system automatically verifies your choices every time you click a button.

```json
// Definition (at top of day)
{ 
  "id": "Sprite Friend", 
  "description": "You welcomed the sprite.", 
  "requiredChoiceSequence": [1] 
}

// No node tagging needed!
```

### B. Reach-Only Achievement (Manual)
Awarded only when the player reaches a specific line of dialogue, regardless of previous choices.
*   **Definition**: Omit `requiredChoiceSequence` or set it to `[]` in the day header.
*   **Trigger**: Add `obtainableAchievement` to the target Line Node.

```json
// Definition (at top of day)
{ "id": "Explorer", "description": "You reached the secret grove!" }

// Trigger inside nodes
{
  "id": "secret_grove_entry",
  "type": "line",
  "obtainableAchievement": "Explorer",
  "text": "You stepped into a clearing that smells of ancient magic."
}
```
