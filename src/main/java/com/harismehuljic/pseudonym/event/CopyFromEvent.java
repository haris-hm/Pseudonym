package com.harismehuljic.pseudonym.event;

import com.harismehuljic.pseudonym.nicknames.impl.NickManager;
import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class CopyFromEvent implements ServerPlayerEvents.CopyFrom {
    /**
     * Called when player data is copied to a new player.
     *
     * @param oldPlayer the old player
     * @param newPlayer the new player
     * @param alive     whether the old player is still alive
     */
    @Override
    public void copyFromPlayer(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        NickManager newPlayerManager = (NickManager) newPlayer.networkHandler;

        NickPlayer oldNickPlayer = (NickPlayer) oldPlayer;
        NickPlayer newNickPlayer = (NickPlayer) newPlayer;

        oldNickPlayer.pseudonym$writeCustomData();
        oldNickPlayer.pseudonym$getNickname().getNicknameLabel().destroyLabel();

        newNickPlayer.pseudonym$getNickname().loadNicknameData(oldNickPlayer.pseudonym$getNickname().getNicknameData(), newPlayer);
        newPlayerManager.pseudonym$updateDisplayName(newNickPlayer);
    }
}
