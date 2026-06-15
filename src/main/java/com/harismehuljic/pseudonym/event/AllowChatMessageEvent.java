package com.harismehuljic.pseudonym.event;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

public class AllowChatMessageEvent implements ServerMessageEvents.AllowChatMessage {
    /**
     * Called when the server broadcasts a chat message sent by a player, typically
     * from a client GUI or a player-executed command. Returning {@code false}
     * prevents the message from being broadcast and the {@link #CHAT_MESSAGE} event
     * from triggering.
     *
     * <p>If the message is from a player-executed command, this will be called
     * only if {@link #ALLOW_COMMAND_MESSAGE} event did not block the message,
     * and after triggering {@link #COMMAND_MESSAGE} event.
     *
     * @param message the broadcast message with message decorators applied; use {@code message.getContent()} to get the text
     * @param sender  the player that sent the message
     * @param params  the {@link ChatType.Bound}
     * @return {@code true} if the message should be broadcast, otherwise {@code false}
     */
    @Override
    public boolean allowChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params) {
        String messageContents = message.signedContent();

        // Send the newly formatted message to each player
        for (ServerPlayer spe : sender.level().getServer().getPlayerList().getPlayers()) {
            assert sender.getDisplayName() != null;

            MutableComponent senderDisplayName = sender.getDisplayName().copy();
            MutableComponent separator = Component.literal(" 》 ").withStyle(ChatFormatting.WHITE);
            MutableComponent msg = Component.literal(messageContents).withStyle(ChatFormatting.WHITE);

            separator.setStyle(separator.getStyle().withBold(false).withItalic(false));
            msg.setStyle(separator.getStyle().withBold(false).withItalic(false));

            spe.sendSystemMessage(senderDisplayName.append(separator).append(msg));
        }

        // Block the sending of the original Minecraft formatted message
        return false;
    }
}
