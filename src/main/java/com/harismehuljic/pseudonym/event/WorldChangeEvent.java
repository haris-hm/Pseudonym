package com.harismehuljic.pseudonym.event;

import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class WorldChangeEvent implements ServerEntityWorldChangeEvents.AfterPlayerChange {
    /**
     * Called after a player has been moved to different world.
     *
     * @param player      the player
     * @param origin      the original world the player was in
     * @param destination the new world the player was moved to
     */
    @Override
    public void afterChangeWorld(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
        NickPlayer nickPlayer = (NickPlayer) player;

        nickPlayer.pseudonym$getNickname().getNicknameLabel().destroyLabel();
        nickPlayer.pseudonym$getNickname().getNicknameLabel().createCustomLabel();
    }
}
