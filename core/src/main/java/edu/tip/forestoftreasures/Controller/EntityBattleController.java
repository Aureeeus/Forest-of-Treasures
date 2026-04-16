package edu.tip.forestoftreasures.Controller;

import java.util.function.Consumer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Model.dialogue.MinigameNode;
import edu.tip.forestoftreasures.Model.entities.Bandit;
import edu.tip.forestoftreasures.Model.entities.CavernCreature;
import edu.tip.forestoftreasures.Model.entities.Centipede;
import edu.tip.forestoftreasures.Model.entities.Leviathan;
import edu.tip.forestoftreasures.Model.entities.Wyvern;
import edu.tip.forestoftreasures.Model.entities.Knight;
import edu.tip.forestoftreasures.Model.entities.KnightCaptain;
import edu.tip.forestoftreasures.Model.entities.GoblinKing;
import edu.tip.forestoftreasures.Model.entities.OldMan;
import edu.tip.forestoftreasures.Model.entities.Entity;
import edu.tip.forestoftreasures.Model.entities.Player;
import edu.tip.forestoftreasures.Model.mechanics.AttackResult;
import edu.tip.forestoftreasures.Model.mechanics.DamageTier;
import edu.tip.forestoftreasures.Model.mechanics.Dice;
import edu.tip.forestoftreasures.Model.mechanics.SkillResult;
import edu.tip.forestoftreasures.Model.mechanics.StatusEffect;
import edu.tip.forestoftreasures.View.EntityBattleScreen;
import edu.tip.forestoftreasures.View.GameOverScreen;
import edu.tip.forestoftreasures.utils.DialogueUtils;
import edu.tip.forestoftreasures.utils.FontFactory;
import edu.tip.forestoftreasures.utils.UIFactory;

/**
 * Controller for the entity battle screen. Handles all user input,
 * skill selection logic, enemy resolution, and battle flow.
 * Follows MVC: the View ({@link EntityBattleScreen}) renders UI,
 * this controller handles logic and input.
 */
public class EntityBattleController {

  public enum BattleState {
    STARTING,
    PLAYER_TURN,
    ENEMY_TURN,
    ANIMATING_DIALOGUE,
    BATTLE_END
  }

  private final GameLauncher game;
  private final EntityBattleScreen screen;
  private final Player player;
  private final String screenKey;

  private BattleState state = BattleState.STARTING;

  // Blessing mechanic — exclusive to knights_battle_minigame
  private static final int BLESSING_THRESHOLD = 15;
  private String blessingFlavorText;

  private int maxEnemy = 1; // Default for standard battles

  // Enemy data
  private Entity enemy;
  private String enemyName;
  private final MinigameNode node;

  // Skill selection state
  private int selectedSkillRow = 0;
  private static final int SKILL_COUNT = 3;

  // Status effect tracking
  private int enemyStatusCounter = 0;
  private static final int STATUS_THRESHOLD = 2;
  private boolean enemyAppliedStatusThisTurn = false; // Tracking flag for immediate ticks in the current round

  // UI references from screen
  private Image selectSkillIcon;
  private Container<Actor> skillIconCell0 = new Container<>(null);
  private Container<Actor> skillIconCell1 = new Container<>(null);
  private Container<Actor> skillIconCell2 = new Container<>(null);
  private Container<Actor> skillLabelCell0 = new Container<>(null).align(Align.left | Align.center);
  private Container<Actor> skillLabelCell1 = new Container<>(null).align(Align.left | Align.center);
  private Container<Actor> skillLabelCell2 = new Container<>(null).align(Align.left | Align.center);

  // Text dialogue box
  private Table textDialogueTable;
  private boolean pendingOverflowTrim = false;
  private Font dialogueFont;

  private final Consumer<Boolean> onComplete;

