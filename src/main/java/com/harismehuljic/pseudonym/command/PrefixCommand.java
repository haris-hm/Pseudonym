package com.harismehuljic.pseudonym.command;

import com.harismehuljic.pseudonym.Pseudonym;
import com.harismehuljic.pseudonym.nicknames.impl.NickManager;
import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.literal;

public class PrefixCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("prefix")
                .then(literal("set")
                        .then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("prefix", StringArgumentType.greedyString())
                                .executes(PrefixCommand::setPrefix)
                        )
                )
                .then(literal("color")
                        .then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("color", StringArgumentType.greedyString())
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

        dispatcher.register(literal("p").then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("prefix", StringArgumentType.greedyString())
                .executes(PrefixCommand::setPrefix)
        ));
    }

    /**
     * Sets a players prefix based on the argument provided in the command
     *
     * @param context The source executing the command.
     * @return Unimportant.
     */
    private static int setPrefix(CommandContext<ServerCommandSource> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).networkHandler;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        final String prefix = StringArgumentType.getString(context, "prefix");

        if (!NickManager.validateNickname(prefix) && Pseudonym.CONFIG_DATA.enforceValidNicknameCharacters()) {
            context.getSource().sendFeedback(() -> feedbackText("Sorry, but \"", prefix, "\" isn't a valid prefix.", Formatting.LIGHT_PURPLE), false);
            return 1;
        }

        nickPlayer.pseudonym$getNickname().setPrefix(prefix);
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendFeedback(() -> feedbackText("Your prefix has been changed to \"", prefix, "\"", nickPlayer.pseudonym$getNickname().getPrefixColor()), false);

        return 0;
    }

    private static int setPrefixColor(CommandContext<ServerCommandSource> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).networkHandler;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        final String color = StringArgumentType.getString(context, "color");

        if (!Formatting.getNames(true, false).contains(color)) {
            context.getSource().sendFeedback(() -> feedbackText("Sorry, but \"", color, "\" isn't a valid color.", Formatting.LIGHT_PURPLE), false);
            return 1;
        }

        nickPlayer.pseudonym$getNickname().setPrefixColor(color);
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendFeedback(() -> feedbackText("Your prefix color has been changed to \"", color, "\"", Formatting.byName(color)), false);
        return 0;
    }

    private static int removePrefix(CommandContext<ServerCommandSource> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).networkHandler;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().removePrefix();
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendFeedback(() -> feedbackText("Your prefix has been removed.", "", "", Formatting.LIGHT_PURPLE), false);
        return 0;
    }

    private static int italicizePrefix(CommandContext<ServerCommandSource> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).networkHandler;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().setItalicizedPrefix(!nickPlayer.pseudonym$getNickname().isItalicizedPrefix());
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        Text msg = nickPlayer.pseudonym$getNickname().isItalicizedPrefix() ?
                Text.literal("Your prefix is now italicized.").formatted(Formatting.AQUA) :
                Text.literal("Your prefix is no longer italicized.").formatted(Formatting.AQUA);

        context.getSource().sendFeedback(() -> msg, false);
        return 0;
    }

    private static int boldPrefix(CommandContext<ServerCommandSource> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).networkHandler;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().setBoldPrefix(!nickPlayer.pseudonym$getNickname().isBoldPrefix());
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        Text msg = nickPlayer.pseudonym$getNickname().isBoldPrefix() ?
                Text.literal("Your prefix is now bold.").formatted(Formatting.AQUA) :
                Text.literal("Your prefix is no longer bold.").formatted(Formatting.AQUA);

        context.getSource().sendFeedback(() -> msg, false);
        return 0;
    }

    private static int parrotPrefix(CommandContext<ServerCommandSource> context) {
        final Text noPrefix = Text.literal("Your don't currently have a prefix set.").formatted(Formatting.AQUA);

        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        Text prefix = nickPlayer.pseudonym$getNickname().getPrefix();

        Text prefixText = prefix == null ? noPrefix : feedbackText("Your current prefix is \"", prefix.getString(), "\"", nickPlayer.pseudonym$getNickname().getPrefixColor());

        context.getSource().sendFeedback(() -> prefixText, false);

        return 0;
    }

    private static final SuggestionProvider<ServerCommandSource> COLOR_PROVIDER = (source, builder) -> {
        return CommandSource.suggestMatching(Formatting.getNames(true, false), builder);
    };

    private static Text feedbackText(String intro, String var, String end, Formatting varColor) {
        return Text.literal(intro)
                .formatted(Formatting.AQUA)
                .append(Text.literal(var).formatted(varColor, Formatting.ITALIC))
                .append(Text.literal(end).formatted(Formatting.RESET, Formatting.AQUA));
    }
}
