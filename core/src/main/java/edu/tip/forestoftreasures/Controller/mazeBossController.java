package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class mazeBossController {
  public Vector2 playerPosition;
  public float playerSpeed = 200f;

  public mazeBossController() {
    playerPosition = new Vector2(100, 100); // Initial position
  }

  public void movement(float deltaTime) { // basic player movement using WASD keys
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      playerPosition.y += playerSpeed * deltaTime; // Move up
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      playerPosition.y -= playerSpeed * deltaTime; // Move down
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      playerPosition.x -= playerSpeed * deltaTime; // Move left
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      playerPosition.x += playerSpeed * deltaTime; // Move right
    }
  }
}