  /**
   * @param game      the main game launcher holding the asset manager
   * @param screen    the battle screen view this controller manages
   * @param player    the player entity for this battle
   * @param node      the MinigameNode defining this battle scenario
   * @param onComplete the callback to run when the battle ends
   */
  public EntityBattleController(
      GameLauncher game,
      EntityBattleScreen screen,
      Player player,
      MinigameNode node,
      Consumer<Boolean> onComplete) {
    this.game = game;
    this.screen = screen;
    this.player = player;
    this.node = node;
    this.screenKey = node.screenKey;
    this.onComplete = onComplete;
    
    if (node.playerTurn != null) {
      this.player.setInitiative(node.playerTurn ? 20f : 1f);
    } else {
      this.player.setInitiative(Dice.roll()); // random initiative every battle
    }

    resolveEnemy(node.screenKey, node.playerTurn);

    // Dynamic sequence setup
    if ("knights_battle_minigame".equals(screenKey)) {
      this.maxEnemy = 10;
      player.resetMultiBattleCounter();
      if (node.healBlessing != null && node.healBlessing) {
        applyAbsoluteBlessing();
      } else {
        rollForBlessing();
      }
    } else if ("3knights_battle_minigame".equals(screenKey)) {
      this.maxEnemy = 3;
      player.resetMultiBattleCounter();
      if (node.healBlessing != null && node.healBlessing) {
        applyAbsoluteBlessing();
      }
    } else if ("5bandits_battle_minigame".equals(screenKey)) {
        this.maxEnemy = 5;
        player.resetMultiBattleCounter();
        if (node.healBlessing != null && node.healBlessing) {
          applyAbsoluteBlessing();
        }
    } else if (node.healBlessing != null && node.healBlessing) {
      // Allow absolute blessing for other battle types if explicitly set
      applyAbsoluteBlessing();
    }

    // Arrow icon from sprite sheet
    selectSkillIcon = UIFactory.getSelectionArrowIcon(game);

    // Dialogue font
    dialogueFont = FontFactory.generateFont("fonts/DotGothic16-Regular.ttf", 20, Color.WHITE);
  }

  /**
   * Performs a dedicated D20 roll to determine the player's blessing tier.
   * A roll at or above the threshold grants HIGH blessing (heal after every knight fight);
   * below the threshold grants LOW blessing (heal after every 3 knight fights).
   */
  private void rollForBlessing() {
    int blessingRoll = Dice.roll();
    Player.BlessingTier tier = blessingRoll >= BLESSING_THRESHOLD
        ? Player.BlessingTier.HIGH
        : Player.BlessingTier.LOW;

    player.setBlessingTier(tier);

    String tierColor = (tier == Player.BlessingTier.HIGH) ? "#66FF00" : "#AEE2FF";
    String tierLabel = (tier == Player.BlessingTier.HIGH)
        ? "An unknown force blesses you to full health every battle."
        : "An unknown force blesses you to full health every 3 battles.";

    blessingFlavorText = String.format(
      "\n{COLOR=#FFDB51}Roll for Blessing: [%d]{ENDCOLOR} - {COLOR=%s}%s{ENDCOLOR}",
      blessingRoll, tierColor, tierLabel);
  }

  /**
   * Applies the absolute blessing mechanic, bypassing the dice roll.
   * Sets the tier to HIGH (heal every battle) and sets the appropriate flavor text.
   */
  private void applyAbsoluteBlessing() {
    player.setBlessingTier(Player.BlessingTier.HIGH);
    blessingFlavorText = "\n{COLOR=#66FF00}A Divine Blessing washes over you. You will heal every battle.{ENDCOLOR}";
  }

  // ---------------------------------------------------------------------------
  // ENEMY RESOLUTION
  // ---------------------------------------------------------------------------

