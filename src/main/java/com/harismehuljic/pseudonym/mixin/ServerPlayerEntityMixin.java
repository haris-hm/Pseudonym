package com.harismehuljic.pseudonym.mixin;

import com.harismehuljic.pseudonym.data.PlayerData;
import com.harismehuljic.pseudonym.nicknames.Nickname;
import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements NickPlayer {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Shadow @Final
    public MinecraftServer server;
    @Unique
    private Nickname nickname;

    public ServerPlayerEntityMixin(ServerWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/world/ServerWorld;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/packet/c2s/common/SyncedClientOptions;)V")
    private void onInit(CallbackInfo info) {
        this.nickname = new Nickname(this.getGameProfile());
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo ci) {
        this.nickname.getNicknameLabel().tickLabel(this.isSneaking());
    }

    @Inject(at = @At("TAIL"), method = "getPlayerListName", cancellable = true)
    private void onGetPlayerListName(CallbackInfoReturnable<Text> cir) {
        cir.setReturnValue(Team.decorateName(this.getScoreboardTeam(), this.nickname.getFinalStylizedName()));
    }

    public void pseudonym$readCustomData(ServerPlayerEntity spe) {
        NbtCompound nbt = PlayerData.readPlayerData(this.getServer(), this.networkHandler.getPlayer());

        this.nickname.loadNicknameData(nbt == null ? new NbtCompound() : nbt, spe);
    }

    public void pseudonym$writeCustomData() {
        this.nickname.getNicknameLabel().destroyLabel();
        PlayerData.savePlayerData(this.getServer(), this.networkHandler.getPlayer(), this.nickname.getNicknameData());
    }

    public Nickname pseudonym$getNickname() {
        return this.nickname;
    }
}
