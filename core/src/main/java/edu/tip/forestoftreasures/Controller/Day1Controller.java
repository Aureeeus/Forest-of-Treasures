package edu.tip.forestoftreasures.Controller;

import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TypingAdapter;
import com.github.tommyettinger.textra.TypingLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.View.Day1Screen;

public class Day1Controller {
  private final GameLauncher game;
  private final Day1Screen screen;

  // Important game-related variables
  private final Queue<String> dialogueQueue = new LinkedList<>();
  private boolean isTyping = false; // flag to check for dialogue box

  // UI resources
  private Image settingsIcon;
  private final Table scenarioContentTable;
  private final Table textDialogueTable;
  private final Table dialogueWidgetTable;

  // Textures and Fonts
  private Texture scenarioTexture;
  private final Font textFont;

  public Day1Controller(GameLauncher game, Day1Screen screen) {
    this.game = game;
    this.screen = screen; 

    this.scenarioContentTable = screen.getScenarioContentTable();
    this.textDialogueTable = screen.getTextDialogueTable();
    this.dialogueWidgetTable = screen.getDialogueWidgetTable();
    this.settingsIcon = screen.getSettingsIcon();

    // Convert BitmapFont to TextraTypist Font
    BitmapFont baseFont = this.game.assets.get("fonts/DotGothic16-Dialogue.fnt", BitmapFont.class);
    this.textFont = new Font(Gdx.files.internal("fonts/DotGothic16-Dialogue.fnt"));

    addListeners();
    playScenario();
  }

  // Function to add listener for widgets
  private void addListeners() {
    // Event listener for settings icon
    screen.getSettingsIcon().addListener(new ClickListener() {
      // Event handler for when the settings icon is clicked
      @Override
      public void clicked(InputEvent event, float x, float y)  {
        Gdx.app.log("Day1Controller", "Settings icon clicked!");
      }

      // Event handler for when the settings icon is pressed down
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        settingsIcon.setColor(Color.valueOf("#808080"));
        return super.touchDown(event, x, y, pointer, button);
      }

      // Event handler for when the settings icon is released
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        settingsIcon.setColor(Color.WHITE);
        super.touchUp(event, x, y, pointer, button);
      }

      // Event handler for when the mouse cursor enters the settings icon area
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (pointer == -1) {
          settingsIcon.setColor(Color.valueOf("#c7c7c7"));
        }
        super.enter(event, x, y, pointer, fromActor);
      }

      // Event handler for when the mouse cursor exits the settings icon area
      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (pointer == -1) {
          settingsIcon.setColor(Color.WHITE);
        }
        super.exit(event, x, y, pointer, toActor);
      }
    });
  }

  // Scenario initializer when game starts
  private void playScenario() {
    // Load intial image to the scenario box
    scenarioTexture = new Texture(Gdx.files.internal("scenarios/day1/forest_intro.png"));
    Image scenarioImage = new Image(scenarioTexture);
    scenarioContentTable.add(scenarioImage).expand().fill();

    // Load initial story lines to the dialogue box
    addDialogue("The forest seems normal. Goblins, bugs, and fairies roam the forest. Nothing out of the ordinary. The forest is still lit by the sun - probably because its high noon and the trees, so far, are normal.");
    addDialogue("{WAIT=1}\nA sprite lands on my shoulder. Rumors say these sprites have been blessed, and their touch can grant blessings or take them away from me. Should I shoo it away?");
  }

  // Adds text dialogue to textDialogueTable
  private void addDialogue(String text) {
    dialogueQueue.add(text);
    if (!isTyping) showNextDialogue();
  }

  private void showNextDialogue() {
    // Check if there is no queued text to display
    if (dialogueQueue.isEmpty()) {
      isTyping = false;
      return;
    }

    isTyping = true;
    String text = dialogueQueue.poll();

    TypingLabel typingLabel = new TypingLabel(text, textFont);
    typingLabel.setWrap(true);

    // (IMPORTANT) listener to trigger to the next line
    typingLabel.setTypingListener(new TypingAdapter() {
      @Override
      public void end() {
        showNextDialogue(); // Recursive call to show consequent lines.
      }
    });

    // Add to dialogue table
    textDialogueTable.add(typingLabel)
      .growX()
      .bottom()
      .left()
      .padBottom(5f)
      .row();
    
    textDialogueTable.invalidateHierarchy();
    textDialogueTable.layout();
  }

  // Dispose memory resources once day1 is done
  private void dispose() {
    scenarioTexture.dispose();
  }
}
