package edu.tip.forestoftreasures.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TextraLabel;

import edu.tip.forestoftreasures.GameLauncher;
import edu.tip.forestoftreasures.Controller.CreditsController;
import edu.tip.forestoftreasures.utils.FontFactory;

/**
 * Credits screen. Displays a scrolling list of contributor names and roles
 * that drifts upward automatically, in the style of classic end-credits.
 */
public class CreditsScreen implements Screen {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Pixels per second the credits scroll upward. */
    private static final float SCROLL_SPEED = 60f;

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final GameLauncher game;

    private Stage stage;
    private Texture iconSheet;
    private Music creditsMusic;

    /** The table that holds all credit labels; its Y position is animated. */
    private Table creditsTable;

    /** Y position of the credits table (increases over time to scroll up). */
    private float scrollY;

    /** Total height of the credits content so we know when to loop/stop. */
    private float contentHeight;

    // UI
    private ImageButton exitButton;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public CreditsScreen(GameLauncher game) {
        this.game = game;
        initialize();
    }

    // -----------------------------------------------------------------------
    // Initialization
    // -----------------------------------------------------------------------

    private void initialize() {
        stage = new Stage(new ScreenViewport());

        creditsMusic = game.assets.get("audio/bgm/end_credits_bg_music.mp3", Music.class);
        creditsMusic.setLooping(true);
        creditsMusic.setVolume(game.settingsConfig.getGameSettings().bgMusicVolume());

        buildExitButton();
        buildCreditsContent();
    }

    /**
     * Builds the exit (back) button anchored to the top-right corner of the stage.
     */
    private void buildExitButton() {
        iconSheet = game.assets.get("icons/dialogue_ui_sheet.png", Texture.class);
        TextureRegion exitRegion = new TextureRegion(iconSheet, 512, 256, 64, 64);
        Drawable exitDrawable = new TextureRegionDrawable(exitRegion)
                .tint(Color.valueOf("#c78861"));

        exitButton = new ImageButton(exitDrawable);
        exitButton.getImageCell().expand().fill();

        // Anchor the exit button to the top-right using a full-parent overlay table
        Table overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.top().right();
        overlayTable.add(exitButton).size(80f).pad(20f);

        stage.addActor(overlayTable);
    }

