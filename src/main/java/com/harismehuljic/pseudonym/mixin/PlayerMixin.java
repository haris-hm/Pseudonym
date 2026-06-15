package com.harismehuljic.pseudonym.mixin;

import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements NickPlayer {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/PlayerTeam;formatNameForTeam(Lnet/minecraft/world/scores/Team;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/MutableComponent;"), method = "getDisplayName")
    private Component onGetDisplayName(Component originalText) {
        NickPlayer nickPlayer = this.getServerEntity(this);

        if (nickPlayer == null) {
            return originalText;
        }

        return nickPlayer.pseudonym$getNickname().getFinalStylizedName();
    }

    @Unique
    private @Nullable NickPlayer getServerEntity(Object possiblePlayer) {
        if (possiblePlayer instanceof ServerPlayer) {
            return (NickPlayer) possiblePlayer;
        } else {
            return null;
        }
    }
}
