package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingAdapter;
import com.github.tommyettinger.textra.TypingLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Model.entities.AttackResult;
import edu.tip.forestoftreasures.Model.entities.Bandit;
import edu.tip.forestoftreasures.Model.entities.Dice;
import edu.tip.forestoftreasures.Model.entities.Entity;
import edu.tip.forestoftreasures.Model.entities.Player;
import edu.tip.forestoftreasures.Model.entities.SkillResult;
import edu.tip.forestoftreasures.Model.entities.StatusEffect;
import edu.tip.forestoftreasures.View.EntityBattleScreen;
import edu.tip.forestoftreasures.utils.FontFactory;

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

  private BattleState state = BattleState.STARTING;

  // Enemy data
  private Entity enemy;
  private String enemyName;

  // Skill selection state
  private int selectedSkillRow = 0;
  private static final int SKILL_COUNT = 3;

  // Status effect tracking
  private int enemySleepCounter = 0;
  private static final int MAX_SLEEP_TURNS = 2;

  private boolean skipAnimConfig = false; // Globally toggle dialogue animations

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

  private final Runnable onComplete;

  /**
   * @param game      the main game launcher holding the asset manager
   * @param screen    the battle screen view this controller manages
   * @param player    the player entity for this battle
   * @param screenKey the identifier from the MinigameNode in story_schema.json
   * @param onComplete the callback to run when the battle ends
   */
  public EntityBattleController(GameLauncher game, EntityBattleScreen screen, Player player, String screenKey, Runnable onComplete) {
    this.game = game;
    this.screen = screen;
    this.player = player;
    this.onComplete = onComplete;
    this.player.setInitiative(Dice.roll()); // random initiative every battle

    resolveEnemy(screenKey);

    // Arrow icon from sprite sheet
    Texture selectIconSheet = game.assets.get("icons/dialogue_ui_sheet.png", Texture.class);
    TextureRegion selectSkillTexture = new TextureRegion(selectIconSheet, 448, 384, 64, 64);
    selectSkillIcon = new Image(selectSkillTexture);

    // Dialogue font
    dialogueFont = FontFactory.generateFont("fonts/DotGothic16-Regular.ttf", 20, Color.WHITE);
  }

  // ---------------------------------------------------------------------------
  // ENEMY RESOLUTION
  // ---------------------------------------------------------------------------

  /**
   * Instantiates the correct enemy based on the story schema's screenKey.
   * Add new cases here as more enemy types are introduced.
   *
   * @param screenKey the identifier  from the MinigameNode in story_schema.json
   */
  private void resolveEnemy(String screenKey) {
    switch (screenKey) {
      case "bandit_battle_minigame" -> {
        float initiative = Dice.roll();
        Texture banditTexture = game.assets.get("scenarios/day1/hobgoblin_perpetrator.png", Texture.class);
        Sound banditAttackSound = game.assets.get("audio/sfx/bandit_sfx/slash.mp3", Sound.class);

        this.enemy = new Bandit(initiative, banditTexture, banditAttackSound);
        this.enemyName = "Bandit";
      }
      default -> throw new RuntimeException(
        "[EntityBattleController] Unknown battle screenKey: '" + screenKey + "'"
      );
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
      tintCell(labelCells[i], i == selectedSkillRow);
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
    
    switch (selectedSkillRow) {
      case 0 -> {
        AttackResult result = player.useCryOfMisery(enemy);
        flavorText = result.flavorText();
      }
      case 1 -> {
        SkillResult result = player.useIntenseAura(enemy);
        flavorText = result.flavorText();
      }
      case 2 -> {
        AttackResult result = player.useLullabyOfObedience(enemy);
        flavorText = result.flavorText();
      }
    }

    screen.updateEnemyHealth();
    addDialogueLine(flavorText, skipAnimConfig, this::onPlayerTurnEnded);
  }

  /**
   * Called immediately after the player's turn text finishes typing out.
   * Checks if the enemy died to end the battle; otherwise, transitions
   * to the enemy's turn logic.
   */
  private void onPlayerTurnEnded() {
    if (!enemy.isAlive()) {
      state = BattleState.BATTLE_END;
      addDialogueLine("\n{COLOR=GREEN}The " + enemyName + " is defeated!{ENDCOLOR}{WAIT=1}", skipAnimConfig, this::endBattle);
    } else {
      executeEnemyTurn();
    }
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
      skipTurn = currentStatus.applyPerTurn(enemy);
      screen.updateEnemyHealth();

      if (currentStatus == StatusEffect.SLEEP) {
        enemySleepCounter++;
        if (enemySleepCounter >= MAX_SLEEP_TURNS) {
          enemy.clearStatusEffect();
          enemySleepCounter = 0;
        }
      } else {
        enemySleepCounter = 0;
      }
    } else {
      enemySleepCounter = 0;
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
        } else {
          // Fallback for other potential enemies
          float dmg = enemy.attack(player);
          flavorText = "The " + enemyName + " attacks for " + dmg + " damage!";
        }
      }
    }
    
    flavorText = "\n{COLOR=RED}It's the " + enemyName + "'s turn!{ENDCOLOR}\n" + flavorText;
    
    screen.updatePlayerHealth();
    
    addDialogueLine(flavorText, skipAnimConfig, () -> {
      if (!enemy.isAlive()) {
        state = BattleState.BATTLE_END;
        addDialogueLine("\n{COLOR=GREEN}The " + enemyName + " is defeated!{ENDCOLOR}{WAIT=1}", skipAnimConfig, this::endBattle);
      } else if (!player.isAlive()) {
        state = BattleState.BATTLE_END;
        addDialogueLine("\n{COLOR=RED}You have been killed...{ENDCOLOR}", skipAnimConfig, this::endBattle);
      } else {
        state = BattleState.PLAYER_TURN;
        addDialogueLine("\n{COLOR=#FFDB51}It's your turn!{ENDCOLOR}", skipAnimConfig, null);
      }
    });
  }

  /**
   * Concludes the battle scenario. Disposes of native UI resources
   * to prevent memory leaks and executes the completion callback 
   * to transition back to the overworld or previous screen.
   */
  private void endBattle() {
    // TODO: Handle when player dies and should show a popup screen to retry or go back to main menu
    screen.dispose();
    
    if (onComplete != null) {
      onComplete.run();
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
        tintCell(labelCells[i], true);
      } else {
        tintCell(labelCells[i], false);
      }

      float bottomPad = (i < SKILL_COUNT - 1) ? 10f : 0f;
      skillWidgetTable.add(iconCells[i]).size(30f).padRight(20f).padBottom(bottomPad);
      skillWidgetTable.add(labelCells[i]).growX().align(Align.left | Align.center).padBottom(bottomPad).row();
    }
  }

  /**
   * Applies a yellow highlight to the selected cell or resets it to white.
   *
   * @param cell       the container whose actor should be tinted
   * @param isSelected true to highlight, false to reset to white
   */
  private void tintCell(Container<?> cell, boolean isSelected) {
    if (cell.getActor() == null) return;
    cell.getActor().setColor(isSelected ? Color.valueOf("#FFDB51") : Color.WHITE);
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
   */
  public void startFirstTurn() {
    if (player.getInitiative() > enemy.getInitiative()) {
      state = BattleState.PLAYER_TURN;
      addDialogueLine("\n{COLOR=#FFDB51}It's your turn!{ENDCOLOR}", skipAnimConfig, null);
    } else {
      executeEnemyTurn();
    }
  }

  /**
   * Adds a new line of text to the dialogue box using a TypingLabel.
   * Bubbles up existing text by aligning to the bottom, pushing older lines up.
   * If the lines overflow the height, older lines are trimmed.
   *
   * @param text     The text to display.
   * @param skipAnim If true, skips the typing animation.
   * @param onComplete Optional callback executed when typing animation finishes
   */
  public void addDialogueLine(String text, boolean skipAnim, Runnable onComplete) {
    if (textDialogueTable == null) return;

    TypingLabel typingLabel = new TypingLabel(text, dialogueFont);
    typingLabel.setWrap(true);

    if (skipAnim) {
      typingLabel.skipToTheEnd();
      if (onComplete != null) {
        Gdx.app.postRunnable(onComplete);
      }
    } else if (onComplete != null) {
      typingLabel.setTypingListener(new TypingAdapter() {
        @Override
        public void end() {
          onComplete.run();
        }
      });
    }

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
    if (textDialogueTable == null) return;
    
    // Force immediate table layout so we can query exact allocated dimensions
    textDialogueTable.invalidateHierarchy();
    textDialogueTable.validate();

    float tableHeight = textDialogueTable.getHeight();
    if (tableHeight <= 0) {
      pendingOverflowTrim = true; // screen layout not ready yet, retry next frame
      return;
    }

    // Validate that all actors have computed their physical heights.
    // For wrapped text, width must be established before height is accurate.
    for (var cell : textDialogueTable.getCells()) {
      if (!cell.hasActor()) continue;
      
      // Force label layout to register wrap boundaries
      if (cell.getActor() instanceof com.badlogic.gdx.scenes.scene2d.ui.Widget widget) {
        widget.validate(); 
      }
      
      if (cell.getActorHeight() <= 0) {
        pendingOverflowTrim = true; // wait for LibGDX layout frame to settle
        return;
      }
    }

    pendingOverflowTrim = false;

    while (true) {
      // Calculate true content height exclusively for active cells
      float contentHeight = textDialogueTable.getPadTop() + textDialogueTable.getPadBottom();
      int activeActorCount = 0;

      for (var cell : textDialogueTable.getCells()) {
        if (!cell.hasActor()) continue;
        activeActorCount++;
        contentHeight += cell.getActorHeight() + cell.getPadTop() + cell.getPadBottom();
      }

      // If everything fits OR we only have 1 active line left, we are done
      if (contentHeight <= tableHeight || activeActorCount <= 1) {
        break; 
      }

      // Find the oldest valid actor and remove it
      Actor oldestActor = null;
      for (var cell : textDialogueTable.getCells()) {
        if (cell.hasActor()) {
          oldestActor = cell.getActor();
          break; // only remove one per loop iteration
        }
      }

      if (oldestActor != null) {
        com.badlogic.gdx.utils.Array<Actor> activeActors = new com.badlogic.gdx.utils.Array<>();
        for (var cell : textDialogueTable.getCells()) {
          if (cell.hasActor() && cell.getActor() != oldestActor) {
            activeActors.add(cell.getActor());
          }
        }
        
        textDialogueTable.clearChildren();
        
        for (Actor a : activeActors) {
          textDialogueTable.add(a).growX().bottom().left().padBottom(5f).row();
        }
      }

      // Re-layout explicitly after removal to update cell dependencies
      textDialogueTable.invalidateHierarchy();
      textDialogueTable.validate();
    }
  }
}
