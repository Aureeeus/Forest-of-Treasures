package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.DayController;
import edu.tip.forestoftreasures.Model.SaveData;
import edu.tip.forestoftreasures.Model.entities.Player;
import edu.tip.forestoftreasures.utils.DrawableFactory;
import edu.tip.forestoftreasures.utils.FontFactory;
import edu.tip.forestoftreasures.utils.StatFeedbackUtils;

public class DayScreen implements Screen {
  private final Stage stage;
  private final GameLauncher game;
  private boolean isInitialized = false;
  
  // Assets managed by this screen (disposable)
  private final Texture sheet;
  private final Texture settingsTexture;
  private final Texture dexterityTexture;
  private final Texture skill1Texture;
  private final Texture skill2Texture;
  private final Texture skill3Texture;
  
  private final TextureRegion heartTexture;
  private final TextureRegion strengthTexture;
  private final TextureRegion intelligenceTexture;
  private final TextureRegion charismateTexture;

  private final Font playerStatsTitleFont;
  private final Font playerStatsTextFont;

  // Game States
  private final Player player;

  // UI References
  private Table table;
  private Image settingsIcon;
  private Table leftScenarioContentTable;
  private Table leftDialogueContentTable;
  private Table topDialogueContentTable;
  private Table bottomDialogueContentTable;
  private DayController controller;

  // Save/Load state passed from MainMenuController
  private SaveData pendingSaveData;

  // Settings Pop-up UI Components
  private Table settingsDialog;
  private Table dimOverlay;
  private TextraLabel returnToMenuButton;
  private TextraLabel resumeButton;
  private TextraLabel saveGameButton;

  // Player stat labels (refreshable after battle)
  private TextraLabel hpLabel;
  private TextraLabel strengthLabel;
  private TextraLabel intelligenceLabel;
  private TextraLabel dexterityLabel;
  private TextraLabel charismaLabel;

  // Track previous stat values for floating feedback
  private float prevHp;
  private float prevStrength;
  private float prevIntelligence;
  private float prevDexterity;
  private float prevCharisma;

  // --- Right Panel Color Configuration ---
  Color rightBorderColor = Color.valueOf("#2a2a2a");
  Color rightContentColor = Color.valueOf("#212121");

  // --- Left Panel Color Configuration ---
  Color leftBorderColor = Color.valueOf("#2a2a2a");
  Color leftContentColor = Color.valueOf("#191a1c");

  public DayScreen(GameLauncher game) {
    this.game = game;
    this.sheet = game.assets.get("icons/stats_icons.png", Texture.class);
    sheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    this.player = game.getPlayer();

    this.playerStatsTitleFont = FontFactory
      .generateFont("fonts/PressStart2P-Regular.ttf", 30, Color.valueOf("#FFDB51"));
    this.playerStatsTextFont = FontFactory.generateFont("fonts/PressStart2p-Regular.ttf", 24, Color.WHITE);

    // Right Panel Textures
    // Player Stats
    this.settingsTexture = game.assets.get("icons/Gear.png", Texture.class);
    this.heartTexture = new TextureRegion(sheet, 0, 32, 32, 32);
    this.strengthTexture = new TextureRegion(sheet, 128, 32, 32, 32);
    this.intelligenceTexture = new TextureRegion(sheet, 96, 32, 32, 32);
    this.dexterityTexture = game.assets.get("icons/dex_icon.png", Texture.class);
    this.charismateTexture = new TextureRegion(sheet, 0, 128, 32, 32);

    // Player Movesets
    this.skill1Texture = game.assets.get("icons/skill1_icon.png", Texture.class);
    this.skill2Texture = game.assets.get("icons/skill2_icon.png", Texture.class);
    this.skill3Texture = game.assets.get("icons/skill3_icon.png", Texture.class);
    
    this.stage = new Stage(new ScreenViewport());

    // Initialize previous stat values to current player stats
    this.prevHp = player.getHp();
    this.prevStrength = player.getStrength();
    this.prevIntelligence = player.getIntelligence();
    this.prevDexterity = player.getDexterity();
    this.prevCharisma = player.getCharisma();
  }

