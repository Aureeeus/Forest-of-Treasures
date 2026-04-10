package edu.tip.forestoftreasures.Model.dialogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import edu.tip.forestoftreasures.Model.Achievement;


public class DialogueLoader {

  /**
   * Immutable data container returned by DialogueLoader after parsing a day's JSON.
   *
   * @param rootNode     The entry node of the day's dialogue graph.
   * @param achievements All achievement definitions declared for this day.
   */
  public record DayData(DialogueNode rootNode, List<Achievement> achievements) {}

  // --- Static Variables ---
  private static final String TAG = "DialogueLoader";

  /**
   * Loads and parses a single day from story_schema.json by its day key.
   *
   * @param filePath The path to story_schema.json relative to the assets folder.
   * @param dayKey   The key identifying the day to load.
   * @return A DayData holding the root DialogueNode and all achievements
   *         defined for this day.
   * @throws RuntimeException if the file is missing, the day key is not found,
   *                          or any node references an unknown id.
   */
  public static DayData load(String filePath, String dayKey) {
    String rawJson = Gdx.files.internal(filePath).readString();
    JsonValue root = new JsonReader().parse(rawJson);
    JsonValue dayJson = root.get(dayKey);

    // Check if day data is presented on the loaded json file
    if (dayJson == null) {
      throw new RuntimeException("[DialogueLoader] Day key '" + dayKey
        + "' not found in " + filePath);
    }

    return parseDay(dayJson, dayKey);
  }


  /**
   * Orchestrates the two-pass loading for a single day's JsonValue object.
   *
   * @param dayJson The JsonValue representing one day.
   * @param dayKey  Used only for error logging to identify which day failed.
   * @return A fully assembled DayData for this data.
   */
  private static DayData parseDay(JsonValue dayJson, String dayKey) {
    String entryNodeId = dayJson.getString("entryNode");
    JsonValue nodesJson = dayJson.get("nodes");
    JsonValue achievementsJson = dayJson.get("achievements");

    // PASS 1: create all nodes and index them by id
    Map<String, DialogueNode> nodeMap = createNodes(nodesJson, dayKey);

    // Pass 2a: link ChoiceNodes and RollNodes FIRST so they are rebuilt in the map
    linkChoiceAndRollNodes(nodesJson, nodeMap, dayKey);

    // Pass 2b: link LineNodes and MinigameNodes after ChoiceNodes are rebuilt
    linkLineAndMinigameNodes(nodesJson, nodeMap, dayKey);

    List<Achievement> achievements = parseAchievements(achievementsJson);

    // Resolve the entry node — the first node the runner will start from
    DialogueNode rootNode = nodeMap.get(entryNodeId);
    if (rootNode == null) {
        throw new RuntimeException("[DialogueLoader] entryNode id '" + entryNodeId
            + "' not found in nodes for " + dayKey);
    }

    Gdx.app.log(TAG, "Loaded Day '" + dayKey + "' with "
        + nodeMap.size() + " nodes and " + achievements.size() + " achievements.");

    return new DayData(rootNode, achievements);
  }


  /**
   * Pass 1: Reads every node object from the JSON array and instantiates it
   * as a LineNode, ChoiceNode, or MinigameNode based on its "type" field.
   *
   * Nodes are stored in a HashMap keyed by their "id" string.
   * At this point, nodes are NOT linked — "next" references are not resolved yet.
   * ChoiceNode choices and MinigameNode minigames are also not linked — that happens in Pass 2.
   *
   * @param nodesJson The JsonValue representing the "nodes" array.
   * @param dayKey    Used for error messages only.
   * @return A map of node id → DialogueNode for all nodes in this day.
   */
  private static Map<String, DialogueNode> createNodes(JsonValue nodesJson, String dayKey) {
    Map<String, DialogueNode> nodeMap = new HashMap<>();

     // Iterate every object in the "nodes" JSON array
    for (JsonValue nodeJson = nodesJson.child; nodeJson != null; nodeJson = nodeJson.next) {
      String id   = nodeJson.getString("id");
      String type = nodeJson.getString("type");

      DialogueNode node = switch (type) {
        case "line"   -> createLineNode(nodeJson);
        case "choice" -> createChoiceNode(nodeJson);
        case "minigame" -> createMinigameNode(nodeJson);
        case "automatic_roll" -> createAutomaticRollNode(nodeJson);
        case "manual_roll" -> createManualRollNode(nodeJson);
        default -> throw new RuntimeException(
            "[DialogueLoader] Unknown node type '" + type + "' on id '" + id + "' in " + dayKey
        );
      };

      nodeMap.put(id, node);
    }

    return nodeMap;
  }

