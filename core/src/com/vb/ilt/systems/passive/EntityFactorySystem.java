package com.vb.ilt.systems.passive;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.vb.ilt.assets.AssetDescriptors;
import com.vb.ilt.assets.AssetPaths;
import com.vb.ilt.assets.RegionNames;
import com.vb.ilt.config.GameConfig;
import com.vb.ilt.entity.CharacterType;
import com.vb.ilt.entity.Direction;
import com.vb.ilt.entity.components.AnimationComponent;
import com.vb.ilt.entity.components.BoundsComponent;
import com.vb.ilt.entity.components.DictionaryComponent;
import com.vb.ilt.entity.components.DimensionComponent;
import com.vb.ilt.entity.components.DirectionComponent;
import com.vb.ilt.entity.components.VelocityComponent;
import com.vb.ilt.entity.components.MusicComponent;
import com.vb.ilt.entity.components.ParticlesComponent;
import com.vb.ilt.entity.components.PlayerComponent;
import com.vb.ilt.entity.components.PositionComponent;
import com.vb.ilt.entity.components.SoundComponent;
import com.vb.ilt.entity.components.TextureComponent;
import com.vb.ilt.entity.components.ZOrderComponent;
import com.vb.ilt.entity.components.dialog_model.Conversation;
import com.vb.ilt.entity.components.hud.ControlsComponent;
import com.vb.ilt.entity.components.hud.HudComponent;
import com.vb.ilt.entity.components.hud.StageComponent;
import com.vb.ilt.entity.components.npc.NPCComponent;
import com.vb.ilt.entity.components.npc.StoryComponent;
import com.vb.ilt.entity.components.world.PortalSensorComponent;
import com.vb.ilt.entity.components.world.PortalSensorSpawnComponent;
import com.vb.ilt.entity.components.world.TiledMapComponent;
import com.vb.ilt.entity.components.world.TiledMapRendererComponent;
import com.vb.ilt.entity.components.world.WorldCollisionObjectComponent;
import com.vb.ilt.entity.components.world.WorldObjectComponent;
import com.vb.ilt.shape.ShapeUtils;
import com.vb.ilt.ui.stages.HudStage;

import java.util.Map;
import java.util.TreeMap;

public class EntityFactorySystem extends EntitySystem{

    private static final float BOUNDS_OFFSET_X = 0.5f;
    private static final float BOUNDS_OFFSET_Y = 0.5f;

    private static final float VISION_RANGE = 6f;

    private static final float ANIMATION_TIME_FRONT = 0.05f; //0.04f
    private static final float ANIMATION_TIME_WALKING = 0.015f;

    private static final int DEFAULT_PLAYER_Z_ORDER = 1;
    private static final int DEFAULT_NPC_Z_ORDER = 2;

    private static final Logger log = new Logger(EntityFactorySystem.class.getName(), Logger.DEBUG);

    private PooledEngine engine;
    private final AssetManager assetManager;
    private final Batch batch;
    private final Batch HUDBatch;

    public EntityFactorySystem(AssetManager assetManager, Batch batch, Batch HUDBatch){
        this.batch = batch;
        this.HUDBatch = HUDBatch;
        this.assetManager = assetManager;
    }

    @Override
    public boolean checkProcessing() {
        return false;
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = (PooledEngine) engine;
    }

