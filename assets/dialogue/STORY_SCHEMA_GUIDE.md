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
- `next`: The ID of the next node (or `null` to end the day).

### 2. Choice Node (`type: "choice"`)
A decision point for the player.
- `choices`: An array of player options:
  - `label`: The text shown on the choice button.
  - `next`: The node ID to advance to if selected.

### 3. Minigame Node (`type: "minigame"`)
Triggers an external gameplay sequence.
- `screenKey`: The unique key of the screen to launch (e.g., `"maze_minigame"`, `"bandit_battle_minigame"`).
- `playerTurn`: (Optional parameter) A Boolean dictating Minigame initiative. `true` sets Player (20), `false` sets Enemy (20). If omitted/null, it performs a normal D20 random check.
- `next`: The ID of the node to resume from upon minigame completion.

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
  "next": "d2_ambush_defeated"
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

### Choice Branching
```json
{
  "id": "forest_fork",
  "type": "choice",
  "choices": [
    { "label": "LEFT", "next": "left_path" },
    { "label": "RIGHT", "next": "right_path" }
  ]
}
```