  /**
   * Instantiates the correct enemy based on the story schema's screenKey.
   * Add new cases here as more enemy types are introduced.
   *
   * @param screenKey     the identifier  from the MinigameNode in story_schema.json
   * @param playerTurn    optional override flag to set fixed priorities
   */
  private void resolveEnemy(String screenKey, Boolean playerTurn) {
    float initiative = calculateEnemyInitiative(playerTurn);

    switch (screenKey) {
      case "bandit_battle_minigame", "5bandits_battle_minigame" -> {
        Texture banditTexture = game.assets.get("scenarios/day1/hobgoblin_perpetrator.png", Texture.class);
        Sound banditAttackSound = game.assets.get("audio/sfx/enemies/bandit_attack.mp3", Sound.class);

        this.enemy = new Bandit(initiative, banditTexture, banditAttackSound);
        this.enemyName = "Bandit";
      }
      case "cavern_creature_battle_minigame" -> {
        Texture creatureTexture = game.assets.get("scenarios/day2/cavern_arc/cavern_creature.png", Texture.class);
        Sound creatureAttackSound = game.assets.get("audio/sfx/enemies/cavern_creature_attack.mp3", Sound.class);

        this.enemy = new CavernCreature(initiative, creatureTexture, creatureAttackSound);
        this.enemyName = "Cavern Creature";
      }
      case "centipede_battle_minigame" -> {
        Texture centipedeTexture = game.assets.get("scenarios/day2/cavern_arc/centipede.png", Texture.class);

        this.enemy = new Centipede(initiative, centipedeTexture, null);
        this.enemyName = "Centipede";
      }
      case "leviathan_battle_minigame" -> {
        Texture leviathanTexture = game.assets.get("scenarios/day2/cavern_arc/leviathan.png", Texture.class);
        Sound leviathanAttackSound = game.assets.get("audio/sfx/enemies/leviathan_attack.mp3", Sound.class);

        this.enemy = new Leviathan(initiative, leviathanTexture, leviathanAttackSound);
        this.enemyName = "Leviathan";
      }
      case "wyvern_battle_minigame" -> {
        Texture wyvernTexture = game.assets.get("scenarios/day2/leave_arc/wyvern.png", Texture.class);
        Sound wyvernAttackSound = game.assets.get("audio/sfx/enemies/wyvern_attack.mp3", Sound.class);

        this.enemy = new Wyvern(initiative, wyvernTexture, wyvernAttackSound);
        this.enemyName = "Wyvern";
      }
      case "knights_battle_minigame", "3knights_battle_minigame" -> {
        Texture knightTexture = game.assets.get("scenarios/day2/cavern_arc/knights.png", Texture.class);
        Sound knightSlashSound = game.assets.get("audio/sfx/enemies/bandit_attack.mp3", Sound.class);

        this.enemy = new Knight(initiative, knightTexture, knightSlashSound);
        this.enemyName = "Knight";
      }
      case "knight_captain_battle_minigame" -> {
        Texture captainTexture = game.assets.get("scenarios/day2/cavern_arc/knight_captain.png", Texture.class);
        Sound captainSlashSound = game.assets.get("audio/sfx/enemies/bandit_attack.mp3", Sound.class);

        this.enemy = new KnightCaptain(initiative, captainTexture, captainSlashSound);
        this.enemyName = "Knight Captain";
      }
      case "goblin_king_battle_minigame" -> {
        Texture goblinKingTexture = game.assets.get("scenarios/day2/bandit_arc/goblin_king.png", Texture.class);
        Sound goblinKingAttackSound = game.assets.get("audio/sfx/enemies/goblin_king_attack.mp3", Sound.class);

        this.enemy = new GoblinKing(initiative, goblinKingTexture, goblinKingAttackSound);
        this.enemyName = "Goblin King";
      }
      case "old_man_battle_minigame" -> {
        Texture oldManTexture = game.assets.get("scenarios/day3/old_man.png", Texture.class);
        Sound oldManAttackSound = game.assets.get("audio/sfx/enemies/old_man_attack.mp3", Sound.class);

        this.enemy = new OldMan(initiative, oldManTexture, oldManAttackSound);
        this.enemyName = "Old Man";
      }
      default -> throw new RuntimeException(
        "[EntityBattleController] Unknown battle screenKey: '" + screenKey + "'"
      );
    }
  }

  /**
   * Calculates the enemy's initiative based on the scenario's playerTurn override or by rolling.
   *
   * @param playerTurn optional override flag from story_schema.json
   * @return the calculated initiative
   */
  private float calculateEnemyInitiative(Boolean playerTurn) {
    if (playerTurn != null) {
      return playerTurn ? 1f : 20f;
    } else {
      return Dice.roll();
    }
  }

  // ---------------------------------------------------------------------------
  // INPUT HANDLING
  // ---------------------------------------------------------------------------

