package com.harismehuljic.pseudonym.mixin;

import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementType.class)
public class AdvancementTypeMixin {
    @Shadow
    private final String name;

    public AdvancementTypeMixin(String id) {
        this.name = id;
    }

    @Inject(at = @At("RETURN"), method = "createAnnouncement", cancellable = true)
    public void onGetChatAnnouncementText(AdvancementHolder advancementEntry, ServerPlayer player, CallbackInfoReturnable<MutableComponent> cir) {
        NickPlayer nickPlayer = (NickPlayer) player;
        MutableComponent announcementPlayerName = player.getDisplayName().copy();

        if (nickPlayer.pseudonym$getNickname().getNickname() != null) {
            announcementPlayerName.append(Component.literal(" ("))
                    .append(nickPlayer.pseudonym$getNickname().getRealName().copy().withStyle(ChatFormatting.ITALIC))
                    .append(Component.literal(")"));
        }

        cir.setReturnValue(Component.translatable("chat.type.advancement." + this.name, announcementPlayerName, Advancement.name(advancementEntry)));
    }
}
