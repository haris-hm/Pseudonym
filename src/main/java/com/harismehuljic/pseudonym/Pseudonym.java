package com.harismehuljic.pseudonym;

import com.harismehuljic.pseudonym.config.ConfigData;
import com.harismehuljic.pseudonym.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pseudonym implements ModInitializer {
    public static final String ID = "pseudonym";

    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final String VERSION = FabricLoader.getInstance().getModContainer(ID).get().getMetadata().getVersion().getFriendlyString();

    public static ConfigData CONFIG_DATA;

    @Override
    public void onInitialize() {
        LOGGER.info("Pseudonym version {} loading", VERSION);

        CONFIG_DATA = ConfigManager.loadConfig();
        Registries.registerAll();

        LOGGER.info("Pseudonym successfully loaded.");
    }
}