  @Override
  public void show() {
    // Prepare your screen here.
    Gdx.input.setInputProcessor(stage);

    if (!isInitialized) {
      isInitialized = true; // Ensures initialization is done only once

      // Add actor to stage
      table = new Table();
      table.setFillParent(true);
      stage.addActor(table);
  
      // Create a subtable for player stats, skills, and settings of the game
      // Define right table background and content drawables
      Drawable rightBorderBg = DrawableFactory.getColoredDrawable(rightBorderColor);
      Drawable rightContentBg = DrawableFactory.getColoredDrawable(rightContentColor);
  
      // Right Content table
      Table rightContentTable = new Table();
      rightContentTable.setBackground(rightContentBg);
      rightContentTable.align(Align.top); // Starting position of cell insertions
      rightContentTable.pad(40f, 20f, 40f, 20f); // Table content padding
  
      // Right Border table
      Table rightBorderTable = new Table();
      rightBorderTable.setBackground(rightBorderBg);
      rightBorderTable.add(rightContentTable).grow().pad(0f, 3f, 0f, 0f);
      
      // Adding UI elements to the right content table
      settingsIcon = new Image(settingsTexture);
      rightContentTable.add(settingsIcon)
        .colspan(2)
        .size(100f)
        .top()
        .right()
        .row();
  
      // Divider
      Image horizontalLineTop = DrawableFactory.createDivider(Color.WHITE);
      rightContentTable.add(horizontalLineTop)
        .colspan(2)
        .growX()
        .height(5f)
        .pad(20f, 0f, 20f, 0f)
        .row();
  
      // Player Stats UI
      TextraLabel playerStatsLabel = new TextraLabel("PLAYER STATS:", playerStatsTitleFont);
      rightContentTable.add(playerStatsLabel)
        .colspan(2)
        .center()
        .padBottom(10f)
        .row();
  
      Image hpIcon = new Image(heartTexture);
      TextraLabel hpPrefix = new TextraLabel(": ", playerStatsTextFont);
      hpLabel = new TextraLabel(formatStat(player.getHp()), playerStatsTextFont);
      rightContentTable.add(hpIcon).size(90f).left().padRight(10f);
      
      Table hpRowTable = new Table();
      hpRowTable.add(hpPrefix).left();
      hpRowTable.add(hpLabel).left();
      rightContentTable.add(hpRowTable).left().row();
  
      Image strengthIcon = new Image(strengthTexture);
      TextraLabel strengthPrefix = new TextraLabel(": ", playerStatsTextFont);
      strengthLabel = new TextraLabel(formatStat(player.getStrength()), playerStatsTextFont);
      rightContentTable.add(strengthIcon).size(90f).left().padRight(10f);
      
      Table strengthRowTable = new Table();
      strengthRowTable.add(strengthPrefix).left();
      strengthRowTable.add(strengthLabel).left();
      rightContentTable.add(strengthRowTable).left().row();
  
      Image intelligenceIcon = new Image(intelligenceTexture);
      TextraLabel intelligencePrefix = new TextraLabel(": ", playerStatsTextFont);
      intelligenceLabel = new TextraLabel(formatStat(player.getIntelligence()), playerStatsTextFont);
      rightContentTable.add(intelligenceIcon).size(90f).left().padRight(10f);
      
      Table intelligenceRowTable = new Table();
      intelligenceRowTable.add(intelligencePrefix).left();
      intelligenceRowTable.add(intelligenceLabel).left();
      rightContentTable.add(intelligenceRowTable).left().row();
      
      Image dexterityIcon = new Image(dexterityTexture);
      TextraLabel dexterityPrefix = new TextraLabel(": ", playerStatsTextFont);
      dexterityLabel = new TextraLabel(formatStat(player.getDexterity()), playerStatsTextFont);
      rightContentTable.add(dexterityIcon).size(90f).left().padRight(10f);
      
      Table dexterityRowTable = new Table();
      dexterityRowTable.add(dexterityPrefix).left();
      dexterityRowTable.add(dexterityLabel).left();
      rightContentTable.add(dexterityRowTable).left().row();
  
      Image charismaIcon = new Image(charismateTexture);
      TextraLabel charismaPrefix = new TextraLabel(": ", playerStatsTextFont);
      charismaLabel = new TextraLabel(formatStat(player.getCharisma()), playerStatsTextFont);
      rightContentTable.add(charismaIcon).size(90f).left().padRight(10f);
      
      Table charismaRowTable = new Table();
      charismaRowTable.add(charismaPrefix).left();
      charismaRowTable.add(charismaLabel).left();
      rightContentTable.add(charismaRowTable).left().row();
      
      // Divider
      Image horizontalLineBottom = DrawableFactory.createDivider(Color.WHITE);
      rightContentTable.add(horizontalLineBottom)
        .colspan(2)
        .growX()
        .height(5f)
        .pad(20f, 0f, 20f, 0f)
        .row();
  
      // Player Movesets UI
      TextraLabel playerMovesetsLabel = new TextraLabel("MOVESETS:", playerStatsTitleFont);
      rightContentTable.add(playerMovesetsLabel)
        .colspan(2)
        .center()
        .padBottom(10f)
        .row();
  
      Image skill1Icon = new Image(skill1Texture);
      TextraLabel skill1Label = new TextraLabel(": Cry of Misery", playerStatsTextFont);
      rightContentTable.add(skill1Icon)
        .size(90f)
        .left()
        .padBottom(20f);
      rightContentTable.add(skill1Label)
        .growX()
        .left()
        .row();
  
      Image skill2Icon = new Image(skill2Texture);
      TextraLabel skill2Label = new TextraLabel(": Intense Aura", playerStatsTextFont);
      rightContentTable.add(skill2Icon)
        .size(90f)
        .left()
        .padBottom(20f);
        rightContentTable.add(skill2Label)
        .growX()
        .left()
        .row();
  
      Image skill3Icon = new Image(skill3Texture);
      TextraLabel skill3Label = new TextraLabel(": Lullaby of\n   Obedience", playerStatsTextFont);
      skill3Label.setWrap(true);
      rightContentTable.add(skill3Icon)
        .size(90f)
        .left();
      rightContentTable.add(skill3Label)
        .growX()
        .left()
        .row();
      
      // Create a subtable for game image scenario and text dialogue box
      // Define left table background and content drawables
      Drawable leftContentBg = DrawableFactory.getColoredDrawable(leftContentColor);
      Drawable leftBorderBg = DrawableFactory.getColoredDrawable(leftBorderColor);
  
      // Table for game image scenario
      leftScenarioContentTable = new Table();
      leftScenarioContentTable.setBackground(leftContentBg);
  
      Table leftScenarioBorderTable = new Table();
      leftScenarioBorderTable.setBackground(leftBorderBg);
      leftScenarioBorderTable.add(leftScenarioContentTable).grow().pad(5f);
  
      // Table for text dialogue box
      leftDialogueContentTable = new Table();
      leftDialogueContentTable.setBackground(leftContentBg);
      leftDialogueContentTable.pad(15f);
  
      // Split dialogueContentTable into for text space and interactable widgets.
      topDialogueContentTable = new Table();
      topDialogueContentTable.setBackground(leftContentBg);
      topDialogueContentTable.align(Align.bottomLeft); // align cells to bottom left of table
      topDialogueContentTable.pad(0f, 10f, 0f, 10f);
      topDialogueContentTable.setClip(true);
  
      // Space for widgets (player choices)
      bottomDialogueContentTable = new Table();
      bottomDialogueContentTable.setBackground(leftContentBg);
  
      // Adding top and bottom spaces to the dialogueContentTable
      leftDialogueContentTable.add(topDialogueContentTable)
        .height(Value.percentHeight(0.7f, leftDialogueContentTable))
        .growX()
        .row();
      leftDialogueContentTable.add(bottomDialogueContentTable)
        .grow();
  
      Table leftDialogueBorderTable = new Table();
      leftDialogueBorderTable.setBackground(leftBorderBg);
      leftDialogueBorderTable.add(leftDialogueContentTable).grow().pad(3f);
  
      // Table for left panel
      Table leftContainer = new Table();
      leftContainer.pad(50f);
  
      leftContainer.add(leftScenarioBorderTable)
        .size(500f, 500f)
        .padBottom(50f)
        .row();
      leftContainer.add(leftDialogueBorderTable)    
        .grow();
      
      // Add left table and right table to the main table layout
      table.add(leftContainer)
        .growY()
        .width(Value.percentWidth(0.7f, table));
      table.add(rightBorderTable)
        .growY()
        .width(Value.percentWidth(0.3f, table));

      // Load controller to this screen
      controller = new DayController(game, this);
    }
  }