    public void createPlayer(Vector2 spawnPoint){
        TextureAtlas playerAtlas = assetManager.get(AssetDescriptors.PLAYER);

        DimensionComponent dimension = engine.createComponent(DimensionComponent.class);
        dimension.width = GameConfig.PLAYER_WIDTH;
        dimension.height = GameConfig.PLAYER_HEIGHT;

        PositionComponent position = engine.createComponent(PositionComponent.class);
        position.x = spawnPoint.x;
        position.y = spawnPoint.y;

        BoundsComponent bounds = engine.createComponent(BoundsComponent.class);
        bounds.polygon = polygonToIso(new Polygon(ShapeUtils.createRectangle(-BOUNDS_OFFSET_X, BOUNDS_OFFSET_Y,dimension.width / 2f, dimension.height / 2f)));

        VelocityComponent movement = engine.createComponent(VelocityComponent.class);
        AnimationComponent animation = engine.createComponent(AnimationComponent.class);
        Animation<TextureRegion> playerFront = new Animation<TextureRegion>(
                ANIMATION_TIME_FRONT,
                playerAtlas.findRegions(RegionNames.PLAYER_FRONT),
                Animation.PlayMode.LOOP
        );

        Animation<TextureRegion> playerUp = new Animation<TextureRegion>(
                ANIMATION_TIME_WALKING,
                playerAtlas.findRegions(RegionNames.PLAYER_UP),
                Animation.PlayMode.LOOP
        );

        Animation<TextureRegion> playerDown = new Animation<TextureRegion>(
                ANIMATION_TIME_WALKING,
                playerAtlas.findRegions(RegionNames.PLAYER_DOWN),
                Animation.PlayMode.LOOP
        );

        Animation<TextureRegion> playerRight = new Animation<TextureRegion>(
                ANIMATION_TIME_WALKING,
                playerAtlas.findRegions(RegionNames.PLAYER_RIGHT),
                Animation.PlayMode.LOOP
        );

        Animation<TextureRegion> playerLeft = new Animation<TextureRegion>(
                ANIMATION_TIME_WALKING,
                playerAtlas.findRegions(RegionNames.PLAYER_LEFT),
                Animation.PlayMode.LOOP
        );

        Array<Animation<TextureRegion>> anims = new Array<>();
        anims.add(playerFront);
        anims.add(playerUp);
        anims.add(playerDown);
        anims.add(playerLeft);
        anims.add(playerRight);

        animation.animations = new ImmutableArray<>(anims);
        animation.setAnimationIndex(0);

        TextureComponent texture = engine.createComponent(TextureComponent.class);
        texture.region = anims.get(0).getKeyFrame(0);
        log.debug(texture.region.toString());

        SoundComponent sound = engine.createComponent(SoundComponent.class);
        sound.sound = assetManager.get(AssetDescriptors.STEP_SOUND);

        DirectionComponent direction = engine.createComponent(DirectionComponent.class);
        direction.direction = Direction.IDLE;

        PlayerComponent player = engine.createComponent(PlayerComponent.class);

        ZOrderComponent zOrder = engine.createComponent(ZOrderComponent.class);
        zOrder.z = DEFAULT_PLAYER_Z_ORDER;

        ParticlesComponent particles = engine.createComponent(ParticlesComponent.class);
        particles.pooledEffect = new ParticleEffectPool(assetManager.get(AssetDescriptors.DIRT_PARTICLES), 5, 20);
        particles.offset = new Vector2(GameConfig.PLAYER_HALF_WIDTH, GameConfig.PLAYER_HALF_HEIGHT / 2f);

        addEntity(position, dimension, bounds, movement, player, texture, animation, zOrder, sound, direction, particles);
    }

    public void createNPCs(Map<Vector2, String> spawnPoints){
        TextureAtlas npcAtlas = assetManager.get(AssetDescriptors.NPC);

        for(Map.Entry<Vector2, String> point : spawnPoints.entrySet()) {
            String typeStr = point.getValue();

            NPCComponent npc = engine.createComponent(NPCComponent.class);
            npc.type = CharacterType.valueOf(typeStr.toUpperCase());

            DimensionComponent dimension = engine.createComponent(DimensionComponent.class);
            dimension.width = GameConfig.PLAYER_WIDTH;
            dimension.height = GameConfig.PLAYER_HEIGHT;

            PositionComponent position = engine.createComponent(PositionComponent.class);
            position.x = point.getKey().x;
            position.y = point.getKey().y;

            BoundsComponent bounds = engine.createComponent(BoundsComponent.class);
            bounds.polygon = polygonToIso(new Polygon(ShapeUtils.createRectangle(-BOUNDS_OFFSET_X, BOUNDS_OFFSET_Y,dimension.width / 2f, dimension.height / 2f)));

            AnimationComponent animation = engine.createComponent(AnimationComponent.class);
            Animation<TextureRegion> npcFront = new Animation<TextureRegion>(
                    ANIMATION_TIME_FRONT,
                    npcAtlas.findRegions(typeStr),
                    Animation.PlayMode.LOOP_PINGPONG
            );

            Array<Animation<TextureRegion>> anims = new Array<>();
            anims.add(npcFront);

            animation.animations = new ImmutableArray<>(anims);
            animation.setAnimationIndex(0);

            TextureComponent texture = engine.createComponent(TextureComponent.class);
            texture.region = anims.get(0).getKeyFrame(0);

            ZOrderComponent zOrder = engine.createComponent(ZOrderComponent.class);
            zOrder.z = DEFAULT_NPC_Z_ORDER;

            SoundComponent sound = engine.createComponent(SoundComponent.class);
            sound.sound = assetManager.get(String.format("%s/%s.wav", AssetPaths.NPC_SOUNDS, typeStr), Sound.class);

            addEntity(position, dimension, bounds, texture, animation, npc, zOrder, sound);
        }
    }

