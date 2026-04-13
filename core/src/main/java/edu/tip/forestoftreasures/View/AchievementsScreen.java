package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.AchievementsController;
import edu.tip.forestoftreasures.utils.DrawableFactory;
import edu.tip.forestoftreasures.utils.FontFactory;

/**
 * Achievements screen showing a scrollable list of 12 achievement entries.
 * Each entry displays a 100×100 icon placeholder alongside a title and
 * description.
 */
public class AchievementsScreen implements Screen {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    private static final int ACHIEVEMENT_COUNT = 16;

    /** Pixel size of the achievement icon box. */
    private static final float ICON_SIZE = 100f;

    /** Vertical gap between achievement rows. */
    private static final float ROW_SPACING = 18f;

    // Colors used for the box outlines and icon placeholders
    private static final Color BOX_BG_COLOR = new Color(0.13f, 0.09f, 0.06f, 0.85f);
    private static final Color BOX_BORDER_COLOR = new Color(0.65f, 0.45f, 0.22f, 1f);

    private static final String[] ACHIEVEMENT_ICON_PATHS = {
            "icons/Achievements/Achievement First Blood.png",
            "icons/Achievements/Achievement Greedy Water Boy.png",
            "icons/Achievements/Achievement Into the Unknown.png",
            "icons/Achievements/Achievement Lovely Labyrinth.png",
            "icons/Achievements/Achievement Peace Talks.png",
            "icons/Achievements/Achievement The Most Ambitious Crossover.png",
            "icons/Achievements/Achievement The Treasure I Never Had.png",
            "icons/Achievements/Achievement The Treasure of Life.png",
            "icons/Achievements/Achievement Treasure of Aggression.png",
            "icons/Achievements/Achievement Treasure of Harmony.png",
            "icons/Achievements/Achievement Treasure of Riches.png",
            "icons/Achievements/Achievement Wait for it.png",
            "icons/Achievements/Achievement_Blessing_of_the_Sprite.png",
            "icons/Achievements/Achievement Gruesome Farewell.png",
            "icons/Achievements/Achievement Java and the Striped Socks.png",
            "icons/Achievements/Achievement So Close Yet So Far.png"
    };

    private static final String[] ACHIEVEMENT_TITLES = {
            "First Blood",
            "Greedy Water Boy",
            "Into the Unknown",
            "Lovely Labyrinth",
            "Peace Talks",
            "The Most Ambitious Crossover",
            "The Treasure I Never Had",
            "The Treasure of Life",
            "The Treasure of Aggression",
            "The Treasure of Harmony",
            "The Treasure of Riches",
            "Wait for it...",
            "Blessing of the Sprite",
            "Gruesome Farewell",
            "Java and the Striped Socks",
            "So Close Yet So Far"
    };

    private static final String[] ACHIEVEMENT_DESCRIPTIONS = {
            "Defeat your first enemy and claim dominance.",
            "Better safe than sorry, I suppose...",
            "Take a leap of faith into unexplored territories.",
            "Successfully navigate the trickiest of mazes.",
            "Resolve a conflict without drawing your weapon.",
            "You solved Racism, YAY!.",
            "You fought hard, Mage.",
            "Maybe life is the treasure all along.",
            "Is this really a treasure..?",
            "Restore balance to the spirits of the forest.",
            "Gather an unimaginable amount of gold coins.",
            "Talk Less! Smile More!",
            "Receive a magical blessing from the forest sprites.",
            "Meet a terrible and gruesome end.",
            "Now, getting ladies is a piece of cake.",
            "Defeated after almost reaching the goal."
    };

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final GameLauncher game;

    private Stage stage;
    private Texture iconSheet;

    private ImageButton exitButton;
    private ScrollPane scrollPane;

    private Font headerFont;
    private Font rowTitleFont;
    private Font rowDescFont;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public AchievementsScreen(GameLauncher game) {
        this.game = game;
        initialize();
    }

    // -----------------------------------------------------------------------
    // Initialization
    // -----------------------------------------------------------------------

    private void initialize() {
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        root.pad(40f, 60f, 40f, 60f);
        stage.addActor(root);

        addHeader(root);
        addScrollableAchievements(root);

        new AchievementsController(game, this);
    }

    /**
     * Adds the "ACHIEVEMENTS" title and the exit button to the top of the root
     * table.
     */
    private void addHeader(Table root) {
        Table header = new Table();

        headerFont = FontFactory.generateFont("fonts/PressStart2P-Regular.ttf", 52, Color.valueOf("#f5deb3"));
        TextraLabel titleLabel = new TextraLabel("ACHIEVEMENTS", headerFont);

        iconSheet = game.assets.get("icons/dialogue_ui_sheet.png", Texture.class);
        TextureRegion exitRegion = new TextureRegion(iconSheet, 512, 256, 64, 64);
        Drawable exitDrawable = new TextureRegionDrawable(exitRegion).tint(Color.valueOf("#c78861"));

        exitButton = new ImageButton(exitDrawable);
        exitButton.getImageCell().expand().fill();

        // Layout: [spacer] [title] [exit button] — spacer mirrors the button width to
        // keep title centred
        header.add().width(90f);
        header.add(titleLabel).expandX().center();
        header.add(exitButton).size(90f).right();

        root.add(header).growX().padBottom(30f).row();

        // Horizontal divider beneath the title
        Image divider = DrawableFactory.createDivider(BOX_BORDER_COLOR);
        root.add(divider).growX().height(3f).padBottom(26f).row();
    }