  @Override
  public void render(float delta) {
    // Update and draw your screen here.
    ScreenUtils.clear(Color.valueOf("#121315"));

    stage.act(delta);
    stage.draw();
    
    if (controller != null) {
      controller.update();
    }
  }

  @Override
  public void resize(int width, int height) {
    // Handle screen resizing here.
    // If the window is minimized on a desktop (LWJGL3) platform, width and height
    // are 0, which causes problems.
    // In that case, we don't resize anything, and wait for the window to be a
    // normal size before updating.
    if (width <= 0 || height <= 0)
      return;

    // Resize your screen here. The parameters represent the new window size.
    stage.getViewport().update(width, height, true);

    // Re-center settings dialog if open
    if (settingsDialog != null) {
      settingsDialog.setPosition(
        (stage.getWidth() - settingsDialog.getWidth()) / 2f,
        (stage.getHeight() - settingsDialog.getHeight()) / 2f
      );
    }

    // Re-queue the overflow trim since viewport update invalidates all layouts
    if (controller != null) {
      controller.setPendingOverflowTrim(true);
    }
  }

  @Override
  public void pause() {
    // Handle game pause here.
    if (controller != null) {
      controller.pause();
    }
  }

  @Override
  public void resume() {
    // Handle game resume here.
    if (controller != null) {
      controller.resume();
    }
  }

