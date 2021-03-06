package com.vb.ilt.screen.game;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.vb.ilt.GameBase;
import com.vb.ilt.common.GameManager;
import com.vb.ilt.common.TiledMapManager;
import com.vb.ilt.config.GameConfig;
import com.vb.ilt.screen.ScreenBaseAdapter;
import com.vb.ilt.screen.menu.MainMenuScreen;
import com.vb.ilt.screen.transition.transitions.ScreenTransitions;
import com.vb.ilt.systems.active.AnimationSystem;
import com.vb.ilt.systems.active.BoundsSystem;
import com.vb.ilt.systems.active.CameraFollowingPlayerSystem;
import com.vb.ilt.systems.active.ConversationSystem;
import com.vb.ilt.systems.active.DictionarySystem;
import com.vb.ilt.systems.active.FinishSystem;
import com.vb.ilt.systems.active.HudSystem;
import com.vb.ilt.systems.active.MonologueSystem;
import com.vb.ilt.systems.active.MovementSystem;
import com.vb.ilt.systems.active.MusicSystem;
import com.vb.ilt.systems.active.PlayerControlSystem;
import com.vb.ilt.systems.active.SoundSystem;
import com.vb.ilt.systems.active.WorldRenderSystem;
import com.vb.ilt.systems.active.ZOrderSystem;
import com.vb.ilt.systems.debug.DebugCameraSystem;
import com.vb.ilt.systems.debug.DebugRenderSystem;
import com.vb.ilt.systems.debug.EntityLogger;
import com.vb.ilt.systems.passive.CharacterRenderSystem;
import com.vb.ilt.systems.passive.CleanUpSystem;
import com.vb.ilt.systems.passive.EntityFactorySystem;
import com.vb.ilt.systems.passive.ParticlesSystem;
import com.vb.ilt.systems.passive.PauseSystem;
import com.vb.ilt.systems.passive.StartUpSystem;
import com.vb.ilt.systems.passive.collision.NPCCollisionSystem;
import com.vb.ilt.systems.passive.collision.SensorCollisionSystem;
import com.vb.ilt.systems.passive.collision.WorldObjectsCollisionSystem;
import com.vb.ilt.systems.passive.collision.WorldWrapUpSystem;
import com.vb.ilt.ui.stages.PauseCallback;
import com.vb.ilt.util.GdxUtils;

public class GameScreen extends ScreenBaseAdapter{

    private static final String MAP_PATH_PATTERN = "maps/%s";
    private static final String CONVERSATION_PATH_PATTERN = "stories/%s.json";
    private static final String PROPERTIES_PATH_PATTERN = "props/%s.properties";

    private final GameBase game;
    private final AssetManager assetManager;
    private final Batch batch;
    private final Batch HUDBatch;

    private final String level;

    private OrthographicCamera camera;
    private Viewport viewport;
    private Viewport hudViewport;
    private ShapeRenderer renderer;
    private PooledEngine engine;

    public GameScreen(GameBase game, String level) {
        this.game = game;
        this.assetManager = game.getAssetManager();
        this.batch = game.getBatch();
        this.HUDBatch = new SpriteBatch();
        this.level = level;
    }

    @Override
    public void show() {

        Gdx.input.setCatchBackKey(true);
        camera = new OrthographicCamera(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        camera.setToOrtho(false);
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        hudViewport = new FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT);
        renderer = new ShapeRenderer();
        engine = new PooledEngine();

        TiledMapManager tiledMapManager = new TiledMapManager(String.format(MAP_PATH_PATTERN, level));

        EntitySystem conversationSystem = new ConversationSystem(assetManager, hudViewport, HUDBatch);
        conversationSystem.setProcessing(false);

        EntitySystem hudSystem = new HudSystem(hudViewport, batch);

        engine.addSystem(new EntityFactorySystem(assetManager, batch, HUDBatch));
        engine.addSystem(new StartUpSystem(hudViewport, tiledMapManager, String.format(CONVERSATION_PATH_PATTERN, level)));

        engine.addSystem(new PlayerControlSystem(hudViewport, assetManager));
        engine.addSystem(new SoundSystem());
        engine.addSystem(new MusicSystem());
        engine.addSystem(new WorldObjectsCollisionSystem());
        engine.addSystem(new WorldWrapUpSystem());
        engine.addSystem(new NPCCollisionSystem());
        engine.addSystem(new SensorCollisionSystem(tiledMapManager));

        engine.addSystem(new CleanUpSystem());

        engine.addSystem(new MovementSystem());
        engine.addSystem(new BoundsSystem());
        engine.addSystem(new ZOrderSystem());

        engine.addSystem(new CameraFollowingPlayerSystem(camera));
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new CharacterRenderSystem(batch));
        engine.addSystem(new WorldRenderSystem(viewport, batch));


        engine.addSystem(hudSystem);
        engine.addSystem(new PauseSystem(assetManager, hudViewport, HUDBatch, (PauseCallback) hudSystem));
        engine.addSystem(new FinishSystem(hudViewport, HUDBatch, assetManager));
        if (GameConfig.DEBUG_MODE){
            engine.addSystem(new DebugCameraSystem(camera));
            engine.addSystem(new DebugRenderSystem(viewport, renderer));
        }

        engine.addSystem(new ParticlesSystem(batch, viewport));
        engine.addSystem(new DictionarySystem());

        engine.addSystem(conversationSystem);
        engine.addSystem(new MonologueSystem(assetManager, hudViewport, HUDBatch));

        engine.addSystem(new EntityLogger());

    }

    @Override
    public void render(float delta) {
        GdxUtils.clearScreen();
        engine.update(delta);
        if (GameManager.INSTANCE.isQuit()){
            engine.getSystem(MusicSystem.class).setEnabled(false);
            game.setScreen(new MainMenuScreen(game), ScreenTransitions.FADE);
        }else if (GameManager.INSTANCE.isFinished()){
            engine.getSystem(MovementSystem.class).setProcessing(false);
            engine.getSystem(PlayerControlSystem.class).setProcessing(false);
            engine.getSystem(HudSystem.class).setProcessing(false);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        HUDBatch.dispose();
        engine.removeAllEntities();
    }
}
