package com.vb.ilt.systems.active;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.vb.ilt.entity.components.world.TiledMapComponent;
import com.vb.ilt.entity.components.world.TiledMapRendererComponent;
import com.vb.ilt.entity.components.world.WorldObjectComponent;
import com.vb.ilt.systems.passive.CharacterRenderSystem;
import com.vb.ilt.systems.passive.ParticlesSystem;
import com.vb.ilt.util.Mappers;

public class WorldRenderSystem extends EntitySystem{

    private static final Logger log = new Logger(WorldRenderSystem.class.getName(), Logger.DEBUG);

    private final Batch batch;
    private final Viewport viewport;

    private static final Family MAP_FAMILY = Family.all(
            TiledMapComponent.class,
            TiledMapRendererComponent.class,
            WorldObjectComponent.class
    ).get();

    public WorldRenderSystem(Viewport viewport, Batch batch) {
        this.viewport = viewport;
        this.batch = batch;
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> tiledMaps = getEngine().getEntitiesFor(MAP_FAMILY);
        IsometricTiledMapRenderer mapRenderer = Mappers.MAP_RENDERER.get(tiledMaps.first()).mapRenderer;

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().projection);
        mapRenderer.setView((OrthographicCamera) viewport.getCamera());
        mapRenderer.render(new int[]{0, 1, 2});
        getEngine().getSystem(ParticlesSystem.class).update(deltaTime);
        getEngine().getSystem(CharacterRenderSystem.class).update(deltaTime);
        mapRenderer.render(new int[]{3, 4});
    }
}
