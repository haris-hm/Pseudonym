package com.harismehuljic.pseudonym.nicknames;

import com.harismehuljic.pseudonym.Pseudonym;
import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.AffineTransformation;
import org.joml.Vector3f;

import java.util.Optional;

public class NicknameLabel {
    private final ServerPlayerEntity spe;
    private final NickPlayer nickPlayer;
    //private final Scoreboard scoreboard;

    private boolean sneakChecked = false;

    //Team team;
    DisplayEntity.TextDisplayEntity label;

    public NicknameLabel(ServerPlayerEntity spe) {
        this.spe = spe;
        this.nickPlayer = (NickPlayer) spe;
    }

    public void createCustomLabel() {
        ServerWorld world = this.spe.getWorld();

        Optional<Entity> labelFromNbt = EntityType.getEntityFromData(this.generateLabelData(!this.spe.isSneaking()), world, SpawnReason.COMMAND);

        this.label = (DisplayEntity.TextDisplayEntity) labelFromNbt.get();
        this.label.startRiding(this.spe);
        world.spawnEntity(this.label);
    }

    public void updateLabel(boolean nameVisible) {
        this.label.readData(this.generateLabelData(nameVisible));
    }

    public void hideLabel(boolean sneaking) {
        if (sneaking && !this.sneakChecked) {
            this.label.readData(this.generateLabelData(false));
            this.sneakChecked = true;
        }
        else if (!sneaking && this.sneakChecked) {
            this.label.readData(this.generateLabelData(true));
            this.sneakChecked = false;
        }
    }

    public void destroyLabel() {
        this.label.remove(Entity.RemovalReason.DISCARDED);
    }

    private ReadView toReadView(NbtCompound nbt) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "LabelReadView", Pseudonym.LOGGER)) {
            return NbtReadView.create(logging, this.spe.getWorld().getRegistryManager(), nbt);
        }
    }

    private ReadView generateLabelData(boolean nameVisible) {
        NbtCompound nbt = new NbtCompound();

        String nickname = String.format("{\"text\":\"%s\",\"italic\":%s,\"color\":\"%s\",\"bold\":%s}",
                this.nickPlayer.pseudonym$getNickname().getNickname() == null ?
                        this.nickPlayer.pseudonym$getNickname().getRealName().getString() :
                        this.nickPlayer.pseudonym$getNickname().getNickname().getString(),
                this.nickPlayer.pseudonym$getNickname().isItalicizedNick() ? "true" : "false",
                this.nickPlayer.pseudonym$getNickname().getNickColor().getName(),
                this.nickPlayer.pseudonym$getNickname().isBoldNick() ? "true" : "false"
        );

        String formattedName = this.nickPlayer.pseudonym$getNickname().getPrefix() != null ?
                String.format("[{\"text\":\"[%s] \",\"italic\":%s,\"color\":\"%s\",\"bold\":%s}, %s]",
                        this.nickPlayer.pseudonym$getNickname().getPrefix().getString(),
                        this.nickPlayer.pseudonym$getNickname().isItalicizedPrefix() ? "true" : "false",
                        this.nickPlayer.pseudonym$getNickname().getPrefixColor().getName(),
                        this.nickPlayer.pseudonym$getNickname().isBoldPrefix() ? "true" : "false",
                        nickname
                ) :
                nickname;

        AffineTransformation.ANY_CODEC.encodeStart(NbtOps.INSTANCE, new AffineTransformation(new Vector3f(0.0f, 0.3f, 0.0f), null, null, null)).ifSuccess((transformations) -> {
            nbt.put("transformation", transformations);
        });
        nbt.putString("billboard", "center");
        nbt.putString("alignment", "center");
        nbt.putString("text", formattedName);
        nbt.putBoolean("see_through", nameVisible);
        nbt.putString("id", "minecraft:text_display");

        return this.toReadView(nbt);
    }
}
