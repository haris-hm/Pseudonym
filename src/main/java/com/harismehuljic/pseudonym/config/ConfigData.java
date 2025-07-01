package com.harismehuljic.pseudonym.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.harismehuljic.pseudonym.Pseudonym;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ConfigData {
    @Expose private String validNicknameCharacters = "^[a-zA-Z0-9 _-]+$";
    @Expose private int characterLimit = 30;

    @Expose private String profaneWordsUrl = "https://raw.githubusercontent.com/zacanger/profane-words/master/words.json";
    @Expose private String[] customIllegalNicknames = {};

    @Expose private boolean enforceValidNicknameCharacters = true;
    @Expose private boolean enforceCustomIllegalNicknames = true;

    private final ArrayList<String> profaneWords = new ArrayList<>();

    public String getValidNicknameCharacters() {
        return validNicknameCharacters;
    }

    public int getCharacterLimit() {
        return characterLimit;
    }

    public boolean enforceValidNicknameCharacters() {
        return enforceValidNicknameCharacters;
    }

    public boolean enforceIllegalNicknames() {
        return enforceCustomIllegalNicknames;
    }

    public ArrayList<String> getIllegalNicknames() {
        ArrayList<String> filteredWords = new ArrayList<>(List.of(this.customIllegalNicknames));

        if (this.profaneWords.isEmpty() && !this.profaneWordsUrl.isEmpty()) {
            try {
                // Connect to the URL using java's native library
                URL url = new URL(this.profaneWordsUrl);
                URLConnection request = url.openConnection();
                request.connect();

                // Convert to a JSON object to print data
                JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));

                if (root.isJsonArray()) {
                    JsonArray jsonArray = root.getAsJsonArray();

                    for (JsonElement element : jsonArray) {
                        filteredWords.add(element.getAsString());
                    }
                }
            }
            catch (Exception e) {
                Pseudonym.LOGGER.error("Error trying to read from provided URL: {}", e.getMessage());
            }

            this.profaneWords.addAll(filteredWords);
        }

        return this.profaneWords;
    }
}
