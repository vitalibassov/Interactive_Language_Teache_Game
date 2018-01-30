package com.vb.ilt.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Logger;

import java.util.LinkedHashMap;

public final class GameManager {

    private static final Logger log = new Logger(GameManager.class.getName(), Logger.DEBUG);

    private int currentLevel;
    private final LinkedHashMap<String, String> bigDictionary;

    public static final GameManager INSTANCE = new GameManager();

    private GameManager() {
        this.bigDictionary = new LinkedHashMap<String, String>();
        loadDictionary();
    }

    private void loadDictionary(){
        for (String word : Gdx.files.internal("dictionary/dictionary.txt").readString().split("\n")){
            String[] engrus = word.toLowerCase().split(" -- ");
            if (engrus.length == 2){
                //System.out.println(engrus[0]);
                bigDictionary.put(engrus[0], engrus[0] + " - " + engrus[1]);
            }
        }

        //seven records are lost somehow... (equal keys)
        log.debug("SIZE OF THE BIG DICTIONARY = " + bigDictionary.size());

    }

    public void doStuff(){}
}