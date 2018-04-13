package com.vb.ilt.ui.stages;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.vb.ilt.assets.AssetDescriptors;
import com.vb.ilt.assets.ButtonStyleNames;
import com.vb.ilt.assets.RegionNames;

public class AuthorSpeechStage extends Stage {

    private static final Logger log = new Logger(ConversationStage.class.getName(), Logger.DEBUG);

    private final Skin skin;
    private Label authorText;
    private ExitCallback exitCallback;

    private TextureRegion region;


    public AuthorSpeechStage(Viewport viewport, SpriteBatch batch, AssetManager assetManager, ExitCallback exitCallback) {
        super(viewport, batch);
        this.skin = assetManager.get(AssetDescriptors.UI_SKIN);
        this.region = assetManager.get(AssetDescriptors.PANELS).findRegion(RegionNames.AUTHOR_SPEECH);
        this.exitCallback = exitCallback;
        init();
    }

    private void init(){
        Table mainTable = new Table();
        this.authorText = new Label("", skin);
        this.authorText.setWrap(true);

        //this.npcText.setFontScale(2);

        Table container = new Table();
        Table buttonTable = new Table();

        Button exitButton = new ImageButton(skin, ButtonStyleNames.QUIT);

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                exitCallback.exit();
            }
        });


        buttonTable.add(exitButton).padTop(40f).right().top();

        ScrollPane scrollPane = new ScrollPane(authorText);
        scrollPane.setFadeScrollBars(false);

        mainTable.add(buttonTable).right().top().row();
        mainTable.add(scrollPane).center().width(640f).height(780f).padBottom(40f);

        mainTable.setBackground(new TextureRegionDrawable(region));
        mainTable.center();
        mainTable.setSize(720f, 1000f);

        mainTable.pack();

        container.setFillParent(true);
        container.add(mainTable);

        this.addActor(container);
    }

    public void updateText(String text){
        this.authorText.setText(text);
    }
}
