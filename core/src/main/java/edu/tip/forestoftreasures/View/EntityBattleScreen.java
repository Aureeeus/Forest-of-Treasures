package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Music;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;

import edu.tip.forestoftreasures.Controller.EntityBattleController;
import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Model.dialogue.MinigameNode;
import edu.tip.forestoftreasures.Model.entities.Entity;
import edu.tip.forestoftreasures.Model.entities.Player;
import edu.tip.forestoftreasures.utils.DrawableFactory;
import edu.tip.forestoftreasures.utils.FontFactory;
import edu.tip.forestoftreasures.utils.StatFeedbackUtils;

/**
 * View for the entity battle screen. Responsible only for rendering UI.
 * All input handling and game logic is delegated to {@link EntityBattleController}.
 */
public class EntityBattleScreen implements Screen {
  private final GameLauncher game;

  // --- Local variables ---
  private Stage stage;
  private Table screenTable;
  private Table leftPanelTable;
  private Table rightPanelTable;

  // For scenario image container
  private Table scenarioImageBorderTable;
  private Table scenarioImageContentTable;

  // For text dialogue container
  private Table textDialogueBorderTable;
  private Table textDialogueContentTable;

  // For enemy stats container
  private Table enemyStatsBorderTable;
  private Table enemyStatsContentTable;

  // For player stats container
  private Table playerStatsBorderTable;
  private Table playerStatsContentTable;

  // Border and Content Color Configuration
  private Color borderColor = Color.valueOf("#2a2a2a");
  private Color contentColor = Color.valueOf("#191a1c");

  // Border and Content Drawables
  private Drawable borderBg;
  private Drawable contentBg;

  // Fonts (cached by FontFactory — do not dispose per screen)
  private Font statsFont;

  // Controller
  private EntityBattleController controller;

  // UI Components to refresh
  private TextraLabel enemyHpLabel;
  private TextraLabel playerHpLabel;
  private Music battleMusic;
  private float prevPlayerHp;
  private float prevEnemyHp;

  public EntityBattleScreen(GameLauncher game, MinigameNode node, Player player, Runnable onComplete) {
    this.game = game;
    statsFont = FontFactory.generateFont("fonts/PressStart2P-Regular.ttf", 32, Color.WHITE);
    controller = new EntityBattleController(this.game, this, player, node, onComplete);
  }

  @Override
  public void show() {
    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);

    borderBg = DrawableFactory.getColoredDrawable(borderColor);
    contentBg = DrawableFactory.getColoredDrawable(contentColor);

    try {
        battleMusic = game.assets.get("audio/sfx/Battle_Screen_Music.mp3", Music.class);
        if (battleMusic != null) {
            battleMusic.setLooping(true);
            battleMusic.setVolume(game.settingsConfig.getGameSettings().bgMusicVolume());
            battleMusic.play();
        }
    } catch (Exception e) {
        // Skip playing if not properly loaded
    }

    if (controller != null && controller.getPlayer() != null) {
        prevPlayerHp = controller.getPlayer().getHp();
    }
    if (controller != null && controller.getEnemy() != null) {
        prevEnemyHp = controller.getEnemy().getHp();
    }

