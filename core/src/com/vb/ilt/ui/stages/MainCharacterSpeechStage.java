package com.vb.ilt.ui.stages;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.vb.ilt.assets.AssetDescriptors;
import com.vb.ilt.assets.RegionNames;

public class MainCharacterSpeechStage extends MonologueStage{
    private static final Logger log = new Logger(ConversationStage.class.getName(), Logger.DEBUG);

    public MainCharacterSpeechStage(Viewport viewport, SpriteBatch batch, AssetManager assetManager, ExitCallback exitCallback) {
        super(viewport, batch, assetManager, exitCallback);
    }

    protected void init(){
        Table mainTable = new Table();
        this.text = new Label("", skin);
        this.text.setWrap(true);

        Table container = new Table();

        Button letsPlay = new TextButton("Let's Play!", skin);

        letsPlay.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                exitCallback.exit();
            }
        });


        Label label = new Label("THE DOG", skin, "stencil");

        Image mainCharacterImage = new Image(this.assetManager.get(AssetDescriptors.CLOSE_UP).findRegion(RegionNames.MAIN_CHARACTER_CLOSE_UP));

        Table textWithButton = new Table();
        textWithButton.add(mainCharacterImage).width(640f).height(400f).padBottom(20f).row();
        textWithButton.add(text).growX().left().row();
        textWithButton.add(letsPlay).padTop(20f).padBottom(20f).expandX();


        ScrollPane scrollPane = new ScrollPane(textWithButton);
        scrollPane.setFadeScrollBars(false);

        mainTable.add(label).padTop(20f).padBottom(20f).row();
        mainTable.add(scrollPane).center().width(640f).height(830f);

        mainTable.setBackground(new TextureRegionDrawable(region));
        mainTable.center();
        mainTable.setSize(720f, 1000f);

        mainTable.pack();

        container.setFillParent(true);
        container.add(mainTable);

        this.addActor(container);
    }

    public void updateText(String text){
        this.text.setText(text);
    }

}