    public void createMap(TiledMap tMap){
        TiledMapComponent tiledMap = engine.createComponent(TiledMapComponent.class);
        tiledMap.map = tMap;

        TiledMapRendererComponent mapRenderer = engine.createComponent(TiledMapRendererComponent.class);
        mapRenderer.mapRenderer = new IsometricTiledMapRenderer(tiledMap.map,1f / GameConfig.MAP_SCALE, batch);

        BoundsComponent bounds = engine.createComponent(BoundsComponent.class);
        MapProperties props = tiledMap.map.getProperties();

        float [] vertices = ShapeUtils.createRectangle(VISION_RANGE, VISION_RANGE,
                props.get("width", Integer.class) - VISION_RANGE * 2f,
                props.get("height", Integer.class) - VISION_RANGE * 2f);

        bounds.polygon = polygonToIso(new Polygon(vertices));
        bounds.polygon.setPosition(0.05f, 0.45f);

        WorldObjectComponent worldObject = engine.createComponent(WorldObjectComponent.class);

        addEntity(tiledMap, bounds, mapRenderer, worldObject);
    }

    public void createPortalSensors(Map<PolygonMapObject, String> sensors){
        for (Map.Entry<PolygonMapObject, String> sensor : sensors.entrySet()){
            BoundsComponent bounds = engine.createComponent(BoundsComponent.class);
            Polygon originPolygon = sensor.getKey().getPolygon();
            bounds.polygon = polygonToMapIso(originPolygon);

            Vector2 polygonPos = polygonPosToIso(originPolygon);
            bounds.polygon.setPosition(polygonPos.x, polygonPos.y);

            PositionComponent position = engine.createComponent(PositionComponent.class);
            position.x = bounds.polygon.getX();
            position.y = bounds.polygon.getY();

            SoundComponent sound = engine.createComponent(SoundComponent.class);
            sound.sound = assetManager.get(AssetDescriptors.DOOR_SOUND);

            WorldObjectComponent worldObject = engine.createComponent(WorldObjectComponent.class);

            PortalSensorComponent portalSensor = engine.createComponent(PortalSensorComponent.class);
            portalSensor.name = sensor.getValue();

            addEntity(bounds, portalSensor, position, worldObject, sound);
        }
    }

    public void createPortalSensorSpawns(Map<Vector2, String> spawns){
        for(Map.Entry<Vector2, String> point : spawns.entrySet()) {

            PositionComponent position = engine.createComponent(PositionComponent.class);
            position.x = point.getKey().x;
            position.y = point.getKey().y;

            WorldObjectComponent worldObject = engine.createComponent(WorldObjectComponent.class);
            PortalSensorSpawnComponent portalSensorSpawn = engine.createComponent(PortalSensorSpawnComponent.class);
            portalSensorSpawn.name = point.getValue();

            addEntity(position, worldObject, portalSensorSpawn);

        }
    }

    public void createCollisionObjects(Array<PolygonMapObject> mapObjects){
        log.debug("mapObjects size= " + mapObjects.size);
        for(PolygonMapObject object : mapObjects){
            BoundsComponent bounds = engine.createComponent(BoundsComponent.class);
            Polygon originPolygon = object.getPolygon();
            log.debug("X: " + originPolygon.getX() + " Y: " + originPolygon.getY());
            bounds.polygon = polygonToMapIso(originPolygon);

            Vector2 polygonPos = polygonPosToIso(originPolygon);
            bounds.polygon.setPosition(polygonPos.x, polygonPos.y);

            PositionComponent position = engine.createComponent(PositionComponent.class);
            position.x = bounds.polygon.getX();
            position.y = bounds.polygon.getY();

            WorldObjectComponent worldObject = engine.createComponent(WorldObjectComponent.class);
            WorldCollisionObjectComponent worldCollisionObject = engine.createComponent(WorldCollisionObjectComponent.class);

            addEntity(bounds, position, worldObject, worldCollisionObject);
        }
    }

