package com.harismehuljic.pseudonym.nicknames.impl;

import com.harismehuljic.pseudonym.nicknames.Nickname;
import net.minecraft.server.network.ServerPlayerEntity;

public interface NickPlayer {
    void pseudonym$readCustomData(ServerPlayerEntity spe);
    void pseudonym$writeCustomData();
    Nickname pseudonym$getNickname();
}
