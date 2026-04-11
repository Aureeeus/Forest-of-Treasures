package edu.tip.forestoftreasures;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import edu.tip.forestoftreasures.Model.entities.Player;

import edu.tip.forestoftreasures.Model.SettingsConfiguration;
import edu.tip.forestoftreasures.View.AchievementsScreen;
import edu.tip.forestoftreasures.View.MainMenuScreen;
import edu.tip.forestoftreasures.View.SettingsScreen;
import edu.tip.forestoftreasures.utils.DrawableFactory;
import edu.tip.forestoftreasures.utils.FontFactory;
import edu.tip.forestoftreasures.utils.UIFactory;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class GameLauncher extends Game {
    public AssetManager assets;
    public SettingsConfiguration settingsConfig;
    private Player player;

    // Cached Screens
    private MainMenuScreen mainMenuScreen;
    private SettingsScreen settingsScreen;
    private AchievementsScreen achievementsScreen;

    public MainMenuScreen getMainMenuScreen() {
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(this);
        }
        return mainMenuScreen;
    }

    public SettingsScreen getSettingsScreen() {
        if (settingsScreen == null) {
            Music bgMusic = assets.get("audio/bgm/main_menu_bg_music.mp3", Music.class);
            settingsScreen = new SettingsScreen(this, bgMusic);
        }
        return settingsScreen;
    }

    public AchievementsScreen getAchievementsScreen() {
        if (achievementsScreen == null) {
            achievementsScreen = new AchievementsScreen(this);
        }
        return achievementsScreen;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void create() {
        assets = new AssetManager();

        SkinLoader.SkinParameter params = new SkinLoader.SkinParameter("ui/fotskin.atlas");
        assets.load("ui/fotskin.json", Skin.class, params);

        // --- Backgrounds ---
        assets.load("images/backgrounds/main_menu_bg.png", Texture.class);
        assets.load("images/backgrounds/settings_bg.png", Texture.class);

        // --- Player Stats and Movesets UI ---
        assets.load("icons/stats_icons.png", Texture.class);
        assets.load("icons/Gear.png", Texture.class);
        assets.load("icons/dex_icon.png", Texture.class);
        assets.load("icons/skill1_icon.png", Texture.class);
        assets.load("icons/skill2_icon.png", Texture.class);
        assets.load("icons/skill3_icon.png", Texture.class);

        // --- Spritesheet icons ---
        assets.load("icons/dialogue_ui_sheet.png", Texture.class);
        
        // --- Achievement Icons ---
        assets.load("icons/Achievements/Achievement First Blood.png", Texture.class);
        assets.load("icons/Achievements/Achievement Greedy Water Boy.png", Texture.class);
        assets.load("icons/Achievements/Achievement Into the Unknown.png", Texture.class);
        assets.load("icons/Achievements/Achievement Lovely Labyrinth.png", Texture.class);
        assets.load("icons/Achievements/Achievement Peace Talks.png", Texture.class);
        assets.load("icons/Achievements/Achievement The Most Ambitious Crossover.png", Texture.class);
        assets.load("icons/Achievements/Achievement Wait for it.png", Texture.class);
        assets.load("icons/Achievements/Achievement The Treasure I Never Had.png", Texture.class);
        assets.load("icons/Achievements/Achievement The Treasure of Life.png", Texture.class);
        assets.load("icons/Achievements/Achievement Treasure of Aggression.png", Texture.class);
        assets.load("icons/Achievements/Achievement Treasure of Harmony.png", Texture.class);
        assets.load("icons/Achievements/Achievement Treasure of Riches.png", Texture.class);
        assets.load("icons/Achievements/Achievement_Blessing_of_the_Sprite.png", Texture.class);
        assets.load("icons/Achievements/Achievement Gruesome Farewell.png", Texture.class);

        // --- Bg Music ---
        assets.load("audio/bgm/main_menu_bg_music.mp3", Music.class);
        assets.load("audio/bgm/end_credits_bg_music.mp3", Music.class);
        assets.load("audio/sfx/Battle_Screen_Music.mp3", Music.class);

        // --- Sound Effects ---
        assets.load("audio/sfx/main_menu_start_sound.wav", Sound.class);
        assets.load("audio/sfx/select_sound.wav", Sound.class);
        assets.load("audio/sfx/maze_enter.mp3", Sound.class);

        // --- Battle SFX ---
        assets.load("audio/sfx/Cry of Misery.mp3", Sound.class);
        assets.load("audio/sfx/Intense Aura.wav", Sound.class);
        assets.load("audio/sfx/Lullaby Of Obedience.wav", Sound.class);
        assets.load("audio/sfx/enemies/bandit_attack.mp3", Sound.class);
        assets.load("audio/sfx/enemies/cavern_creature_attack.mp3", Sound.class);
        assets.load("audio/sfx/enemies/leviathan_attack.mp3", Sound.class);
        assets.load("audio/sfx/enemies/wyvern_attack.mp3", Sound.class);
        assets.load("audio/sfx/enemies/goblin_king_attack.mp3", Sound.class);

        // --- Particle Effects ---
        assets.load("particles/autumn_leaf.p", ParticleEffect.class);

        // --- Settings UI ---
        assets.load("icons/wood_exit_button.png", Texture.class);
        assets.load("images/ui/wood_checkbox_unchecked.png", Texture.class);
        assets.load("images/ui/wood_checkbox_checked.png", Texture.class);
        assets.load("images/ui/wood_slider_empty.png", Texture.class);
        assets.load("images/ui/wood_slider_filled.png", Texture.class);
        assets.load("images/ui/wood_grabber_normal.png", Texture.class);
        assets.load("images/ui/wood_grabber_pressed.png", Texture.class);

        // --- Day 1 UI ---
        assets.load("scenarios/day1/forest_intro.png", Texture.class);
        assets.load("scenarios/day1/forest_sprite.png", Texture.class);
        assets.load("scenarios/day1/deep_in_forest.png", Texture.class);
        assets.load("scenarios/day1/hobgoblin_hurt.png", Texture.class);
        assets.load("scenarios/day1/forest_treant.png", Texture.class);
        assets.load("scenarios/day1/day1_end.png", Texture.class);
        assets.load("scenarios/day1/hobgoblin_perpetrator.png", Texture.class);
        
        // --- Day 2 UI ---
        assets.load("scenarios/day2/main_arc/stream.png", Texture.class);
        assets.load("scenarios/day2/main_arc/bushes.png", Texture.class);
        assets.load("scenarios/day2/main_arc/goblin_settlement.png", Texture.class);
        assets.load("scenarios/day2/main_arc/goblin_being_harassed.png", Texture.class);
        assets.load("scenarios/day2/main_arc/goblin_city_gate.png", Texture.class);
        assets.load("scenarios/day2/main_arc/goblin_city_interior.png", Texture.class);
        assets.load("scenarios/day2/cavern_arc/cavern_creature.png", Texture.class);
        assets.load("scenarios/day2/cavern_arc/bioluminescent_moss.png", Texture.class);
        assets.load("scenarios/day2/cavern_arc/cavern_heart.png", Texture.class);
        assets.load("scenarios/day2/cavern_arc/centipede.png", Texture.class);
        assets.load("scenarios/day2/cavern_arc/leviathan.png", Texture.class);
        assets.load("scenarios/day2/cavern_arc/ravine.png", Texture.class);
        assets.load("scenarios/day2/cavern_arc/knight.png", Texture.class);
        assets.load("scenarios/day2/cavern_arc/knights.png", Texture.class);
        assets.load("scenarios/day2/cavern_arc/knight_captain.png", Texture.class);
        assets.load("scenarios/day2/leave_arc/wyvern.png", Texture.class);
        assets.load("scenarios/day2/bandit_arc/goblin_king.png", Texture.class);
        assets.load("scenarios/day2/attack_arc/bullfrog_soldiers.png", Texture.class);
        assets.load("scenarios/day2/bandit_arc/bandit_encounter.png", Texture.class);
        assets.load("scenarios/day2/bandit_arc/bandit_city_gate.png", Texture.class);
        assets.load("scenarios/day2/bandit_arc/bandit_goblin_collab.png", Texture.class);
        assets.load("scenarios/day2/bandit_arc/battle_scene.png", Texture.class);
        assets.finishLoading();

        settingsConfig = new SettingsConfiguration();
        
        // Initialize persistent player with default stats
        player = new Player(
            Player.STARTING_HP, 
            Player.STARTING_STR, 
            0f, 
            Player.STARTING_INT, 
            Player.STARTING_DEX, 
            Player.STARTING_CHA
        );

        this.setScreen(getMainMenuScreen());
    }

    @Override
    public void dispose() {
        super.dispose();

        // Explicitly dispose of cached screens to release their manual local resources
        // (e.g., Stages, SpriteBatches)
        // Their loaded Textures and Sounds are handled by the AssetManager below.
        if (mainMenuScreen != null)
            mainMenuScreen.dispose();
        if (settingsScreen != null)
            settingsScreen.dispose();
        if (achievementsScreen != null)
            achievementsScreen.dispose();

        assets.dispose();
        DrawableFactory.dispose();
        FontFactory.disposeAll();
        UIFactory.dispose();
    }
}

// * TODO: Ask Manalo about the stats and pictures of hobgoblin king as well as its attack lines and also the ferocious battle bullshit. Java and Striped socks achievement also.