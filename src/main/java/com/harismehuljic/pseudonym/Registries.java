package com.harismehuljic.pseudonym;

import com.harismehuljic.pseudonym.command.NickCommand;
import com.harismehuljic.pseudonym.command.PrefixCommand;
import com.harismehuljic.pseudonym.event.AllowChatMessageEvent;
import com.harismehuljic.pseudonym.event.CopyFromEvent;
import com.harismehuljic.pseudonym.event.PlayerConnectionEvents;
import com.harismehuljic.pseudonym.event.WorldChangeEvent;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Registries {
    public static void registerAll() {
        registerCommands();
        registerEvents();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(NickCommand::register);
        CommandRegistrationCallback.EVENT.register(PrefixCommand::register);
    }

    private static void registerEvents() {
        ServerPlayerEvents.COPY_FROM.register(new CopyFromEvent());
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(new AllowChatMessageEvent());
        ServerPlayConnectionEvents.JOIN.register(new PlayerConnectionEvents.PlayerJoinEvent());
        ServerPlayConnectionEvents.DISCONNECT.register(new PlayerConnectionEvents.PlayerLeaveEvent());
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(new WorldChangeEvent());

    }
}
