package com.vb.ilt.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class ParticlesComponent implements Component, Pool.Poolable{

    public ParticleEffectPool pooledEffect;
    public boolean toProcess;
    public Vector2 offset = new Vector2();


    @Override
    public void reset() {
        pooledEffect = null;
        toProcess = false;
        offset.setZero();
    }
}
