package me.homeHelper.assets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.util.Objects;

public final class TextAssets {
    public static final TextAssets TEXTS = new TextAssets();

    private final JsonObject jsonObject;

    TextAssets() {
        jsonObject = JsonParser.parseReader(new InputStreamReader(
                Objects.requireNonNull(TextAssets.class.getResourceAsStream("/texts.json"))))
                .getAsJsonObject();
    }

    public String get(String key) {
        return jsonObject.get(key).getAsString();
    }
}
