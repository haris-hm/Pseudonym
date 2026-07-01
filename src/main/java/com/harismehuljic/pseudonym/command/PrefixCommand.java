package com.harismehuljic.pseudonym.command;

import com.harismehuljic.pseudonym.Pseudonym;
import com.harismehuljic.pseudonym.nicknames.Formatting;
import com.harismehuljic.pseudonym.nicknames.impl.NickManager;
import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Objects;

import static net.minecraft.commands.Commands.literal;

public class PrefixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        dispatcher.register(literal("prefix")
                .then(literal("set")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("prefix", StringArgumentType.greedyString())
                                .executes(PrefixCommand::setPrefix)
                        )
                )
                .then(literal("color")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("color", StringArgumentType.greedyString())
                                .suggests(COLOR_PROVIDER)
                                .executes(PrefixCommand::setPrefixColor)
                        )
                )
                .then(literal("bold")
                        .executes(PrefixCommand::boldPrefix)
                )
                .then(literal("italic")
                        .executes(PrefixCommand::italicizePrefix)
                )
                .then(literal("remove")
                        .executes(PrefixCommand::removePrefix)
                )
                .executes(PrefixCommand::parrotPrefix)
        );

        dispatcher.register(literal("p").then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("prefix", StringArgumentType.greedyString())
                .executes(PrefixCommand::setPrefix)
        ));
    }

    /**
     * Sets a players prefix based on the argument provided in the command
     *
     * @param context The source executing the command.
     * @return Unimportant.
     */
    private static int setPrefix(CommandContext<CommandSourceStack> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).connection;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        final String prefix = StringArgumentType.getString(context, "prefix");

        if (!NickManager.validateNickname(prefix) && Pseudonym.CONFIG_DATA.enforceValidNicknameCharacters()) {
            context.getSource().sendSuccess(() -> feedbackText("Sorry, but \"", prefix, "\" isn't a valid prefix.", ChatFormatting.LIGHT_PURPLE), false);
            return 1;
        }

        nickPlayer.pseudonym$getNickname().setPrefix(prefix);
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendSuccess(() -> feedbackText("Your prefix has been changed to \"", prefix, "\"", nickPlayer.pseudonym$getNickname().getPrefixColor()), false);

        return 0;
    }

    private static int setPrefixColor(CommandContext<CommandSourceStack> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).connection;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        final String color = StringArgumentType.getString(context, "color");

        if (!Formatting.getIds().contains(color)) {
            context.getSource().sendSuccess(() -> feedbackText("Sorry, but \"", color, "\" isn't a valid color.", ChatFormatting.LIGHT_PURPLE), false);
            return 1;
        }

        nickPlayer.pseudonym$getNickname().setPrefixColor(color);
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendSuccess(() -> feedbackText("Your prefix color has been changed to \"", color, "\"", Formatting.getByName(color).getFormat()), false);
        return 0;
    }

    private static int removePrefix(CommandContext<CommandSourceStack> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).connection;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().removePrefix();
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendSuccess(() -> feedbackText("Your prefix has been removed.", "", "", ChatFormatting.LIGHT_PURPLE), false);
        return 0;
    }

    private static int italicizePrefix(CommandContext<CommandSourceStack> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).connection;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().setItalicizedPrefix(!nickPlayer.pseudonym$getNickname().isItalicizedPrefix());
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        Component msg = nickPlayer.pseudonym$getNickname().isItalicizedPrefix() ?
                Component.literal("Your prefix is now italicized.").withStyle(ChatFormatting.AQUA) :
                Component.literal("Your prefix is no longer italicized.").withStyle(ChatFormatting.AQUA);

        context.getSource().sendSuccess(() -> msg, false);
        return 0;
    }

    private static int boldPrefix(CommandContext<CommandSourceStack> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).connection;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().setBoldPrefix(!nickPlayer.pseudonym$getNickname().isBoldPrefix());
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        Component msg = nickPlayer.pseudonym$getNickname().isBoldPrefix() ?
                Component.literal("Your prefix is now bold.").withStyle(ChatFormatting.AQUA) :
                Component.literal("Your prefix is no longer bold.").withStyle(ChatFormatting.AQUA);

        context.getSource().sendSuccess(() -> msg, false);
        return 0;
    }

    private static int parrotPrefix(CommandContext<CommandSourceStack> context) {
        final Component noPrefix = Component.literal("Your don't currently have a prefix set.").withStyle(ChatFormatting.AQUA);

        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        Component prefix = nickPlayer.pseudonym$getNickname().getPrefix();

        Component prefixText = prefix == null ? noPrefix : feedbackText("Your current prefix is \"", prefix.getString(), "\"", nickPlayer.pseudonym$getNickname().getPrefixColor());

        context.getSource().sendSuccess(() -> prefixText, false);

        return 0;
    }

    private static final SuggestionProvider<CommandSourceStack> COLOR_PROVIDER = (source, builder) -> {
        return SharedSuggestionProvider.suggest(Formatting.getIds(), builder);
    };

    private static Component feedbackText(String intro, String var, String end, ChatFormatting varColor) {
        return Component.literal(intro)
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(var).withStyle(varColor, ChatFormatting.ITALIC))
                .append(Component.literal(end).withStyle(ChatFormatting.RESET, ChatFormatting.AQUA));
    }
}
