package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.Day1Controller;
import edu.tip.forestoftreasures.utils.DrawableMaker;

public class Day1Screen implements Screen {
  private final Stage stage;
  private final GameLauncher game;

  // Assets managed by this screen
  private final Texture settingsTexture;

  // UI References
  private Table table;
  private Image settingsIcon;
  private Table leftScenarioContentTable;
  private Table leftDialogueContentTable;
  private Table topDialogueContentTable;
  private Table bottomDialogueContentTable;

  // --- Right Panel Color Configuration ---
  Color rightBorderColor = Color.valueOf("#2a2a2a");
  Color rightContentColor = Color.valueOf("#212121");

  // --- Left Panel Color Configuration ---
  Color leftBorderColor = Color.valueOf("#2a2a2a");
  Color leftContentColor = Color.valueOf("#191a1c");

  public Day1Screen(GameLauncher game) {
    this.game = game;

    settingsTexture = new Texture(Gdx.files.internal("icons/Gear.png"));
    stage = new Stage(new ScreenViewport());
  }

  @Override
  public void show() {
    // Prepare your screen here.
    Gdx.input.setInputProcessor(stage);

    // Add actor to stage
    table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    // Create a subtable for player stats, skills, and settings of the game
    // Define right table background and content drawables
    Drawable rightBorderBg = DrawableMaker.getColoredDrawable(rightBorderColor);
    Drawable rightContentBg = DrawableMaker.getColoredDrawable(rightContentColor);

    // Right Content table
    Table rightContentTable = new Table();
    rightContentTable.setBackground(rightContentBg);

    // Right Border table
    Table rightBorderTable = new Table();
    rightBorderTable.setBackground(rightBorderBg);
    rightBorderTable.add(rightContentTable).grow().pad(3f);

    // Adding UI elements to the right content table
    settingsIcon = new Image(settingsTexture);
    rightContentTable.add(settingsIcon)
        .size(70f)
        .expand()
        .top()
        .right()
        .pad(20f);

    // Create a subtable for game image scenario and text dialogue box
    // Define left table background and content drawables
    Drawable leftContentBg = DrawableMaker.getColoredDrawable(leftContentColor);
    Drawable leftBorderBg = DrawableMaker.getColoredDrawable(leftBorderColor);

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

    bottomDialogueContentTable = new Table();
    bottomDialogueContentTable.setBackground(leftContentBg);

    // Adding top and bottom spaces to the dialogueContentTable
    leftDialogueContentTable.add(topDialogueContentTable)
        .grow()
        .row();
    leftDialogueContentTable.add(bottomDialogueContentTable)
        .grow();

    Table leftDialogueBorderTable = new Table();
    leftDialogueBorderTable.setBackground(leftBorderBg);
    leftDialogueBorderTable.add(leftDialogueContentTable).grow().pad(3f);

    // Table for left panel
    Table leftContainer = new Table();
    leftContainer.pad(100f);

    leftContainer.add(leftScenarioBorderTable)
        .size(500f, 500f)
        .padBottom(50f)
        .row();
    leftContainer.add(leftDialogueBorderTable)
        .growX()
        .height(Value.percentHeight(0.4f, leftContainer));

    // Add left table and right table to the main table layout
    table.add(leftContainer)
        .expand()
        .fill()
        .width(Value.percentWidth(0.7f, table));
    table.add(rightBorderTable)
        .expand()
        .fill()
        .width(Value.percentWidth(0.3f, table));

    // Create controller and pass the screen to it
    new Day1Controller(game, this);
  }

  @Override
  public void render(float delta) {
    // Update and draw your screen here.
    ScreenUtils.clear(Color.valueOf("#121315"));

    stage.act(delta);
    stage.draw();
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
  }

  @Override
  public void pause() {
    // Handle game pause here.
  }

  @Override
  public void resume() {
    // Handle game resume here.
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
    settingsTexture.dispose();
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
}