    /**
     * Builds a {@link ScrollPane} containing all 12 achievement rows
     * and adds it to the root table so it fills the remaining space.
     */
    private void addScrollableAchievements(Table root) {
        Table listTable = new Table();
        listTable.top();
        listTable.padTop(4f);

        rowTitleFont = FontFactory.generateFont("fonts/PressStart2P-Regular.ttf", 28, Color.valueOf("#d4a96a"));
        rowDescFont = FontFactory.generateFont("fonts/DotGothic16-Regular.ttf", 30, Color.valueOf("#ccc5b0"));

        for (int i = 1; i <= ACHIEVEMENT_COUNT; i++) {
            listTable.add(buildAchievementRow(i)).growX().padBottom(ROW_SPACING).row();
        }

        ScrollPane.ScrollPaneStyle spStyle = new ScrollPane.ScrollPaneStyle();
        final Drawable baseBg = DrawableFactory.getColoredDrawable(new Color(0.2f, 0.2f, 0.2f, 0.5f));
        spStyle.vScroll = new com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable() {
            @Override
            public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float x, float y, float width, float height) {
                baseBg.draw(batch, x, y, width, height);
            }

            @Override
            public float getMinWidth() {
                return 10f;
            }
        };
        final Drawable baseKnob = DrawableFactory.getColoredDrawable(BOX_BORDER_COLOR);
        spStyle.vScrollKnob = new com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable() {
            @Override
            public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float x, float y, float width, float height) {
                baseKnob.draw(batch, x, y, width, height);
            }

            @Override
            public float getMinWidth() {
                return 10f;
            }

            @Override
            public float getMinHeight() {
                return 30f;
            }
        };

        scrollPane = new ScrollPane(listTable, spStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // vertical scroll only
        scrollPane.setOverscroll(false, false);

        root.add(scrollPane).grow().row();
    }

    /**
     * Builds one achievement box row using nested tables.
     * The outer table carries the border color; inner table carries the fill.
     *
     * <pre>
     * ┌─────────────────────────────────┐
     * │ [icon 100×100] | Title          │
     * │                | Description    │
     * └─────────────────────────────────┘
     * </pre>
     *
     * @param index 1-based index used to label the placeholder achievement.
     * @return A {@link Table} representing the styled achievement box.
     */
    private Table buildAchievementRow(int index) {

        // Outer table acts as a visible border via its background color + padding
        Table borderTable = new Table();
        borderTable.setBackground(DrawableFactory.getColoredDrawable(BOX_BORDER_COLOR));
        borderTable.pad(3f);

        // Inner table provides the dark fill behind the content
        Table inner = new Table();
        inner.setBackground(DrawableFactory.getColoredDrawable(BOX_BG_COLOR));
        inner.pad(14f);

        // Map the index to one of the 15 icons we declared
        String path = ACHIEVEMENT_ICON_PATHS[(index - 1) % ACHIEVEMENT_ICON_PATHS.length];
        Texture iconTex = game.assets.get(path, Texture.class);
        Image iconPlaceholder = new Image(iconTex);

        // Vertical divider
        Image vertDivider = DrawableFactory.createDivider(BOX_BORDER_COLOR);

        // Text column
        Table textColumn = new Table();
        textColumn.center().left();
        textColumn.defaults().left().padBottom(6f);

        String title = ACHIEVEMENT_TITLES[(index - 1) % ACHIEVEMENT_TITLES.length];
        String desc = ACHIEVEMENT_DESCRIPTIONS[(index - 1) % ACHIEVEMENT_DESCRIPTIONS.length];

        TextraLabel nameLabel = new TextraLabel(title, rowTitleFont);
        TextraLabel descLabel = new TextraLabel(desc, rowDescFont);
        descLabel.setWrap(true);

        textColumn.add(nameLabel).growX().padTop(15f).row();
        textColumn.add(descLabel).growX().row();

        // Assemble the inner content row
        inner.add(iconPlaceholder).size(ICON_SIZE).padRight(14f);
        inner.add(vertDivider).width(2f).fillY().padRight(14f);
        inner.add(textColumn).expandX().fillX().center();

        // Slot the inner table into the border table
        borderTable.add(inner).grow();

        return borderTable;
    }

    // -----------------------------------------------------------------------
    // Screen lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        if (scrollPane != null) {
            stage.setScrollFocus(scrollPane);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0)
            return;
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null)
            stage.dispose();
    }

    // -----------------------------------------------------------------------
    // Getters for controller
    // -----------------------------------------------------------------------

    /** @return The exit button that returns the user to the main menu. */
    public ImageButton getExitButton() {
        return exitButton;
    }
}
