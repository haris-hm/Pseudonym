package com.harismehuljic.pseudonym.event;

import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class PlayerConnectionEvents {
    public static class PlayerJoinEvent implements ServerPlayConnectionEvents.Join {
        @Override
        public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
            NickPlayer nickPlayer = (NickPlayer) handler.getPlayer();
            nickPlayer.pseudonym$readCustomData(handler.getPlayer());
        }
    }

    public static class PlayerLeaveEvent implements  ServerPlayConnectionEvents.Disconnect {
        @Override
        public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
            NickPlayer nickPlayer = (NickPlayer) handler.getPlayer();
            nickPlayer.pseudonym$writeCustomData();
        }
    }
}
