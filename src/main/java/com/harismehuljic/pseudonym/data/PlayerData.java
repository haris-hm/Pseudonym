package com.harismehuljic.pseudonym.data;

import com.harismehuljic.pseudonym.Pseudonym;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlayerData {
    private static final Logger playerDataLogger = LoggerFactory.getLogger(Pseudonym.ID + "_player_data");

    public static void savePlayerData(MinecraftServer server, ServerPlayerEntity spe, NbtCompound nbt) {
        Path playerDataPath = getPlayerDataPath(server, spe);

        try {
            if(!nbt.isEmpty()) {
                Files.createDirectories(playerDataPath.getParent());
                NbtIo.writeCompressed(nbt, playerDataPath);
            }
        } catch (IOException e) {
            playerDataLogger.error(String.format("Couldn't save player data for %s.\n%s", spe.getGameProfile().getName(), e.getMessage()));
        }
    }

    public static NbtCompound readPlayerData(MinecraftServer server, ServerPlayerEntity spe) {
        Path playerDataPath = getPlayerDataPath(server, spe);

        try {
            if (!Files.exists(playerDataPath)) {
                playerDataLogger.info(String.format("Player data does not yet exist for %s.", spe.getGameProfile().getName()));
                return null;
            }

            return NbtIo.readCompressed(playerDataPath, NbtSizeTracker.ofUnlimitedBytes());
        }
        catch (IOException e) {
            playerDataLogger.error(String.format("Couldn't load player data for %s.\n%s", spe.getGameProfile().getName(), e.getMessage()));
            return null;
        }
    }

    private static Path getPlayerDataPath(MinecraftServer server, ServerPlayerEntity spe) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(Pseudonym.ID).resolve("players").resolve(spe.getUuidAsString() + ".dat");
    }
}
