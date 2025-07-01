package com.harismehuljic.pseudonym.mixin;

import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements NickPlayer {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"), method = "getDisplayName")
    private Text onGetDisplayName(Text originalText) {
        NickPlayer nickPlayer = this.getServerEntity(this);

        if (nickPlayer == null) {
            return originalText;
        }

        return nickPlayer.pseudonym$getNickname().getFinalStylizedName();
    }

    @Unique
    private @Nullable NickPlayer getServerEntity(Object possiblePlayer) {
        if (possiblePlayer instanceof ServerPlayerEntity) {
            return (NickPlayer) possiblePlayer;
        }
        else {
            return null;
        }
    }
}
