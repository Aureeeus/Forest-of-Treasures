package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.View.Day1Screen;

public class Day1Controller {
  private final GameLauncher game;
  private final Day1Screen screen;

  private Image settingsIcon;

  public Day1Controller(GameLauncher game, Day1Screen screen) {
    this.game = game;
    this.screen = screen; 

    settingsIcon = screen.getSettingsIcon();

    addListeners();
  }

  private void addListeners() {
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
}
