package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

public class mazeBossController {
  public Vector2 playerPosition;
  public float playerSpeed = 800f;
  private int tileWidth = 64;

  public mazeBossController() {
    playerPosition = new Vector2(2000, 2000); // Initial position
  }

  public void movement(float deltaTime, TiledMap map) {
    TiledMapTileLayer collisionLayer = (TiledMapTileLayer) map.getLayers().get("Walls");
    if (collisionLayer == null)
      return;

    float oldX = playerPosition.x;
    float oldY = playerPosition.y;

    // --- Handle X Movement ---
    if (Gdx.input.isKeyPressed(Input.Keys.A))
      playerPosition.x -= playerSpeed * deltaTime;
    if (Gdx.input.isKeyPressed(Input.Keys.D))
      playerPosition.x += playerSpeed * deltaTime;

    if (isColliding(playerPosition.x, playerPosition.y, collisionLayer)) {
      playerPosition.x = oldX; // Revert X only
    }

    // --- Handle Y Movement ---
    if (Gdx.input.isKeyPressed(Input.Keys.W))
      playerPosition.y += playerSpeed * deltaTime;
    if (Gdx.input.isKeyPressed(Input.Keys.S))
      playerPosition.y -= playerSpeed * deltaTime;

    if (isColliding(playerPosition.x, playerPosition.y, collisionLayer)) {
      playerPosition.y = oldY;
      playerPosition.x = oldX;
    }
  }

  private boolean isColliding(float x, float y, TiledMapTileLayer layer) {
    if (layer == null)
      return false;

    // Check the center point of your player
    // Based on your 64px tiles, we check 32 pixels in
    int cellX = (int) (x);
    int cellY = (int) (y);

    // Safety check to stay inside the map boundaries
    if (cellX < 0 || cellX >= layer.getWidth() || cellY < 0 || cellY >= layer.getHeight()) {
      return false;
    }

    TiledMapTileLayer.Cell cell = layer.getCell(cellX, cellY);

    // Return true only if there is a tile and it has the "Blocked" property
    return cell != null &&
        cell.getTile() != null &&
        cell.getTile().getProperties().containsKey("Blocked");
  }
}