  /**
   * Creates an unlinked LineNode from a JSON node object.
   *
   * Stores the texture path string instead of a Texture object.
   * The controller resolves the path via game.assets.get() at render time,
   * keeping all textures managed by AssetManager.
   *
   * @param nodeJson The JsonValue for a single node in the "nodes" array.
   * @return An unlinked LineNode (next is null until Pass 2).
   */
  private static LineNode createLineNode(JsonValue nodeJson) {
    String text            = nodeJson.getString("text");
    String texturePath     = nodeJson.getString("texture", null); // null if absent
    int damage             = nodeJson.getInt("damage", 0);
    boolean triggerGameEnd = nodeJson.getBoolean("triggerGameEnd", false);
    String obtainableAchievement = nodeJson.getString("obtainableAchievement", null);
    Integer increaseCharisma = nodeJson.has("increaseCharisma")
        ? nodeJson.getInt("increaseCharisma") : null;

    return new LineNode(text, texturePath, damage, triggerGameEnd, obtainableAchievement, increaseCharisma);
  }

  /**
   * Creates an unlinked AutomaticRollNode from a JSON node object.
   */
  private static AutomaticRollNode createAutomaticRollNode(JsonValue nodeJson) {
    int threshold = nodeJson.getInt("threshold");
    return new AutomaticRollNode(threshold);
  }

  /**
   * Creates an unlinked ManualRollNode from a JSON node object.
   */
  private static ManualRollNode createManualRollNode(JsonValue nodeJson) {
    String text        = nodeJson.getString("text");
    String texturePath = nodeJson.getString("texture", null);
    String label       = nodeJson.getString("label");
    int threshold      = nodeJson.getInt("threshold");
    return new ManualRollNode(text, texturePath, label, threshold);
  }

  /**
   * Creates an unlinked ChoiceNode from a JSON node object.
   *
   * In Pass 1, all choice.next() values are null because the referenced nodes
   * may not exist in the map yet. Pass 2 replaces them with real nodes.
   *
   * @param nodeJson The JsonValue for a single node in the "nodes" array.
   * @return An unlinked ChoiceNode (all choice.next() values are null until Pass 2).
   */
  private static ChoiceNode createChoiceNode(JsonValue nodeJson) {
    JsonValue choicesJson = nodeJson.get("choices");
    List<ChoiceNode.Choice> choices = new ArrayList<>();

    for (JsonValue choiceJson = choicesJson.child; choiceJson != null; choiceJson = choiceJson.next) {
      String label = choiceJson.getString("label");
      Integer increaseCharisma = choiceJson.has("increaseCharisma")
          ? choiceJson.getInt("increaseCharisma") : null;
      choices.add(new ChoiceNode.Choice(label, null, increaseCharisma)); // next resolved in Pass 2
    }

    return new ChoiceNode(choices.toArray(new ChoiceNode.Choice[0]));
  }

  /**
   * Creates an unlinked MinigameNode from a JSON node object.
   * The "next" reference is null until Pass 2b.
   *
   * @param nodeJson The JsonValue for this node.
   * @return An unlinked MinigameNode.
   */
  private static MinigameNode createMinigameNode(JsonValue nodeJson) {
    String screenKey = nodeJson.getString("screenKey");
    Boolean playerTurn = null;
    if (nodeJson.has("playerTurn")) {
      playerTurn = nodeJson.getBoolean("playerTurn");
    }
    return new MinigameNode(screenKey, playerTurn);
  }

  // ---------------------------------------------------------------------------
  // PASS 2a — CHOICE AND ROLL NODE LINKING
  // ---------------------------------------------------------------------------

