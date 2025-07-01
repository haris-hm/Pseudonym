package com.harismehuljic.pseudonym.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.harismehuljic.pseudonym.Pseudonym;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class ConfigManager {
    @Nullable
    public static ConfigData loadConfig() {
        String fileName = Pseudonym.ID + ".json";
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        try {
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), fileName);

            return gson.fromJson(new FileReader(configFile), ConfigData.class);
        }
        catch (FileNotFoundException e) {
            Pseudonym.LOGGER.error("Couldn't find config file. Regenerating default config.");
            ConfigData configData = new ConfigData();

            try (Writer writer = new FileWriter(FabricLoader.getInstance().getConfigDir().resolve(fileName).toString())) {
                gson.toJson(configData, writer);
            }
            catch (IOException exception) {
                Pseudonym.LOGGER.error("Couldn't generate default config: {}", exception.getMessage());
                return null;
            }

            return configData;
        }
        catch (Exception e) {
            Pseudonym.LOGGER.error("Something went wrong when reading the config: {}", e.getMessage());
            return null;
        }
    }
}
