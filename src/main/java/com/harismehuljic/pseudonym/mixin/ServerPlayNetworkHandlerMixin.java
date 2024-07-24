package com.harismehuljic.pseudonym.mixin;

import com.harismehuljic.pseudonym.nicknames.impl.NickManager;
import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements NickManager {
    @Shadow
    public ServerPlayerEntity player;

    /**
     * Sends a packet to all the players on the server to indicate someone's name has changed to
     * their new nickname
     */
    @Override
    public void pseudonym$updateDisplayName(NickPlayer nickPlayer) {
        Objects.requireNonNull(this.player.getServer())
                .getPlayerManager()
                .sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, this.player));
    }
}
