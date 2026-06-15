package com.harismehuljic.pseudonym.mixin;

import com.harismehuljic.pseudonym.data.PlayerData;
import com.harismehuljic.pseudonym.nicknames.Nickname;
import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements NickPlayer {
    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Shadow
    @Final
    public MinecraftServer server;
    @Unique
    private Nickname nickname;

    public ServerPlayerMixin(ServerLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)V")
    private void onInit(CallbackInfo info) {
        this.nickname = new Nickname(this.getGameProfile());
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo ci) {
        this.nickname.getNicknameLabel().tickLabel(this.isShiftKeyDown());
    }

    @Inject(at = @At("TAIL"), method = "getTabListDisplayName", cancellable = true)
    private void onGetPlayerListName(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(PlayerTeam.formatNameForTeam(this.getTeam(), this.nickname.getFinalStylizedName()));
    }

    public void pseudonym$readCustomData(ServerPlayer spe) {
        CompoundTag nbt = PlayerData.readPlayerData(this.server, this.connection.getPlayer());

        this.nickname.loadNicknameData(nbt == null ? new CompoundTag() : nbt, spe);
    }

    public void pseudonym$writeCustomData() {
        this.nickname.getNicknameLabel().destroyLabel(true);
        PlayerData.savePlayerData(this.server, this.connection.getPlayer(), this.nickname.getNicknameData());
    }

    public Nickname pseudonym$getNickname() {
        return this.nickname;
    }
}
