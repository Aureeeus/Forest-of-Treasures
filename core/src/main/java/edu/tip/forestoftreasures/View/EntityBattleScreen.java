package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.utils.DrawableFactory;

public class EntityBattleScreen implements Screen {
  // Reference to the GameLauncher which holds the AssetManager for loading textures, etc.
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

  // Border and Content Color Configuration ---
  private Color borderColor = Color.valueOf("#2a2a2a");
  private Color contentColor = Color.valueOf("#191a1c");

  // Border and Content Drawables;
  private Drawable borderBg;
  private Drawable contentBg;

  public EntityBattleScreen(GameLauncher game) {
    this.game = game;
  }

  @Override
  public void show() {
    // Prepare your screen here
    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);

    // Set border and content color of boxes
    borderBg = DrawableFactory.getColoredDrawable(borderColor);
    contentBg = DrawableFactory.getColoredDrawable(contentColor);

    // Show user interface to the player
    drawUI();
  }

  @Override
  public void render(float delta) {
    // Draw your screen here. "delta" is the time since last render in seconds.
    ScreenUtils.clear(Color.valueOf("#121315"));

    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
    // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
    if(width <= 0 || height <= 0) return;

    // Resize your screen here. The parameters represent the new window size.
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void pause() {
    // Invoked when your application is paused.
  }

  @Override
  public void resume() {
    // Invoked when your application is resumed after pause.
  }

  @Override
  public void hide() {
    // This method is called when another screen replaces this one.
    Gdx.input.setInputProcessor(null);
  }

  @Override
  public void dispose() {
    // Destroy screen's assets here.
    stage.dispose();
  }

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
    // Border instantiation
    enemyStatsBorderTable = new Table();
    enemyStatsBorderTable.pad(3f);
    enemyStatsBorderTable.setBackground(borderBg);

    // Content instantiation
    enemyStatsContentTable = new Table();
    enemyStatsContentTable.setBackground(contentBg);

    // Add content to border table
    enemyStatsBorderTable.add(enemyStatsContentTable)
      .grow();

    rightPanelTable.add(enemyStatsBorderTable)
      .grow()
      .padBottom(50f)
      .row();
  }

  private void drawPlayerStatsBox() {
    // Border instantiation
    playerStatsBorderTable = new Table();
    playerStatsBorderTable.pad(3f);
    playerStatsBorderTable.setBackground(borderBg);

    // Content instantiation
    playerStatsContentTable = new Table();
    playerStatsContentTable.setBackground(contentBg);

    // Add content to border table
    playerStatsBorderTable.add(playerStatsContentTable)
      .grow();

    rightPanelTable.add(playerStatsBorderTable)
      .grow();
  }

  private void drawScenarioImageBox() {
    // Border instantiation
    scenarioImageBorderTable = new Table();
    scenarioImageBorderTable.pad(3f);
    scenarioImageBorderTable.setBackground(borderBg);

    // Content instantiation
    scenarioImageContentTable = new Table();
    scenarioImageContentTable.setBackground(contentBg);

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

    // Add content to border table
    textDialogueBorderTable.add(textDialogueContentTable)
      .grow();

    leftPanelTable.add(textDialogueBorderTable)
      .grow();
  }
}