  /**
   * Pass 2a: Rebuilds ChoiceNodes and RollNodes with fully resolved next references
   * and replaces them in the map BEFORE LineNodes are linked.
   */
  private static void linkChoiceAndRollNodes(
    JsonValue nodesJson,
    Map<String, DialogueNode> nodeMap,
    String dayKey
  ) {
    for (JsonValue nodeJson = nodesJson.child; nodeJson != null; nodeJson = nodeJson.next) {
      if (nodeJson.getString("type").equals("choice")) {
        linkChoiceNode(nodeJson, nodeJson.getString("id"), nodeMap, dayKey);
      } else if (nodeJson.getString("type").equals("automatic_roll")) {
        linkAutomaticRollNode(nodeJson, nodeJson.getString("id"), nodeMap, dayKey);
      } else if (nodeJson.getString("type").equals("manual_roll")) {
        linkManualRollNode(nodeJson, nodeJson.getString("id"), nodeMap, dayKey);
      }
    }
  }

  private static void linkAutomaticRollNode(
    JsonValue nodeJson,
    String id,
    Map<String, DialogueNode> nodeMap,
    String dayKey
  ) {
    String successNextId = nodeJson.getString("successNext", null);
    String failNextId = nodeJson.getString("failNext", null);

    AutomaticRollNode node = (AutomaticRollNode) nodeMap.get(id);
    node.setNexts(
      successNextId != null ? resolveNode(successNextId, nodeMap, id, dayKey) : null, 
      failNextId != null ? resolveNode(failNextId, nodeMap, id, dayKey) : null
    );
  }

  private static void linkManualRollNode(
    JsonValue nodeJson,
    String id,
    Map<String, DialogueNode> nodeMap,
    String dayKey
  ) {
    String successNextId = nodeJson.getString("successNext", null);
    String failNextId = nodeJson.getString("failNext", null);

    ManualRollNode node = (ManualRollNode) nodeMap.get(id);
    node.setNexts(
      successNextId != null ? resolveNode(successNextId, nodeMap, id, dayKey) : null, 
      failNextId != null ? resolveNode(failNextId, nodeMap, id, dayKey) : null
    );
  }

  // ---------------------------------------------------------------------------
  // PASS 2b — LINE AND MINIGAME NODE LINKING
  // ---------------------------------------------------------------------------

  /**
   * Pass 2b: Links all LineNodes and MinigameNodes to their next nodes.
   *
   * Runs after linkChoiceNodes() so all ChoiceNodes in the map are already
   * fully rebuilt before LineNodes and MinigameNodes reference them.
   *
   * @param nodesJson The "nodes" JsonValue array.
   * @param nodeMap   The map with all nodes including rebuilt ChoiceNodes.
   * @param dayKey    Used for error messages.
   */
  private static void linkLineAndMinigameNodes(
    JsonValue nodesJson,
    Map<String, DialogueNode> nodeMap,
    String dayKey
  ) {
    for (JsonValue nodeJson = nodesJson.child; nodeJson != null; nodeJson = nodeJson.next) {
      String type = nodeJson.getString("type");
      String id   = nodeJson.getString("id");

      if (type.equals("line")) {
        linkLineNode(nodeJson, id, nodeMap, dayKey);
      } else if (type.equals("minigame")) {
        linkMinigameNode(nodeJson, id, nodeMap, dayKey);
      }
    }
  }

  /**
   * Links a LineNode to its next node by resolving the "next" id string.
   *
   * If "next" is null or absent, then() is not called — the node remains
   * terminal and triggers onDialogueEnd() when the runner reaches it.
   *
   * @param nodeJson The JsonValue for this node.
   * @param id       The id of this node (for error messages).
   * @param nodeMap  The map to resolve the next id against.
   * @param dayKey   Used for error messages.
   */
  private static void linkLineNode(
    JsonValue nodeJson,
    String id,
    Map<String, DialogueNode> nodeMap,
    String dayKey
  ) {
    String nextId = nodeJson.getString("next", null);
    if (nextId == null) return; // terminal node — no linking needed

    LineNode lineNode = (LineNode) nodeMap.get(id);
    lineNode.then(resolveNode(nextId, nodeMap, id, dayKey));
  }

