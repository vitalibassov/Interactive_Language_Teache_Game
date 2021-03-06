package com.vb.ilt.ui.tables;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import java.util.LinkedHashMap;
import java.util.Map;


public abstract class ListWithSearchTableBase extends Table implements TextField.TextFieldListener{

    private final String btnStyle;
    private final Map<String, String> availableWords;
    private TextField search;
    private Table words;

    ListWithSearchTableBase(Skin skin, String btnStyle) {
        super(skin);
        this.btnStyle = btnStyle;
        this.availableWords = new LinkedHashMap<>();
        init();
    }

    ListWithSearchTableBase(Skin skin, String btnStyle, Map<String, String> availableWords) {
        super(skin);
        this.btnStyle = btnStyle;
        this.availableWords = availableWords;
        init();
    }

    private void init(){
        Table searchTable = new Table();
        searchTable.defaults().pad(10f);

        this.search = new TextField("", getSkin());
        this.search.setTextFieldListener(this);
        searchTable.add(this.search).growX();
        searchTable.setBackground(getSkin().getDrawable("text-field"));

        this.words = new Table();
        for (Map.Entry<String, String> wordEntry : availableWords.entrySet()){
            addRowToWords(wordEntry.getKey(), wordEntry.getValue(), words);
        }

        words.pack();
        words.top();

        ScrollPane scrollPane = new ScrollPane(words);
        scrollPane.setFadeScrollBars(false);
        scrollPane.pack();

        add(searchTable).growX().pad(40).row();
        add(scrollPane).grow().padBottom(40).padLeft(20).padRight(20);
        setFillParent(true);

        pack();
    }

    private void addRowToWords(final String wordKey, final String wordValue, Table words){
        Label label = new Label(wordValue, getSkin());
        label.setWrap(true);
        label.setAlignment(Align.left);
        words.add(label).padLeft(15).padRight(15).padTop(20).padBottom(20).left().top().growX();
        if (checkWordKey(wordKey)) {
            Button btn = new ImageButton(getSkin(), this.btnStyle);
            btn.setName(wordValue);
            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    processBtn(wordKey, wordValue);
                }
            });
            words.add(btn).width(100f).height(100f).row();
        }
        words.add().row();
    }

    protected boolean checkWordKey(final String wordKey){
        return wordKey != null;
    }

    @Override
    public void keyTyped(TextField textField, char c) {
        updateWords();
    }

    public void updateWords(){
        words.clear();
        String text = this.search.getText();
        for (Map.Entry<String, String> wordEntry : availableWords.entrySet()){
            if (wordEntry.getValue().contains(text)){
                addRowToWords(wordEntry.getKey(), wordEntry.getValue(), this.words);
            }
        }
        if (!this.words.hasChildren()){
            addRowToWords(null,"No results...", this.words);
        }
    }

    public Map<String, String> getAvailableWords() {
        return availableWords;
    }

    public void setAvailableWords(Map<String, String> availableWords) {
        this.availableWords.clear();
        this.availableWords.putAll(availableWords);
    }

    public void hideKeyboard(){
        this.search.getOnscreenKeyboard().show(false);
    }

    abstract void processBtn (String wordKey, String wordValue);
}
