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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.literal;

public class NickCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        final LiteralCommandNode<ServerCommandSource> nickNode = dispatcher.register(literal("nick")
                .then(literal("set")
                        .then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("nickname", StringArgumentType.greedyString())
                                .executes(NickCommand::setNickname)
                        )
                )
                .then(literal("color")
                        .then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("color", StringArgumentType.greedyString())
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

        dispatcher.register(literal("n").then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("nickname", StringArgumentType.greedyString())
                .executes(NickCommand::setNickname)
        ));

        dispatcher.register(literal("name").redirect(nickNode));
        dispatcher.register(literal("nickname").redirect(nickNode));
    }

    /**
     * Sets a players nickname based on the argument provided in the command
     * @param context The source executing the command.
     * @return Unimportant.
     */
    private static int setNickname(CommandContext<ServerCommandSource> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).networkHandler;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        final String nickname = StringArgumentType.getString(context, "nickname");

        if (!NickManager.validateNickname(nickname) && Pseudonym.CONFIG_DATA.enforceValidNicknameCharacters()) {
            context.getSource().sendFeedback(() -> feedbackText("Sorry, but \"", nickname, "\" isn't a valid nickname.", Formatting.LIGHT_PURPLE), false);
            return 1;
        }

        nickPlayer.pseudonym$getNickname().setNickname(nickname);
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        //nickPlayer.pseudonym$getNickname().setNicknameLabel(new NicknameLabel(context.getSource().getWorld(), context.getSource().getPlayer()));
        //nickPlayer.pseudonym$getNickname().getNicknameLabel().createCustomLabel();

        context.getSource().sendFeedback(() -> feedbackText("Your nickname has been changed to \"", nickname, "\"", nickPlayer.pseudonym$getNickname().getNickColor()), false);

        return 0;
    }

    private static int setNickColor(CommandContext<ServerCommandSource> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).networkHandler;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        final String color = StringArgumentType.getString(context, "color");

        if (!Formatting.getNames(true, false).contains(color)) {
            context.getSource().sendFeedback(() -> feedbackText("Sorry, but \"", color, "\" isn't a valid color.", Formatting.LIGHT_PURPLE), false);
            return 1;
        }

        nickPlayer.pseudonym$getNickname().setNickColor(color);
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendFeedback(() -> feedbackText("Your nickname color has been changed to \"", color, "\"", Formatting.byName(color)), false);
        return 0;
    }

    private static int italicizeNick(CommandContext<ServerCommandSource> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).networkHandler;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().setItalicizedNick(!nickPlayer.pseudonym$getNickname().isItalicizedNick());
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        Text msg = nickPlayer.pseudonym$getNickname().isItalicizedNick() ?
                Text.literal("Your nickname is now italicized.").formatted(Formatting.AQUA) :
                Text.literal("Your nickname is no longer italicized.").formatted(Formatting.AQUA);

        context.getSource().sendFeedback(() -> msg, false);
        return 0;
    }

    private static int boldNick(CommandContext<ServerCommandSource> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).networkHandler;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().setBoldNick(!nickPlayer.pseudonym$getNickname().isBoldNick());
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        Text msg = nickPlayer.pseudonym$getNickname().isBoldNick() ?
                Text.literal("Your nickname is now bold.").formatted(Formatting.AQUA) :
                Text.literal("Your nickname is no longer bold.").formatted(Formatting.AQUA);

        context.getSource().sendFeedback(() -> msg, false);
        return 0;
    }

    private static int removeNick(CommandContext<ServerCommandSource> context) {
        NickManager nickManager = (NickManager) Objects.requireNonNull(context.getSource().getPlayer()).networkHandler;
        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());

        nickPlayer.pseudonym$getNickname().removeNick();
        nickManager.pseudonym$updateDisplayName(nickPlayer);

        context.getSource().sendFeedback(() -> feedbackText("Your nickname has been removed.", "", "", Formatting.LIGHT_PURPLE), false);
        return 0;
    }

    /**
     * Displays a player's nickname back to them.
     * @param context The source executing the command.
     * @return Unimportant.
     */
    private static int parrotNickname(CommandContext<ServerCommandSource> context){
        final Text noNick = Text.literal("Your don't currently have a nickname set.").formatted(Formatting.AQUA);

        NickPlayer nickPlayer = (NickPlayer) Objects.requireNonNull(context.getSource().getPlayer());
        String nickname = nickPlayer.pseudonym$getNickname().getNickname().toString();

        Text nickText = nickname == null ? noNick : feedbackText("Your current nickname is \"", nickname, "\"", nickPlayer.pseudonym$getNickname().getNickColor());

        context.getSource().sendFeedback(() -> nickText,false);

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