    /**
     * Builds the vertically-scrolling credits content and positions it just
     * below the bottom edge of the screen so it scrolls up into view.
     */
    private void buildCreditsContent() {
        creditsTable = new Table();
        creditsTable.defaults().padBottom(24f).center();

        // --- Fonts ---
        Font titleFont  = FontFactory.generateFont("fonts/PressStart2P-Regular.ttf", 52, Color.valueOf("#f5deb3"));
        Font headerFont = FontFactory.generateFont("fonts/PressStart2P-Regular.ttf", 34, Color.valueOf("#d4a96a"));
        Font bodyFont   = FontFactory.generateFont("fonts/DotGothic16-Regular.ttf",  36, Color.valueOf("#fffde7"));
        Font subFont    = FontFactory.generateFont("fonts/DotGothic16-Regular.ttf",  28, Color.valueOf("#b0bec5"));

        // ===================================================================
        // CREDITS CONTENT
        // Replace the placeholder names/details below with real information.
        // ===================================================================

        addSpacer(creditsTable, 60f);

        // Game title
        addLabel(creditsTable, "FOREST OF TREASURES", titleFont);
        addSpacer(creditsTable, 48f);

        // --- Project Lead ---
        addLabel(creditsTable, "PROJECT LEAD", headerFont);
        addLabel(creditsTable, "LIBRADO, John David", bodyFont);
        addSpacer(creditsTable, 36f);

        // --- Game Design ---
        addLabel(creditsTable, "GAME DESIGN", headerFont);
        addLabel(creditsTable, "ABO, John Ramil", bodyFont);
        addLabel(creditsTable, "LIBRADO, John David", bodyFont);
        addLabel(creditsTable, "MANALO, Christian Benedict", bodyFont);
        addSpacer(creditsTable, 36f);

        // --- Programming ---
        addLabel(creditsTable, "PROGRAMMERS", headerFont);
        addLabel(creditsTable, "ABO, John Ramil", bodyFont);
        addLabel(creditsTable, "LIBRADO, John David", bodyFont);
        addLabel(creditsTable, "MANALO, Christian Benedict", bodyFont);
        addSpacer(creditsTable, 36f);

        // --- Art & Graphics ---
        addLabel(creditsTable, "ART & GRAPHICS", headerFont);
       addLabel(creditsTable, "ABO, John Ramil", bodyFont);
        addLabel(creditsTable, "LIBRADO, John David", bodyFont);
        addLabel(creditsTable, "MANALO, Christian Benedict", bodyFont);
        addSpacer(creditsTable, 36f);

        // --- Sound & Music ---
        addLabel(creditsTable, "SOUND & MUSIC", headerFont);
        addLabel(creditsTable, "ABO, John Ramil", bodyFont);
        addLabel(creditsTable, "LIBRADO, John David", bodyFont);
        addLabel(creditsTable, "MANALO, Christian Benedict", bodyFont);
        addSpacer(creditsTable, 36f);

        // --- Writing & Narrative ---
        addLabel(creditsTable, "WRITING & NARRATIVE", headerFont);
        addLabel(creditsTable, "ABO, John Ramil", bodyFont);
        addLabel(creditsTable, "LIBRADO, John David", bodyFont);
        addLabel(creditsTable, "MANALO, Christian Benedict", bodyFont);
        addSpacer(creditsTable, 36f);

        // --- Special Thanks ---
        addLabel(creditsTable, "SPECIAL THANKS", headerFont);
        addLabel(creditsTable, "Technological Institute of the Philippines", subFont);
        addLabel(creditsTable, "Our Families & Friends", subFont);
        addLabel(creditsTable, "You, the Player", subFont);
        addSpacer(creditsTable, 60f);

        // --- Footer ---
        addLabel(creditsTable, "\u00A9 2025 Forest of Treasures Team", subFont);
        addLabel(creditsTable, "All Rights Reserved.", subFont);

        addSpacer(creditsTable, 120f); // trailing space so last line scrolls fully off the top

        // ===================================================================

        creditsTable.pack();
        contentHeight = creditsTable.getPrefHeight();

        // Start just below the bottom of the screen
        scrollY = -Gdx.graphics.getHeight();
        creditsTable.setPosition(
                (Gdx.graphics.getWidth() - creditsTable.getPrefWidth()) / 2f,
                scrollY
        );

        stage.addActor(creditsTable);
    }

    // -----------------------------------------------------------------------
    // Helper builders
    // -----------------------------------------------------------------------

    private void addLabel(Table table, String text, Font font) {
        TextraLabel label = new TextraLabel(text, font);
        table.add(label).center().row();
    }

    private void addSpacer(Table table, float height) {
        table.add().height(height).row();
    }

    // -----------------------------------------------------------------------
    // Screen lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        // Sync volume in case the player changed it in settings
        creditsMusic.setVolume(game.settingsConfig.getGameSettings().bgMusicVolume());

        // Reset scroll so credits always start fresh when the screen is shown
        scrollY = -Gdx.graphics.getHeight();

        if (!creditsMusic.isPlaying()) {
            creditsMusic.play();
        }

        new CreditsController(game, this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        // Advance scroll position
        scrollY += SCROLL_SPEED * delta;
        creditsTable.setY(scrollY);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;

        stage.getViewport().update(width, height, true);

        // Re-center the credits table horizontally after resize
        creditsTable.setX((width - creditsTable.getPrefWidth()) / 2f);
    }

    @Override
    public void pause() {
        if (creditsMusic != null && creditsMusic.isPlaying()) {
            creditsMusic.pause();
        }
    }

    @Override
    public void resume() {
        Gdx.input.setInputProcessor(stage);
        if (creditsMusic != null && !creditsMusic.isPlaying()) {
            creditsMusic.play();
        }
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        if (creditsMusic != null && creditsMusic.isPlaying()) {
            creditsMusic.stop();
        }
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }

    // -----------------------------------------------------------------------
    // Getters for controller
    // -----------------------------------------------------------------------

    /** @return The exit / back button. */
    public ImageButton getExitButton() {
        return exitButton;
    }

    /**
     * Returns {@code true} once all credits have scrolled fully past the top
     * of the screen — can be used by the controller to auto-return to menu.
     */
    public boolean isScrollComplete() {
        return scrollY > contentHeight + Gdx.graphics.getHeight();
    }
}
