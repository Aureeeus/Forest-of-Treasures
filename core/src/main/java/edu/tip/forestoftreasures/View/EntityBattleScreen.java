package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;

import edu.tip.forestoftreasures.Controller.EntityBattleController;
import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Model.entities.Entity;
import edu.tip.forestoftreasures.Model.entities.Player;
import edu.tip.forestoftreasures.utils.DrawableFactory;
import edu.tip.forestoftreasures.utils.FontFactory;

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

  public EntityBattleScreen(GameLauncher game, String screenKey, Player player, Runnable onComplete) {
    this.game = game;
    statsFont = FontFactory.generateFont("fonts/PressStart2P-Regular.ttf", 32, Color.WHITE);
    controller = new EntityBattleController(this.game, this, player, screenKey, onComplete);
  }

  @Override
  public void show() {
    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);

    borderBg = DrawableFactory.getColoredDrawable(borderColor);
    contentBg = DrawableFactory.getColoredDrawable(contentColor);

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
  }

  @Override
  public void dispose() {
    stage.dispose();
  }

  // ---------------------------------------------------------------------------
  // UPDATE METHODS
  // ---------------------------------------------------------------------------

  public void updateEnemyHealth() {
    if (enemyHpLabel != null && controller != null) {
      enemyHpLabel.setText("Health: " + formatStat(controller.getEnemy().getHp()));
    }
  }

  public void updatePlayerHealth() {
    if (playerHpLabel != null && controller != null) {
      playerHpLabel.setText("Health: " + formatStat(controller.getPlayer().getHp()));
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

    enemyHpLabel = new TextraLabel(
      "Health: " + formatStat(enemy.getHp()), statsFont);
    statsTable.add(enemyHpLabel)
      .left()
      .padBottom(10f)
      .row();

    TextraLabel strengthLabel = new TextraLabel(
      "Strength: " + formatStat(enemy.getStrength()), statsFont);
    statsTable.add(strengthLabel)
      .left()
      .padBottom(10f)
      .row();

    TextraLabel initiativeLabel = new TextraLabel(
      "Initiative: " + formatStat(enemy.getInitiative()), statsFont);
    statsTable.add(initiativeLabel)
      .left()
      .row();

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

    playerHpLabel = new TextraLabel(
      "Health: " + formatStat(player.getHp()), statsFont);
    statsTable.add(playerHpLabel)
      .left()
      .padBottom(10f)
      .row();

    TextraLabel intelligenceLabel = new TextraLabel(
      "Intelligence: " + formatStat(player.getIntelligence()), statsFont);
    statsTable.add(intelligenceLabel)
      .left()
      .padBottom(10f)
      .row();

    TextraLabel initiativeLabel = new TextraLabel(
      "Initiative: " + formatStat(player.getInitiative()), statsFont);
    statsTable.add(initiativeLabel)
      .left()
      .row();

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
