package com.harismehuljic.pseudonym.mixin;

import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementFrame.class)
public class AdvancementFrameMixin {
    @Shadow private final String id;

    public AdvancementFrameMixin(String id) {
        this.id = id;
    }

    @Inject(at = @At("RETURN"), method = "getChatAnnouncementText", cancellable = true)
    public void onGetChatAnnouncementText(AdvancementEntry advancementEntry, ServerPlayerEntity player, CallbackInfoReturnable<MutableText> cir) {
        NickPlayer nickPlayer = (NickPlayer) player;
        MutableText announcementPlayerName = player.getDisplayName().copy();

        if (nickPlayer.pseudonym$getNickname().getNickname() != null) {
            announcementPlayerName.append(Text.literal(" ("))
                    .append(nickPlayer.pseudonym$getNickname().getRealName().copy().formatted(Formatting.ITALIC))
                    .append(Text.literal(")"));
        }

        cir.setReturnValue(Text.translatable("chat.type.advancement." + this.id, new Object[]{announcementPlayerName, Advancement.getNameFromIdentity(advancementEntry)}));
    }
}
