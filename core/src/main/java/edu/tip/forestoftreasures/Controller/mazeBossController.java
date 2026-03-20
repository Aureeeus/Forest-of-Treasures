package edu.tip.forestoftreasures.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class mazeBossController {
  public Vector2 playerPosition;
  public Vector2 enemyPosition;
  public boolean enemyActive = false;
  private float enemySpawnTimer = 0f;
  private final float ENEMY_SPAWN_DELAY = 5f; // seconds
  public boolean playerDead = false;
  public float playerSpeed = 125f;
  public float enemySpeed = 120f;

  // Hitbox used for both player and enemy when checking collisions
  private final float hitboxWidth = 16f;
  private final float hitboxHeight = 16f;
  private final float offsetX = 12f;
  private final float offsetY = 12f;

  // Pathfinding
  private List<int[]> path = new ArrayList<>();
  private int pathIndex = 0;
  private float pathRecalcTimer = 0f;
  private final float REPATH_INTERVAL = 0.5f; // seconds

  // 32px (Original Tile) * 0.6f (Scale) = 19.2f
  private final float TILE_SIZE = 19.2f;
  private final float PLAYER_SIZE = 48f;

  // Walkable grid cached from the map
  private boolean[][] walkable;
  private boolean walkableBuilt = false;

  public mazeBossController() {
    // Start position (Adjust as needed)
    playerPosition = new Vector2(40, 700);
    enemyPosition = new Vector2();
  }

  public void movement(float deltaTime, TiledMap map) {
    TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Walls");
    if (layer == null)
      return;

    // Build walkable grid once per map
    if (!walkableBuilt) {
      buildWalkableGrid(layer);
      walkableBuilt = true;
    }

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

    // Update enemy spawn timer and activation
    if (!enemyActive) {
      enemySpawnTimer += deltaTime;
      if (enemySpawnTimer >= ENEMY_SPAWN_DELAY) {
        enemyActive = true;
        // Spawn at the player's initial spawn (where the player started)
        enemyPosition.set(40, 700);
      }
    }

    // Enemy AI with pathfinding
    if (enemyActive && !playerDead) {
      pathRecalcTimer += deltaTime;

      // Recalculate path periodically
      if (pathRecalcTimer >= REPATH_INTERVAL || path.isEmpty()) {
        pathRecalcTimer = 0f;
        buildPathToPlayer(layer);
      }

      // Follow path if available
      if (!path.isEmpty() && pathIndex < path.size()) {
        int[] cell = path.get(pathIndex);
        // Target the center of the tile adjusted for the actor hitbox so collision
        // checks align
        float targetX = (cell[0] * TILE_SIZE) - offsetX + (TILE_SIZE - hitboxWidth) / 2f;
        float targetY = (cell[1] * TILE_SIZE) - offsetY + (TILE_SIZE - hitboxHeight) / 2f;

        float dx = targetX - enemyPosition.x;
        float dy = targetY - enemyPosition.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 1f) {
          float nx = dx / dist;
          float ny = dy / dist;

          float oldEx = enemyPosition.x;
          float oldEy = enemyPosition.y;

          enemyPosition.x += nx * enemySpeed * deltaTime;
          if (isColliding(enemyPosition.x, enemyPosition.y, layer)) {
            enemyPosition.x = oldEx;
            // if collision happens, force path recalculation
            path.clear();
            pathIndex = 0;
          }

          enemyPosition.y += ny * enemySpeed * deltaTime;
          if (isColliding(enemyPosition.x, enemyPosition.y, layer)) {
            enemyPosition.y = oldEy;
            path.clear();
            pathIndex = 0;
          }
        } else {
          // reached this tile, advance
          pathIndex++;
        }
      } else {
        // fallback: direct chase if no path
        float dx = playerPosition.x - enemyPosition.x;
        float dy = playerPosition.y - enemyPosition.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0.1f) {
          float nx = dx / dist;
          float ny = dy / dist;

          float oldEx = enemyPosition.x;
          float oldEy = enemyPosition.y;

          enemyPosition.x += nx * enemySpeed * deltaTime;
          if (isColliding(enemyPosition.x, enemyPosition.y, layer)) {
            enemyPosition.x = oldEx;
            path.clear();
            pathIndex = 0;
          }

          enemyPosition.y += ny * enemySpeed * deltaTime;
          if (isColliding(enemyPosition.x, enemyPosition.y, layer)) {
            enemyPosition.y = oldEy;
            path.clear();
            pathIndex = 0;
          }
        }
      }

      // Check for overlap (touch) between enemy and player
      float touchDist = 24f; // threshold for touch
      if (enemyPosition.dst(playerPosition) <= touchDist) {
        playerDead = true;
      }
    }
  }

  private void buildWalkableGrid(TiledMapTileLayer layer) {
    int width = layer.getWidth();
    int height = layer.getHeight();
    walkable = new boolean[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        walkable[x][y] = (layer.getCell(x, y) == null);
      }
    }
  }

  private int[] nearestWalkable(int sx, int sy, int width, int height) {
    if (sx >= 0 && sy >= 0 && sx < width && sy < height && walkable[sx][sy])
      return new int[] { sx, sy };
    boolean[][] seen = new boolean[width][height];
    Queue<int[]> q = new LinkedList<>();
    int sxClamped = Math.max(0, Math.min(sx, width - 1));
    int syClamped = Math.max(0, Math.min(sy, height - 1));
    q.add(new int[] { sxClamped, syClamped });
    seen[sxClamped][syClamped] = true;

    int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
    while (!q.isEmpty()) {
      int[] p = q.poll();
      int x = p[0], y = p[1];
      for (int[] d : dirs) {
        int nx = x + d[0], ny = y + d[1];
        if (nx < 0 || ny < 0 || nx >= width || ny >= height)
          continue;
        if (seen[nx][ny])
          continue;
        if (walkable[nx][ny])
          return new int[] { nx, ny };
        seen[nx][ny] = true;
        q.add(new int[] { nx, ny });
      }
    }
    return null;
  }

  private void buildPathToPlayer(TiledMapTileLayer layer) {
    // compute start/goal from actor centers and find nearest walkable tiles
    int startX = (int) ((enemyPosition.x + offsetX + hitboxWidth / 2f) / TILE_SIZE);
    int startY = (int) ((enemyPosition.y + offsetY + hitboxHeight / 2f) / TILE_SIZE);
    int goalX = (int) ((playerPosition.x + offsetX + hitboxWidth / 2f) / TILE_SIZE);
    int goalY = (int) ((playerPosition.y + offsetY + hitboxHeight / 2f) / TILE_SIZE);

    path.clear();
    pathIndex = 0;

    int width = layer.getWidth();
    int height = layer.getHeight();

    // ensure walkable grid exists
    if (walkable == null)
      buildWalkableGrid(layer);

    int[] s = nearestWalkable(startX, startY, width, height);
    int[] g = nearestWalkable(goalX, goalY, width, height);
    if (s == null || g == null)
      return;
    startX = s[0];
    startY = s[1];
    goalX = g[0];
    goalY = g[1];

    if (startX == goalX && startY == goalY)
      return;

    class Node {
      int x, y;
      float g, f;
      Node parent;

      Node(int x, int y) {
        this.x = x;
        this.y = y;
        g = Float.POSITIVE_INFINITY;
        f = Float.POSITIVE_INFINITY;
      }
    }

    PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
    HashSet<Long> closed = new HashSet<>();
    HashMap<Long, Node> all = new HashMap<>();

    long keyStart = (((long) startX) << 32) | (startY & 0xffffffffL);
    Node start = new Node(startX, startY);
    start.g = 0f;
    start.f = (float) Math.hypot(goalX - startX, goalY - startY);
    open.add(start);
    all.put(keyStart, start);

    Node found = null;

    while (!open.isEmpty()) {
      Node cur = open.poll();
      long curKey = (((long) cur.x) << 32) | (cur.y & 0xffffffffL);
      if (closed.contains(curKey))
        continue;
      closed.add(curKey);

      if (cur.x == goalX && cur.y == goalY) {
        found = cur;
        break;
      }

      // 8-directional neighbors (prevent corner cutting)
      for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
          if (dx == 0 && dy == 0)
            continue;
          int nx = cur.x + dx;
          int ny = cur.y + dy;
          if (nx < 0 || ny < 0 || nx >= width || ny >= height)
            continue;
          if (!walkable[nx][ny])
            continue;
          // prevent corner cutting
          if (dx != 0 && dy != 0) {
            if (!walkable[cur.x + dx][cur.y] || !walkable[cur.x][cur.y + dy])
              continue;
          }

          long nkey = (((long) nx) << 32) | (ny & 0xffffffffL);
          if (closed.contains(nkey))
            continue;

          float cost = (dx == 0 || dy == 0) ? 1f : 1.4142f;
          float ng = cur.g + cost;
          Node neighbor = all.get(nkey);
          if (neighbor == null) {
            neighbor = new Node(nx, ny);
            all.put(nkey, neighbor);
          }
          if (ng < neighbor.g) {
            neighbor.g = ng;
            neighbor.f = ng + (float) Math.hypot(goalX - nx, goalY - ny);
            neighbor.parent = cur;
            open.add(neighbor);
          }
        }
      }
    }

    if (found != null) {
      List<int[]> rev = new ArrayList<>();
      Node cur = found;
      while (cur != null && !(cur.x == startX && cur.y == startY)) {
        rev.add(new int[] { cur.x, cur.y });
        cur = cur.parent;
      }
      for (int i = rev.size() - 1; i >= 0; i--)
        path.add(rev.get(i));
    }
  }

  private boolean isColliding(float x, float y, TiledMapTileLayer layer) {
    for (float stepX = 0; stepX <= hitboxWidth; stepX += hitboxWidth / 2) {
      for (float stepY = 0; stepY <= hitboxHeight; stepY += hitboxHeight / 2) {
        if (checkPoint(x + offsetX + stepX, y + offsetY + stepY, layer)) {
          return true;
        }
      }
    }

    if (checkPoint(x + offsetX + hitboxWidth, y + offsetY + hitboxHeight, layer))
      return true;

    return false;
  }

  private boolean checkPoint(float x, float y, TiledMapTileLayer layer) {
    int cellX = (int) (x / TILE_SIZE);
    int cellY = (int) (y / TILE_SIZE);

    if (cellX < 0 || cellX >= layer.getWidth() || cellY < 0 || cellY >= layer.getHeight()) {
      return true;
    }

    TiledMapTileLayer.Cell cell = layer.getCell(cellX, cellY);
    return (cell != null);
  }

  /**
   * Checks if the player's current position overlaps any tile on the Exit layer.
   */
  public boolean isOnExit(TiledMap map) {
    TiledMapTileLayer exitLayer = (TiledMapTileLayer) map.getLayers().get("Exit");
    if (exitLayer == null)
      return false;

    float centerX = playerPosition.x + PLAYER_SIZE / 2f;
    float centerY = playerPosition.y + PLAYER_SIZE / 2f;

    int cellX = (int) (centerX / TILE_SIZE);
    int cellY = (int) (centerY / TILE_SIZE);

    if (cellX < 0 || cellX >= exitLayer.getWidth() || cellY < 0 || cellY >= exitLayer.getHeight()) {
      return false;
    }

    TiledMapTileLayer.Cell cell = exitLayer.getCell(cellX, cellY);
    return cell != null;
  }
}