  @Override
  public void hide() {
    // Handle screen hiding here.
    Gdx.input.setInputProcessor(null);
  }

  @Override
  public void dispose() {
    // Dispose of assets when no longer needed.
    stage.dispose();
    controller.dispose();
  }

  public Image getSettingsIcon() {
    return settingsIcon;
  }

  public Table getScenarioContentTable() {
    return leftScenarioContentTable;
  }

  public Table getTextDialogueTable() {
    return topDialogueContentTable;
  }

  public Table getDialogueWidgetTable() {
    return bottomDialogueContentTable;
  }

  public Stage getStage() {
    return stage;
  }

  public Player getPlayer() {
    return player;
  }

  /**
   * Sets pending save data to be consumed by the DayController on initialization.
   * Called by MainMenuController before transitioning to this screen.
   */
  public void setPendingSaveData(SaveData saveData) {
    this.pendingSaveData = saveData;
  }

  /**
   * Returns and clears any pending save data. The DayController calls this
   * once during initialization to check if it should resume from a saved position.
   *
   * @return The pending SaveData, or null if this is a fresh game.
   */
  public SaveData consumePendingSaveData() {
    SaveData data = this.pendingSaveData;
    this.pendingSaveData = null;
    return data;
  }

  /**
   * Creates and displays a modal settings pop-up window.
   */
  public void showSettingsDialog() {
    if (settingsDialog != null) return;

    // 1. Create dim overlay to focus on the dialog
    dimOverlay = new Table();
    dimOverlay.setFillParent(true);
    dimOverlay.setBackground(DrawableFactory.getColoredDrawable(new Color(0, 0, 0, 0.7f)));
    dimOverlay.setTouchable(Touchable.enabled); // Blocks input to layers below
    stage.addActor(dimOverlay);

    // 2. Create the dialog container
    settingsDialog = new Table();
    settingsDialog.setBackground(DrawableFactory.getColoredDrawable(leftBorderColor));
    settingsDialog.pad(5f);

    Table contentTable = new Table();
    contentTable.setBackground(DrawableFactory.getColoredDrawable(leftContentColor));
    contentTable.pad(40f);
    settingsDialog.add(contentTable).grow();

    // 3. Add Title
    TextraLabel titleLabel = new TextraLabel("[#FFDB51]SETTINGS[]", playerStatsTitleFont);
    contentTable.add(titleLabel).padBottom(40f).row();

    // 4. Add Buttons
    returnToMenuButton = new TextraLabel("RETURN TO MAIN MENU", playerStatsTextFont);
    resumeButton = new TextraLabel("RESUME GAME", playerStatsTextFont);
    saveGameButton = new TextraLabel("SAVE GAME", playerStatsTextFont);

    contentTable.add(resumeButton).padBottom(20f).row();
    contentTable.add(saveGameButton).padBottom(20f).row();
    contentTable.add(returnToMenuButton).row();

    // 5. Position dialog in center
    settingsDialog.setSize(500f, 400f);
    settingsDialog.setPosition(
      (stage.getWidth() - settingsDialog.getWidth()) / 2f,
      (stage.getHeight() - settingsDialog.getHeight()) / 2f
    );
    stage.addActor(settingsDialog);
  }