    public void createControls(){
        TextureAtlas hudAtlas = assetManager.get(AssetDescriptors.HUD);

        HudComponent hud = engine.createComponent(HudComponent.class);

        DimensionComponent dimensions = engine.createComponent(DimensionComponent.class);
        dimensions.width = GameConfig.CONTROLS_SIZE;
        dimensions.height = GameConfig.CONTROLS_SIZE;

        PositionComponent position = engine.createComponent(PositionComponent.class);
        position.x = GameConfig.CONTROLS_X;
        position.y = GameConfig.CONTROLS_Y;

        ControlsComponent controls = engine.createComponent(ControlsComponent.class);
        final float HALF_SIZE = dimensions.width / 2f;
        controls.bottomLeft = new Polygon(ShapeUtils.createRectangle(
                position.x, position.y,
                HALF_SIZE, HALF_SIZE
        ));
        controls.bottomRight = new Polygon(ShapeUtils.createRectangle(
                position.x + HALF_SIZE, position.y,
                HALF_SIZE, HALF_SIZE
        ));
        controls.topLeft = new Polygon(ShapeUtils.createRectangle(
                position.x, position.y + HALF_SIZE,
                HALF_SIZE, HALF_SIZE
        ));
        controls.topRight = new Polygon(ShapeUtils.createRectangle(
                position.x + HALF_SIZE, position.y + HALF_SIZE,
                HALF_SIZE, HALF_SIZE
        ));

        TextureComponent texture = engine.createComponent(TextureComponent.class);
        texture.region = hudAtlas.findRegion(RegionNames.CONTROLS_IDLE);

        addEntity(hud, controls, position, texture, dimensions);
    }

    public void createHud(Viewport hudViewport, DictionaryComponent dictionaryComponent){
        StageComponent stage = engine.createComponent(StageComponent.class);
        stage.stage = new HudStage(assetManager, hudViewport, HUDBatch, dictionaryComponent.allWords, dictionaryComponent.myWords);
        SoundComponent sound = engine.createComponent(SoundComponent.class);
        sound.sound = assetManager.get(AssetDescriptors.ACHIEVEMENT_SOUND);
        HudComponent hud = engine.createComponent(HudComponent.class);

        addEntity(hud, stage, sound);
    }

    public DictionaryComponent createDictionary(){
        DictionaryComponent dict = engine.createComponent(DictionaryComponent.class);
        dict.allWords = new TreeMap<>();
        dict.myWords = new TreeMap<>();
        addEntity(dict);
        return dict;
    }

    public void createStory(Queue<Conversation> conversations) {
        StoryComponent conversation = engine.createComponent(StoryComponent.class);
        SoundComponent sound = engine.createComponent(SoundComponent.class);
        sound.sound = assetManager.get(AssetDescriptors.FINISHED_SOUND);
        conversation.conversations = conversations;
        addEntity(conversation, sound);
    }

    public void createMusic(AssetDescriptor<Music> music){
        MusicComponent musicComponent = engine.createComponent(MusicComponent.class);
        musicComponent.music = assetManager.get(music);
        musicComponent.music.setVolume(GameConfig.MUSIC_VOLUME);
        addEntity(musicComponent);
    }

    private void addEntity(Component ... components){
        Entity entity = engine.createEntity();
        for(Component c : components){
            entity.add(c);
        }
        engine.addEntity(entity);
    }

    private Polygon polygonToMapIso(Polygon originPolygon){
        float [] vertices = originPolygon.getVertices();
        float [] newVertices = new float[vertices.length];
        for(int i = 0, j = 1; j < vertices.length; i += 2, j += 2){
            newVertices[i] = (vertices[j] + vertices[i]) / (GameConfig.TILE_HEIGHT * GameConfig.MAP_SCALE_MULTIPLIER);
            newVertices[j] = (vertices[j] - vertices[i]) / (GameConfig.TILE_WIDTH * GameConfig.MAP_SCALE_MULTIPLIER);
        }
        return new Polygon(newVertices);
    }

    private Polygon polygonToIso(Polygon originPolygon){
        float [] vertices = originPolygon.getVertices();
        float [] newVertices = new float[vertices.length];
        for(int i = 0, j = 1; j < vertices.length; i += 2, j += 2){
            newVertices[i] = (vertices[j] + vertices[i]);
            newVertices[j] = (vertices[j] - vertices[i]) / 2f;
        }
        return new Polygon(newVertices);
    }

    private Vector2 polygonPosToIso(Polygon polygon){
        return new Vector2((polygon.getY() + polygon.getX()) / (GameConfig.TILE_HEIGHT * GameConfig.MAP_SCALE_MULTIPLIER),
                (polygon.getY() - polygon.getX()) / (GameConfig.TILE_WIDTH * GameConfig.MAP_SCALE_MULTIPLIER ) + GameConfig.DEFAULT_Y_OFFSET);
    }
}