  /**
   * Links a MinigameNode to its next node by resolving the "next" id string.
   * If "next" is null, the node remains terminal and triggers onDialogueEnd().
   *
   * @param nodeJson The JsonValue for this node.
   * @param id       The id of this node.
   * @param nodeMap  The map to resolve the next id against.
   * @param dayKey   Used for error messages.
   */
  private static void linkMinigameNode(
    JsonValue nodeJson,
    String id,
    Map<String, DialogueNode> nodeMap,
    String dayKey
  ) {
    String nextId = nodeJson.getString("next", null);
    if (nextId == null) return;

    MinigameNode minigameNode = (MinigameNode) nodeMap.get(id);
    minigameNode.then(resolveNode(nextId, nodeMap, id, dayKey));
  }

  /**
   * Rebuilds a ChoiceNode's choices list with fully resolved next nodes.
   *
   * Because ChoiceNode.Choice is an immutable record, its "next" field cannot
   * be mutated in place. Instead, a new choices list is built with resolved
   * references and a new ChoiceNode replaces the old one in the map.
   *
   * @param nodeJson The JsonValue for this node.
   * @param id       The id of this node (for error messages).
   * @param nodeMap  The map to resolve choice next ids against.
   * @param dayKey   Used for error messages.
   */
  private static void linkChoiceNode(
    JsonValue nodeJson,
    String id,
    Map<String, DialogueNode> nodeMap,
    String dayKey
  ) {
    JsonValue choicesJson = nodeJson.get("choices");
    List<ChoiceNode.Choice> linkedChoices = new ArrayList<>();

    for (JsonValue choiceJson = choicesJson.child; choiceJson != null; choiceJson = choiceJson.next) {
      String label  = choiceJson.getString("label");
      String nextId = choiceJson.getString("next");
      Integer increaseCharisma = choiceJson.has("increaseCharisma")
          ? choiceJson.getInt("increaseCharisma") : null;
      linkedChoices.add(new ChoiceNode.Choice(label, resolveNode(nextId, nodeMap, id, dayKey), increaseCharisma));
    }

    ChoiceNode node = (ChoiceNode) nodeMap.get(id);
    node.setChoices(linkedChoices.toArray(new ChoiceNode.Choice[0]));
  }

  /**
   * Resolves a node id string to its actual DialogueNode from the map.
   *
   * Throws a descriptive RuntimeException if the id is not found, making it
   * easy to trace which JSON node has a broken reference.
   *
   * @param targetId The id to look up.
   * @param nodeMap  The map to search in.
   * @param sourceId The id of the node making the reference (for error messages).
   * @param dayKey   The day key (for error messages).
   * @return The resolved DialogueNode.
   */
  private static DialogueNode resolveNode(
    String targetId,
    Map<String, DialogueNode> nodeMap,
    String sourceId,
    String dayKey
  ) {
    DialogueNode node = nodeMap.get(targetId);
    if (node == null) {
      throw new RuntimeException("[DialogueLoader] Node id '" + targetId
        + "' referenced by '" + sourceId + "' not found in " + dayKey);
    }
    return node;
  }

  /**
   * Parses the "achievements" JSON array into a list of Achievement objects.
   *
   * Returns an empty list if the "achievements" key is absent or null,
   * so chapters without achievements don't cause errors.
   *
   * @param achievementsJson The JsonValue for the "achievements" array, or null.
   * @return A list of Achievement objects. Empty if none are defined.
   */
  private static List<Achievement> parseAchievements(JsonValue achievementsJson) {
    List<Achievement> achievements = new ArrayList<>();
    if (achievementsJson == null) return achievements;

    for (JsonValue achJson = achievementsJson.child; achJson != null; achJson = achJson.next) {
      String id          = achJson.getString("id");
      String description = achJson.getString("description");

      JsonValue sequenceJson = achJson.get("requiredChoiceSequence");
      List<Integer> sequence = new ArrayList<>();
      if (sequenceJson != null) {
        for (JsonValue index = sequenceJson.child; index != null; index = index.next) {
          sequence.add(index.asInt());
        }
      }

      achievements.add(new Achievement(id, description, sequence));
    }

    return achievements;
  }
}
