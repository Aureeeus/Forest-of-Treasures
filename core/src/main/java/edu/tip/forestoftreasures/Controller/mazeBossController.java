package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

public class mazeBossController {
  public Vector2 playerPosition;
  public float playerSpeed = 125f;
  // 32px (Original Tile) * 0.6f (Scale) = 19.2f
  private final float TILE_SIZE = 19.2f;
  private final float PLAYER_SIZE = 48f;

  public mazeBossController() {
    // Start position (Adjust as needed)
    playerPosition = new Vector2(40, 700);
  }

  public void movement(float deltaTime, TiledMap map) {
    TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Walls");
    if (layer == null)
      return;

    float oldX = playerPosition.x;
    float oldY = playerPosition.y;

    // X Movement
    if (Gdx.input.isKeyPressed(Input.Keys.A))
      playerPosition.x -= playerSpeed * deltaTime;
    if (Gdx.input.isKeyPressed(Input.Keys.D))
      playerPosition.x += playerSpeed * deltaTime;

    if (isColliding(playerPosition.x, playerPosition.y, layer)) {
      playerPosition.x = oldX;
    }

    // Y Movement
    if (Gdx.input.isKeyPressed(Input.Keys.W))
      playerPosition.y += playerSpeed * deltaTime;
    if (Gdx.input.isKeyPressed(Input.Keys.S))
      playerPosition.y -= playerSpeed * deltaTime;

    if (isColliding(playerPosition.x, playerPosition.y, layer)) {
      playerPosition.y = oldY;
    }
  }

  private boolean isColliding(float x, float y, TiledMapTileLayer layer) {
    // We create a hitbox that is slightly smaller than the 48px sprite
    // so the player can actually fit through tight paths.
    float hitboxWidth = 16f;
    float hitboxHeight = 16f; // Low height allows "depth" (overlap leaves)
    float offsetX = 12f;
    float offsetY = 12f; // Positioned at the feet

    // SCANNING LOGIC:
    // Because your tree is 1 tile in the map but visually large, we scan
    // the area of the player to see if ANY part of them hits a wall tile.
    for (float stepX = 0; stepX <= hitboxWidth; stepX += hitboxWidth / 2) {
      for (float stepY = 0; stepY <= hitboxHeight; stepY += hitboxHeight / 2) {
        if (checkPoint(x + offsetX + stepX, y + offsetY + stepY, layer)) {
          return true;
        }
      }
    }

    // Final check on the far corners to be safe
    if (checkPoint(x + offsetX + hitboxWidth, y + offsetY + hitboxHeight, layer))
      return true;

    return false;
  }

  private boolean checkPoint(float x, float y, TiledMapTileLayer layer) {
    // 1. Convert pixels to grid index
    int cellX = (int) (x / TILE_SIZE);
    int cellY = (int) (y / TILE_SIZE);

    if (cellX < 0 || cellX >= layer.getWidth() || cellY < 0 || cellY >= layer.getHeight()) {
      return true;
    }

    TiledMapTileLayer.Cell cell = layer.getCell(cellX, cellY);

    // If a tile exists here on the "Walls" layer, block it
    return (cell != null);
  }
}