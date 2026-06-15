package com.harismehuljic.pseudonym.nicknames.impl;

import com.harismehuljic.pseudonym.nicknames.Nickname;
import net.minecraft.server.level.ServerPlayer;

public interface NickPlayer {
    void pseudonym$readCustomData(ServerPlayer spe);
    void pseudonym$writeCustomData();
    Nickname pseudonym$getNickname();
}