  /**
   * Removes the settings pop-up and its overlay from the stage.
   */
  public void closeSettingsDialog() {
    if (settingsDialog != null) {
      settingsDialog.remove();
      settingsDialog = null;
    }
    if (dimOverlay != null) {
      dimOverlay.remove();
      dimOverlay = null;
    }
  }

  public TextraLabel getReturnToMenuButton() {
    return returnToMenuButton;
  }

  public TextraLabel getResumeButton() {
    return resumeButton;
  }

  public TextraLabel getSaveGameButton() {
    return saveGameButton;
  }

  /**
   * Refreshes all player stat labels with the current values from the Player model.
   * Called after returning from a battle minigame where stats (especially HP) may have changed.
   */
  public void updatePlayerStats() {
    if (hpLabel != null) {
      float diff = player.getHp() - prevHp;
      if (diff != 0) {
        StatFeedbackUtils.showStatFeedback(
          stage, 
          hpLabel.localToStageCoordinates(new Vector2(0, 0)), 
          diff, 
          playerStatsTextFont
        );
        prevHp = player.getHp();
      }
      hpLabel.setText(formatStat(player.getHp()));
    }
    if (strengthLabel != null) {
      float diff = player.getStrength() - prevStrength;
      if (diff != 0) {
        StatFeedbackUtils.showStatFeedback(
          stage, 
          strengthLabel.localToStageCoordinates(new Vector2(0, 0)), 
          diff, 
          playerStatsTextFont
        );
        prevStrength = player.getStrength();
      }
      strengthLabel.setText(formatStat(player.getStrength()));
    }
    if (intelligenceLabel != null) {
      float diff = player.getIntelligence() - prevIntelligence;
      if (diff != 0) {
        StatFeedbackUtils.showStatFeedback(
          stage, 
          intelligenceLabel.localToStageCoordinates(new Vector2(0, 0)), 
          diff, 
          playerStatsTextFont
        );
        prevIntelligence = player.getIntelligence();
      }
      intelligenceLabel.setText(formatStat(player.getIntelligence()));
    }
    if (dexterityLabel != null) {
      float diff = player.getDexterity() - prevDexterity;
      if (diff != 0) {
        StatFeedbackUtils.showStatFeedback(
          stage, 
          dexterityLabel.localToStageCoordinates(new Vector2(0, 0)), 
          diff, 
          playerStatsTextFont
        );
        prevDexterity = player.getDexterity();
      }
      dexterityLabel.setText(formatStat(player.getDexterity()));
    }
    if (charismaLabel != null) {
      float diff = player.getCharisma() - prevCharisma;
      if (diff != 0) {
        StatFeedbackUtils.showStatFeedback(
          stage, 
          charismaLabel.localToStageCoordinates(new Vector2(0, 0)), 
          diff, 
          playerStatsTextFont
        );
        prevCharisma = player.getCharisma();
      }
      charismaLabel.setText(formatStat(player.getCharisma()));
    }
  }

  /**
   * Formats a stat value: shows 2 decimal places if the value has a
   * fractional part, otherwise displays the whole number only.
   */
  private String formatStat(float value) {
    return (value % 1 == 0)
      ? String.format("%.0f", value)
      : String.format("%.2f", value);
  }
}