  /**
   * Polls keyboard input each frame. Called from the screen's render loop.
   * UP/DOWN arrows cycle through 3 skill rows; ENTER confirms the selection.
   */
  public void handleInput() {
    if (pendingOverflowTrim) {
      trimDialogueOverflow();
    }

    // Only allow navigating skills if the battle hasn't ended
    if (state != BattleState.BATTLE_END) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        selectedSkillRow = (selectedSkillRow - 1 + SKILL_COUNT) % SKILL_COUNT;
        refreshSkillSelection();
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        selectedSkillRow = (selectedSkillRow + 1) % SKILL_COUNT;
        refreshSkillSelection();
      }
    }

    // Only allow executing a skill if it is the player's turn
    if (state == BattleState.PLAYER_TURN && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      confirmSkillSelection();
    }
  }

  // ---------------------------------------------------------------------------
  // SKILL SELECTION
  // ---------------------------------------------------------------------------

  /**
   * Updates the visual state of all 3 skill rows based on selectedSkillRow.
   * Moves the arrow icon to the selected row and applies color tinting.
   */
  @SuppressWarnings("unchecked")
  private void refreshSkillSelection() {
    Container<Actor>[] iconCells = new Container[]{skillIconCell0, skillIconCell1, skillIconCell2};
    Container<Actor>[] labelCells = new Container[]{skillLabelCell0, skillLabelCell1, skillLabelCell2};

    for (int i = 0; i < SKILL_COUNT; i++) {
      iconCells[i].setActor(i == selectedSkillRow ? selectSkillIcon : null);
      UIFactory.tintCell(labelCells[i], i == selectedSkillRow);
    }
  }

  /**
   * Evaluates the player's selected skill against the enemy entity.
   * Shifts the battle state to dialogue animation to display the flavor text
   * of the executed action before ending the player's turn.
   */
  private void confirmSkillSelection() {
    state = BattleState.ANIMATING_DIALOGUE;
    
    String flavorText = "";
    float sfxVolume = game.settingsConfig.getGameSettings().sfxVolume();
    
    screen.startEnemyHpBatch();
    
    // Aggressive Merging: If the enemy ALREADY has a damaging status, tick it now so it merges with the skill
    if (enemy.hasStatusEffect()) {
        StatusEffect currentEffect = enemy.getActiveStatus();
        if (currentEffect == StatusEffect.POISON || currentEffect == StatusEffect.BURN) {
            currentEffect.applyPerTurn(enemy);
            enemyAppliedStatusThisTurn = true; 
        }
    }

    switch (selectedSkillRow) {
      case 0 -> {
        AttackResult result = player.useCryOfMisery(enemy);
        flavorText = result.flavorText();
        if (result.tier() != DamageTier.MISS) {
          game.assets.get("audio/sfx/Cry of Misery.mp3", Sound.class).play(sfxVolume);
          screen.updateEnemyHealth();
        }
      }
      case 1 -> {
        SkillResult result = player.useIntenseAura(enemy);
        flavorText = result.flavorText();
        if (result.applied()) {
          game.assets.get("audio/sfx/Intense Aura.wav", Sound.class).play(sfxVolume);
          
          // Old Man instant-defeat: Sleep ends the battle immediately
          if ("old_man_battle_minigame".equals(screenKey) && result.statusEffect() == StatusEffect.SLEEP) {
            screen.endEnemyHpBatch();
            state = BattleState.BATTLE_END;
            addDialogueLine(flavorText, () -> {
              addDialogueLine("\n{COLOR=#66FF00}The Old Man collapses into a deep slumber! You seize the moment and escape!{ENDCOLOR}{WAIT=1}", this::endBattle);
            });
            return;
          }

          // If we just applied a NEW status (and it wasn't already ticking via the aggressive merge above)
          StatusEffect effect = result.statusEffect();
          if (!enemyAppliedStatusThisTurn && (effect == StatusEffect.POISON || effect == StatusEffect.BURN)) {
             effect.applyPerTurn(enemy);
             enemyAppliedStatusThisTurn = true; 
          }
          screen.updateEnemyHealth();
        }
      }
      case 2 -> {
        AttackResult result = player.useLullabyOfObedience(enemy);
        flavorText = result.flavorText();
        if (result.tier() != DamageTier.MISS) {
          game.assets.get("audio/sfx/Lullaby Of Obedience.wav", Sound.class).play(sfxVolume);
          screen.updateEnemyHealth();
        }
      }
    }
    screen.endEnemyHpBatch();
    addDialogueLine(flavorText, this::onPlayerTurnEnded);
  }

  /**
   * Called immediately after the player's turn text finishes typing out.
   * Checks if the enemy died to end the battle; otherwise, transitions
   * to the enemy's turn logic.
   */
  private void onPlayerTurnEnded() {
    if (!enemy.isAlive()) {
      boolean isMultiBattle = "knights_battle_minigame".equals(screenKey) || 
                              "3knights_battle_minigame".equals(screenKey) || 
                              "5bandits_battle_minigame".equals(screenKey) ||
                              (node.healBlessing != null && node.healBlessing);

      if (isMultiBattle) {
        player.onMultiBattleCompleted();
        screen.updatePlayerHealth(); // Refresh with any blessing heal
        
        if (player.getMultiBattlesCompleted() < maxEnemy) {
          setupNextEnemy();
          return;
        }
      }
      
      state = BattleState.BATTLE_END;
      addDialogueLine("\n{COLOR=GREEN}The " + enemyName + " is defeated!{ENDCOLOR}{WAIT=1}", this::endBattle);
    } else {
      executeEnemyTurn();
    }
  }

  /**
   * Sets up the next enemy in a multi-battle sequence without disposing the screen.
   * Re-instantiates the enemy, rolls initiative, and refreshes the UI.
   */
  private void setupNextEnemy() {
    state = BattleState.ANIMATING_DIALOGUE;
    
    // Determine enemy type for instantiation
    float enemyInitiative = calculateEnemyInitiative(null);
    if ("knights_battle_minigame".equals(screenKey) || "3knights_battle_minigame".equals(screenKey)) {
      Texture knightTexture = game.assets.get("scenarios/day2/cavern_arc/knights.png", Texture.class);
      Sound knightSlashSound = game.assets.get("audio/sfx/enemies/bandit_attack.mp3", Sound.class);
      this.enemy = new Knight(enemyInitiative, knightTexture, knightSlashSound);
      this.enemyName = "Knight";
    } else if ("5bandits_battle_minigame".equals(screenKey)) {
        Texture banditTexture = game.assets.get("scenarios/day1/hobgoblin_perpetrator.png", Texture.class);
        Sound banditAttackSound = game.assets.get("audio/sfx/enemies/bandit_attack.mp3", Sound.class);
        this.enemy = new Bandit(enemyInitiative, banditTexture, banditAttackSound);
        this.enemyName = "Bandit";
    }
    
    // Re-roll player initiative for the new battle
    player.setInitiative(Dice.roll());
    
    // Refresh the View for both sides to show new initiatives and any blessing heals
    screen.refreshEnemyStats();
    screen.refreshPlayerStats();
    
    addDialogueLine("\n{COLOR=GREEN}The " + enemyName + " is defeated!{ENDCOLOR} {WAVE}Another " + enemyName + " steps forward to challenge you!{ENDWAVE}\n", () -> {
        addDialogueLine(getInitiativeFlavorText(), this::beginFirstTurn);
    });
  }

  /**
   * Calculates and applies the enemy's logic for their turn.
   * This includes evaluating ongoing status effects (e.g., SLEEP causing a skipped turn)
   * and resolving its attack against the player if it is able to act.
   * Updates UI logic and flavor text accordingly.
   */
  private void executeEnemyTurn() {
    state = BattleState.ANIMATING_DIALOGUE;
    
    String flavorText = "";
    boolean skipTurn = false;
    
    // Evaluate status effects before deciding what to do
    if (enemy.hasStatusEffect()) {
      StatusEffect currentStatus = enemy.getActiveStatus();
      
      // Skip the tick if it was already processed immediately upon application this round
      if (!enemyAppliedStatusThisTurn) {
        screen.startEnemyHpBatch();
        skipTurn = currentStatus.applyPerTurn(enemy);
        screen.updateEnemyHealth();
        screen.endEnemyHpBatch();
      }
      
      enemyAppliedStatusThisTurn = false; // Reset flag after processing turn start logic

      // Increment counter for ANY status effect
      enemyStatusCounter++;
      if (enemyStatusCounter >= STATUS_THRESHOLD) {
        enemy.clearStatusEffect();
        enemyStatusCounter = 0;
      }
    } else {
      enemyStatusCounter = 0;
    }
    
    if (enemy.isAlive()) {
      if (skipTurn) {
        // Handled as Sleep (returns true)
        flavorText = "The " + enemyName + " has decided to become part of the forest floor a bit earlier than expected. They are fast asleep.";
      } else {
        // Get SFX volume from settings
        float sfxVolume = game.settingsConfig.getGameSettings().sfxVolume();

        if (enemy instanceof Bandit bandit) {
          bandit.playAttackSound(sfxVolume);
          
          AttackResult result = bandit.attackWithFlavor(player);
          flavorText = result.flavorText();
        } else if (enemy instanceof CavernCreature cavernCreature) {
          cavernCreature.playAttackSound(sfxVolume);
          
          AttackResult result = cavernCreature.attackWithFlavor(player);
          flavorText = result.flavorText();
        } else if (enemy instanceof Centipede centipede) {
          centipede.playAttackSound(sfxVolume);
          
          AttackResult result = centipede.attackWithFlavor(player);
          flavorText = result.flavorText();
        } else if (enemy instanceof Leviathan leviathan) {
          leviathan.playAttackSound(sfxVolume);
          
          AttackResult result = leviathan.attackWithFlavor(player);
          flavorText = result.flavorText();
        } else if (enemy instanceof Wyvern wyvern) {
          wyvern.playAttackSound(sfxVolume);
          
          AttackResult result = wyvern.attackWithFlavor(player);
          flavorText = result.flavorText();
        } else if (enemy instanceof Knight knight) {
          knight.playAttackSound(sfxVolume);
          
          AttackResult result = knight.attackWithFlavor(player);
          flavorText = result.flavorText();
        } else if (enemy instanceof KnightCaptain captain) {
          captain.playAttackSound(sfxVolume);
          
          AttackResult result = captain.attackWithFlavor(player);
          flavorText = result.flavorText();
        } else if (enemy instanceof GoblinKing goblinKing) {
          goblinKing.playAttackSound(sfxVolume);

          AttackResult result = goblinKing.attackWithFlavor(player);
          flavorText = result.flavorText();
        } else if (enemy instanceof OldMan oldMan) {
          oldMan.playAttackSound(sfxVolume);

          AttackResult result = oldMan.attackWithFlavor(player);
          flavorText = result.flavorText();
        } else {
          // Fallback for other potential enemies
          float dmg = enemy.attack(player);
          flavorText = "The " + enemyName + " attacks for " + dmg + " damage!";
        }
      }
    }
    
    flavorText = "\n{COLOR=RED}It's the " + enemyName + "'s turn!{ENDCOLOR}\n" + flavorText;
    
    screen.startPlayerHpBatch();
    screen.updatePlayerHealth(); // Refresh with any damage dealt during the enemy's attack resolving
    screen.endPlayerHpBatch();
    
    addDialogueLine(flavorText, () -> {
      enemyAppliedStatusThisTurn = false; // Reset flag after interaction finishes, ready for next turn
      
      if (!enemy.isAlive()) {
        state = BattleState.BATTLE_END;
        addDialogueLine("\n{COLOR=GREEN}The " + enemyName + " is defeated!{ENDCOLOR}{WAIT=1}", this::endBattle);
      } else if (!player.isAlive()) {
        state = BattleState.BATTLE_END;
        addDialogueLine("\n{COLOR=RED}You have been killed...{ENDCOLOR}", this::endBattle);
      } else {
        state = BattleState.PLAYER_TURN;
        addDialogueLine("\n{COLOR=#FFDB51}It's your turn!{ENDCOLOR}", null);
      }
    });
  }

  /**
   * Concludes the battle scenario. Disposes of native UI resources
   * to prevent memory leaks and executes the completion callback 
   * to transition back to the overworld or previous screen.
   */
  private void endBattle() {
    screen.dispose();

    // The blessing heal and battle counter are now handled internally during the loop,
    // but we can ensure one final increment if needed, or if it was the last fight.
    // However, onKnightBattleCompleted() is already called in onPlayerTurnEnded for the 10-loop.
    
    // If player died, go to GameOverScreen; otherwise resume dialogue
    if (!player.isAlive()) {
      if (node != null && node.getFailNext() != null) {
        onComplete.accept(false); // notify runner of failure
      } else {
        game.setScreen(new GameOverScreen(game));
      }
    } else if (onComplete != null) {
      onComplete.accept(true); // notify runner of success
    }
  }

  /**
   * Renders the 3 skill rows into the given skill widget table.
   * Each row has an icon cell (for the arrow) and a label cell (for the skill name).
   * The first row is selected by default.
   *
   * @param skillWidgetTable the table to add skill rows to
   * @param statsFont        the font used for skill labels
   */
  @SuppressWarnings("unchecked")
  public void renderSkillWidgets(Table skillWidgetTable, Font statsFont) {
    String[] skillNames = {player.getSkill1(), player.getSkill2(), player.getSkill3()};
    Container<Actor>[] iconCells = new Container[]{skillIconCell0, skillIconCell1, skillIconCell2};
    Container<Actor>[] labelCells = new Container[]{skillLabelCell0, skillLabelCell1, skillLabelCell2};

    for (int i = 0; i < SKILL_COUNT; i++) {
      TextraLabel skillLabel = new TextraLabel(skillNames[i], statsFont);
      skillLabel.setAlignment(Align.left);
      labelCells[i].setActor(skillLabel);
      labelCells[i].fill();

      if (i == 0) {
        iconCells[i].setActor(selectSkillIcon);
        UIFactory.tintCell(labelCells[i], true);
      } else {
        UIFactory.tintCell(labelCells[i], false);
      }

      float bottomPad = (i < SKILL_COUNT - 1) ? 10f : 0f;
      skillWidgetTable.add(iconCells[i]).size(30f).padRight(20f).padBottom(bottomPad);
      skillWidgetTable.add(labelCells[i]).growX().align(Align.left | Align.center).padBottom(bottomPad).row();
    }
  }

  // ---------------------------------------------------------------------------
  // GETTERS
  // ---------------------------------------------------------------------------

  public Entity getEnemy() {
    return enemy;
  }

  public String getEnemyName() {
    return enemyName;
  }

  public Player getPlayer() {
    return player;
  }

  /**
   * Returns the flavor text indicating who attacks first based on initiative.
   * The entity with the higher initiative goes first. Ties favor the enemy.
   *
   * @return the turn-order flavor text for UI display
   */
  public String getInitiativeFlavorText() {
    if (player.getInitiative() > enemy.getInitiative()) {
      return "The Spirits of the Woods find you worthy of this moment-they shroud your presence as you prepare to strike!";
    } else {
      return "The Spirits of the Woods whisper a warning to the " + enemyName + "; they lunge before you can even draw breath!";
    }
  }

  // ---------------------------------------------------------------------------
  // TEXT DIALOGUE
  // ---------------------------------------------------------------------------

  public void setTextDialogueTable(Table textDialogueTable) {
    this.textDialogueTable = textDialogueTable;
  }

  /**
   * Starts the battle turn loop. Called after the initiative flavor text finishes animating.
   * If a blessing roll was performed (knights battle), it is shown first.
   */
  public void startFirstTurn() {
    if (blessingFlavorText != null) {
      addDialogueLine(blessingFlavorText, this::beginFirstTurn);
    } else {
      beginFirstTurn();
    }
  }

  /**
   * Resolves who goes first based on initiative and begins the turn loop.
   */
  private void beginFirstTurn() {
    if (player.getInitiative() > enemy.getInitiative()) {
      state = BattleState.PLAYER_TURN;
      addDialogueLine("\n{COLOR=#FFDB51}It's your turn!{ENDCOLOR}", null);
    } else {
      executeEnemyTurn();
    }
  }

  /**
   * Adds a new line of text to the dialogue box using a TypingLabel.
   * Bubbles up existing text by aligning to the bottom, pushing older lines up.
   * If the lines overflow the height, older lines are trimmed.
   *
   * @param text       The text to display.
   * @param onComplete Optional callback executed when typing animation finishes
   */
  public void addDialogueLine(String text, Runnable onComplete) {
    if (textDialogueTable == null) return;

    TypingLabel typingLabel = new TypingLabel(text, dialogueFont);
    if (game.settingsConfig.getGameSettings().isReadAloudEnabled()) {
      typingLabel.setTextSpeed(0.08f / 1.1f);
    }

    DialogueUtils.configureTypingLabel(typingLabel, game, new Runnable() {
      @Override
      public void run() {
        if (game.speechManager.isSpeaking()) {
          Gdx.app.postRunnable(this);
        } else if (onComplete != null) {
          onComplete.run();
        }
      }
    });

    // Narrate the battle text asynchronously (no-op if Read Aloud is disabled)
    game.speechManager.say(text);

    textDialogueTable.add(typingLabel)
      .growX()
      .bottom()
      .left()
      .padBottom(5f)
      .row();

    textDialogueTable.invalidateHierarchy();

    trimDialogueOverflow();
  }

  /**
   * Removes old dialogue lines from the top until all content fits inside the table.
   */
  private void trimDialogueOverflow() {
    pendingOverflowTrim = !DialogueUtils.trimDialogueOverflow(textDialogueTable);
  }
}
