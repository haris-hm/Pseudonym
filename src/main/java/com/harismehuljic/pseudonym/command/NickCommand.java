package com.harismehuljic.pseudonym.command;

import com.harismehuljic.pseudonym.Pseudonym;
import com.harismehuljic.pseudonym.nicknames.impl.NickManager;
import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public class NickCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        final LiteralCommandNode<CommandSourceStack> nickNode = dispatcher.register(literal("nick")
                .then(literal("set")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("nickname", StringArgumentType.greedyString())
                                .executes(NickCommand::setNickname)
                        )
                )
                .then(literal("color")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("color", StringArgumentType.greedyString())
                                .suggests(COLOR_PROVIDER)
                                .executes(NickCommand::setNickColor)
                        )
                )
                .then(literal("bold")
                        .executes(NickCommand::boldNick)
                )
                .then(literal("italic")
                        .executes(NickCommand::italicizeNick)
                )
                .then(literal("remove")
                        .executes(NickCommand::removeNick)
                )
                .executes(NickCommand::parrotNickname)
        );

        dispatcher.register(literal("n").then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("nickname", StringArgumentType.greedyString())
                .executes(NickCommand::setNickname)
        ));

        dispatcher.register(literal("name").redirect(nickNode).executes(NickCommand::parrotNickname));
        dispatcher.register(literal("nickname").redirect(nickNode).executes(NickCommand::parrotNickname));
    }

    /**
     * Sets a players nickname based on the argument provided in the command
     *
     * @param context The source executing the command.
     * @return Unimportant.
     */
    private static int setNickname(CommandContext<CommandSourceStack> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).connection;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        final String nickname = StringArgumentType.getString(context, "nickname");

        if (!NickManager.validateNickname(nickname) && Pseudonym.CONFIG_DATA.enforceValidNicknameCharacters()) {
            context.getSource().sendSuccess(() -> feedbackText("Sorry, but \"", nickname, "\" isn't a valid nickname.", ChatFormatting.LIGHT_PURPLE), false);
            return 1;
        }

        nickPlayer.pseudonym$getNickname().setNickname(nickname);
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendSuccess(() -> feedbackText("Your nickname has been changed to \"", nickname, "\"", nickPlayer.pseudonym$getNickname().getNickColor()), false);

        return 0;
    }

    private static int setNickColor(CommandContext<CommandSourceStack> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).connection;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        final String color = StringArgumentType.getString(context, "color");

        if (!ChatFormatting.getNames(true, false).contains(color)) {
            context.getSource().sendSuccess(() -> feedbackText("Sorry, but \"", color, "\" isn't a valid color.", ChatFormatting.LIGHT_PURPLE), false);
            return 1;
        }

        nickPlayer.pseudonym$getNickname().setNickColor(color);
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendSuccess(() -> feedbackText("Your nickname color has been changed to \"", color, "\"", ChatFormatting.getByName(color)), false);
        return 0;
    }

    private static int italicizeNick(CommandContext<CommandSourceStack> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).connection;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().setItalicizedNick(!nickPlayer.pseudonym$getNickname().isItalicizedNick());
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        Component msg = nickPlayer.pseudonym$getNickname().isItalicizedNick() ?
                Component.literal("Your nickname is now italicized.").withStyle(ChatFormatting.AQUA) :
                Component.literal("Your nickname is no longer italicized.").withStyle(ChatFormatting.AQUA);

        context.getSource().sendSuccess(() -> msg, false);
        return 0;
    }

    private static int boldNick(CommandContext<CommandSourceStack> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).connection;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().setBoldNick(!nickPlayer.pseudonym$getNickname().isBoldNick());
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        Component msg = nickPlayer.pseudonym$getNickname().isBoldNick() ?
                Component.literal("Your nickname is now bold.").withStyle(ChatFormatting.AQUA) :
                Component.literal("Your nickname is no longer bold.").withStyle(ChatFormatting.AQUA);

        context.getSource().sendSuccess(() -> msg, false);
        return 0;
    }

    private static int removeNick(CommandContext<CommandSourceStack> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).connection;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().removeNick();
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendSuccess(() -> feedbackText("Your nickname has been removed.", "", "", ChatFormatting.LIGHT_PURPLE), false);
        return 0;
    }

    /**
     * Displays a player's nickname back to them.
     *
     * @param context The source executing the command.
     * @return Unimportant.
     */
    private static int parrotNickname(CommandContext<CommandSourceStack> context) {
        final Component noNick = Component.literal("Your don't currently have a nickname set.").withStyle(ChatFormatting.AQUA);

        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        Component nickname = nickPlayer.pseudonym$getNickname().getNickname();

        Component nickText = nickname == null ? noNick : feedbackText("Your current nickname is \"", nickname.getString(), "\"", nickPlayer.pseudonym$getNickname().getNickColor());

        context.getSource().sendSuccess(() -> nickText, false);

        return 0;
    }

    private static final SuggestionProvider<CommandSourceStack> COLOR_PROVIDER = (source, builder) -> {
        return SharedSuggestionProvider.suggest(ChatFormatting.getNames(true, false), builder);
    };

    private static Component feedbackText(String intro, String var, String end, ChatFormatting varColor) {
        return Component.literal(intro)
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(var).withStyle(varColor, ChatFormatting.ITALIC))
                .append(Component.literal(end).withStyle(ChatFormatting.RESET, ChatFormatting.AQUA));
    }
}
