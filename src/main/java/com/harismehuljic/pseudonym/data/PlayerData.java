package com.harismehuljic.pseudonym.data;

import com.harismehuljic.pseudonym.Pseudonym;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlayerData {
    private static final Logger playerDataLogger = LoggerFactory.getLogger(Pseudonym.ID + "_player_data");

    public static void savePlayerData(MinecraftServer server, ServerPlayer spe, CompoundTag nbt) {
        Path playerDataPath = getPlayerDataPath(server, spe);

        try {
            if (!nbt.isEmpty()) {
                Files.createDirectories(playerDataPath.getParent());
                NbtIo.writeCompressed(nbt, playerDataPath);
            }
        } catch (IOException e) {
            playerDataLogger.error(String.format("Couldn't save player data for %s.\n%s", spe.getGameProfile().name(), e.getMessage()));
        }
    }

    public static CompoundTag readPlayerData(MinecraftServer server, ServerPlayer spe) {
        Path playerDataPath = getPlayerDataPath(server, spe);

        try {
            if (!Files.exists(playerDataPath)) {
                playerDataLogger.info(String.format("Player data does not yet exist for %s.", spe.getGameProfile().name()));
                return null;
            }

            return NbtIo.readCompressed(playerDataPath, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            playerDataLogger.error(String.format("Couldn't load player data for %s.\n%s", spe.getGameProfile().name(), e.getMessage()));
            return null;
        }
    }

    private static Path getPlayerDataPath(MinecraftServer server, ServerPlayer spe) {
        return server.getWorldPath(LevelResource.ROOT).resolve(Pseudonym.ID).resolve("players").resolve(spe.getStringUUID() + ".dat");
    }
}