    drawUI();
  }

  @Override
  public void render(float delta) {
    ScreenUtils.clear(Color.valueOf("#121315"));

    controller.handleInput();

    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    if(width <= 0 || height <= 0) return;
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {
    Gdx.input.setInputProcessor(null);
    if (battleMusic != null) {
      battleMusic.stop(); // Ensured to restart from the beginning next time
    }
  }

  @Override
  public void dispose() {
    stage.dispose();
    if (battleMusic != null) {
      battleMusic.stop();
      // Note: We do not call battleMusic.dispose() here because it is managed 
      // by the AssetManager. Manually disposing it would break future lookups.
      battleMusic = null;
    }
  }

  // ---------------------------------------------------------------------------
  // UPDATE METHODS
  // ---------------------------------------------------------------------------

  public void updateEnemyHealth() {
    if (enemyHpLabel != null && controller != null) {
      float currentHp = controller.getEnemy().getHp();
      float diff = currentHp - prevEnemyHp;
      if (diff != 0) {
        StatFeedbackUtils.showStatFeedback(stage, enemyHpLabel.localToStageCoordinates(new Vector2(0, 0)), diff, statsFont);
        prevEnemyHp = currentHp;
      }
      enemyHpLabel.setText(formatStat(currentHp));
    }
  }

  public void updatePlayerHealth() {
    if (playerHpLabel != null && controller != null) {
      float currentHp = controller.getPlayer().getHp();
      float diff = currentHp - prevPlayerHp;
      if (diff != 0) {
        StatFeedbackUtils.showStatFeedback(stage, playerHpLabel.localToStageCoordinates(new Vector2(0, 0)), diff, statsFont);
        prevPlayerHp = currentHp;
      }
      playerHpLabel.setText(formatStat(currentHp));
    }
  }

  // ---------------------------------------------------------------------------
  // UI DRAWING
  // ---------------------------------------------------------------------------

  private void drawUI() {
    screenTable = new Table();
    screenTable.setFillParent(true);
    stage.addActor(screenTable);

    drawLeftPanel();
    drawRightPanel();
  }

  private void drawLeftPanel() {
    leftPanelTable = new Table();
    leftPanelTable.pad(40f);
    leftPanelTable.align(Align.left);

    screenTable.add(leftPanelTable)
      .growY()
      .width(Value.percentWidth(0.5f, screenTable));

    drawScenarioImageBox();
    drawTextDialogueBox();
  }

  private void drawRightPanel() {
    rightPanelTable = new Table();
    rightPanelTable.pad(40f);

    screenTable.add(rightPanelTable)
      .growY()
      .width(Value.percentWidth(0.5f, screenTable));

    drawEnemyStatsBox();
    drawPlayerStatsBox();
  }

  private void drawEnemyStatsBox() {
    Entity enemy = controller.getEnemy();

    // Border instantiation
    enemyStatsBorderTable = new Table();
    enemyStatsBorderTable.pad(3f);
    enemyStatsBorderTable.setBackground(borderBg);

    // Content instantiation
    enemyStatsContentTable = new Table();
    enemyStatsContentTable.setBackground(contentBg);
    enemyStatsContentTable.pad(50f);

    // Enemy name (top-center, does not expand vertically)
    TextraLabel nameLabel = new TextraLabel(controller.getEnemyName().toUpperCase(), statsFont);  // Uppercase format for enemy stats name
    enemyStatsContentTable.add(nameLabel)
      .center()
      .expandX()
      .padBottom(20f)
      .row();

    // Stats container (vertically centered in remaining space, left-aligned)
    Table statsTable = new Table();

    Table hpRowTable = new Table();
    TextraLabel enemyHpPrefix = new TextraLabel("Health: ", statsFont);
    enemyHpLabel = new TextraLabel(formatStat(enemy.getHp()), statsFont);
    hpRowTable.add(enemyHpPrefix).left();
    hpRowTable.add(enemyHpLabel).left();
    statsTable.add(hpRowTable).left().padBottom(10f).row();

    Table strengthRowTable = new Table();
    TextraLabel strengthPrefix = new TextraLabel("Strength: ", statsFont);
    TextraLabel strengthLabel = new TextraLabel(formatStat(enemy.getStrength()), statsFont);
    strengthRowTable.add(strengthPrefix).left();
    strengthRowTable.add(strengthLabel).left();
    statsTable.add(strengthRowTable).left().padBottom(10f).row();

    Table initiativeRowTable = new Table();
    TextraLabel initiativePrefix = new TextraLabel("Initiative: ", statsFont);
    TextraLabel initiativeLabel = new TextraLabel(formatStat(enemy.getInitiative()), statsFont);
    initiativeRowTable.add(initiativePrefix).left();
    initiativeRowTable.add(initiativeLabel).left();
    statsTable.add(initiativeRowTable).left().row();

    enemyStatsContentTable.add(statsTable)
      .expand()
      .center()
      .left()
      .row();

    // Add content to border table
    enemyStatsBorderTable.add(enemyStatsContentTable)
      .grow();

    rightPanelTable.add(enemyStatsBorderTable)
      .grow()
      .uniformY()
      .padBottom(50f)
      .row();
  }

  private void drawPlayerStatsBox() {
    Player player = controller.getPlayer();

    // Border instantiation
    playerStatsBorderTable = new Table();
    playerStatsBorderTable.pad(3f);
    playerStatsBorderTable.setBackground(borderBg);

    // Content instantiation
    playerStatsContentTable = new Table();
    playerStatsContentTable.setBackground(contentBg);
    playerStatsContentTable.pad(50f);

    // Player name (top-center, does not expand vertically)
    TextraLabel nameLabel = new TextraLabel("PLAYER", statsFont);
    playerStatsContentTable.add(nameLabel)
      .center()
      .expandX()
      .colspan(2)
      .padBottom(20f)
      .row();

    // Stats container (vertically centered in remaining space, left-aligned)
    Table statsTable = new Table();

    Table hpRowTable = new Table();
    TextraLabel playerHpPrefix = new TextraLabel("Health: ", statsFont);
    playerHpLabel = new TextraLabel(formatStat(player.getHp()), statsFont);
    hpRowTable.add(playerHpPrefix).left();
    hpRowTable.add(playerHpLabel).left();
    statsTable.add(hpRowTable).left().padBottom(10f).row();

    Table intelligenceRowTable = new Table();
    TextraLabel intelligencePrefix = new TextraLabel("Intelligence: ", statsFont);
    TextraLabel intelligenceLabel = new TextraLabel(formatStat(player.getIntelligence()), statsFont);
    intelligenceRowTable.add(intelligencePrefix).left();
    intelligenceRowTable.add(intelligenceLabel).left();
    statsTable.add(intelligenceRowTable).left().padBottom(10f).row();

    Table initiativeRowTable = new Table();
    TextraLabel initiativePrefix = new TextraLabel("Initiative: ", statsFont);
    TextraLabel initiativeLabel = new TextraLabel(formatStat(player.getInitiative()), statsFont);
    initiativeRowTable.add(initiativePrefix).left();
    initiativeRowTable.add(initiativeLabel).left();
    statsTable.add(initiativeRowTable).left().row();

    playerStatsContentTable.add(statsTable)
      .expand()
      .center()
      .left()
      .colspan(2)
      .padBottom(20f)
      .row();

    // Movesets header (centered, same layout as player name)
    TextraLabel movesetsHeader = new TextraLabel("MOVESETS:", statsFont);
    playerStatsContentTable.add(movesetsHeader)
      .center()
      .expandX()
      .colspan(2)
      .padBottom(20f)
      .row();
      
    // Skill selection widget (rendered by the controller)
    controller.renderSkillWidgets(playerStatsContentTable, statsFont);
      
    // Add content to border table
    playerStatsBorderTable.add(playerStatsContentTable)
      .grow();

    rightPanelTable.add(playerStatsBorderTable)
      .grow()
      .uniformY();
  }

  private void drawScenarioImageBox() {
    // Border instantiation
    scenarioImageBorderTable = new Table();
    scenarioImageBorderTable.pad(3f);
    scenarioImageBorderTable.setBackground(borderBg);

    // Content instantiation
    scenarioImageContentTable = new Table();
    scenarioImageContentTable.setBackground(contentBg);

    Entity enemy = controller.getEnemy();
    if (enemy.getTexture() != null) {
      Image enemyImage = new Image(enemy.getTexture());
      enemyImage.setScaling(Scaling.fit);
      scenarioImageContentTable.add(enemyImage).grow();
    }

    // Add content to border table
    scenarioImageBorderTable.add(scenarioImageContentTable)
      .grow();

    leftPanelTable.add(scenarioImageBorderTable)
      .size(500f)
      .padBottom(50f)
      .row();
  }

  private void drawTextDialogueBox() {
    // Border instantiation
    textDialogueBorderTable = new Table();
    textDialogueBorderTable.pad(3f);
    textDialogueBorderTable.setBackground(borderBg);

    // Content instantiation
    textDialogueContentTable = new Table();
    textDialogueContentTable.setBackground(contentBg);
    textDialogueContentTable.pad(10f, 25f, 10f, 25f); // padding top, left, bottom, right
    textDialogueContentTable.align(Align.bottomLeft);
    textDialogueContentTable.setClip(true);

    // Provide the dialogue table to the controller so it can manage lines
    controller.setTextDialogueTable(textDialogueContentTable);
    controller.addDialogueLine(controller.getInitiativeFlavorText(), controller::startFirstTurn);

    // Add content to border table
    textDialogueBorderTable.add(textDialogueContentTable)
      .grow();

    leftPanelTable.add(textDialogueBorderTable)
      .grow();
  }

  /**
   * Formats a stat value: shows 2 decimal places if the value has a
   * fractional part, otherwise displays the whole number only.
   *
   * @param value the stat value to format
   * @return the formatted string
   */
  private String formatStat(float value) {
    return (value % 1 == 0)
      ? String.format("%.0f", value)
      : String.format("%.2f", value);
  }
}
