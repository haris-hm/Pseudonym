package com.harismehuljic.pseudonym.mixin;

import com.harismehuljic.pseudonym.nicknames.impl.NickManager;
import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements NickManager {
    @Shadow
    public ServerPlayer player;

    /**
     * Sends a packet to all the players on the server to indicate someone's name has changed to
     * their new nickname
     */
    @Override
    public void pseudonym$updateDisplayName(NickPlayer nickPlayer) {
        Objects.requireNonNull(this.player.level().getServer())
                .getPlayerList()
                .broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, this.player));
    }
}
