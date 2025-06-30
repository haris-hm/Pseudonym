package com.harismehuljic.pseudonym.event;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
     * @param params  the {@link MessageType.Parameters}
     * @return {@code true} if the message should be broadcast, otherwise {@code false}
     */
    @Override
    public boolean allowChatMessage(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        String messageContents = message.getSignedContent();

        // Send the newly formatted message to each player
        for (ServerPlayerEntity spe : sender.getWorld().getServer().getPlayerManager().getPlayerList()) {
            assert sender.getDisplayName() != null;

            MutableText senderDisplayName = sender.getDisplayName().copy();
            MutableText separator = Text.literal(" 》 ").formatted(Formatting.WHITE);
            MutableText msg = Text.literal(messageContents).formatted(Formatting.WHITE);

            separator.setStyle(separator.getStyle().withBold(false).withItalic(false));
            msg.setStyle(separator.getStyle().withBold(false).withItalic(false));

            spe.sendMessage(senderDisplayName.append(separator).append(msg));
        }

        // Block the sending of the original Minecraft formatted message
        return false;
    }
}
