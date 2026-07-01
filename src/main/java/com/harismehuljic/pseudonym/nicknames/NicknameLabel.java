package com.harismehuljic.pseudonym.nicknames;

import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import com.mojang.math.Transformation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import org.joml.Vector3f;

public class NicknameLabel {
    private final ServerPlayer spe;
    private final NickPlayer nickPlayer;
    private Display.TextDisplay label;
    private boolean playerSneaking = false;
    private boolean recreateLabel = true;

    public NicknameLabel(ServerPlayer spe) {
        this.spe = spe;
        this.nickPlayer = (NickPlayer) spe;
    }

    public void createCustomLabel() {
        ServerLevel world = this.spe.level();

        this.label = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, world);
        this.label.setPos(this.spe.position());
        this.tickLabel(true);

        world.addFreshEntity(this.label);
    }

    public void updateLabel() {
        Component formattedName = this.nickPlayer.pseudonym$getNickname().getFinalStylizedName();
        this.label.setText(formattedName);
        this.label.setBillboardConstraints(Display.BillboardConstraints.CENTER);

        if (this.playerSneaking) {
            this.label.setFlags((byte) 4);
            this.label.setTextOpacity((byte) 180);

            if (!this.spe.isVisuallySwimming()) {
                this.label.setTransformation(new Transformation(new Vector3f(0.0f, 0.1f, 0.0f), null, null, null));
            }
        } else {
            this.label.setFlags((byte) 2);
            this.label.setTextOpacity((byte) 225);
            this.label.setTransformation(new Transformation(new Vector3f(0.0f, 0.25f, 0.0f), null, null, null));
        }
    }

    public void tickLabel(boolean sneaking) {
        boolean isPlayerInvisible = this.spe.isSpectator() || this.spe.isInvisible();

        // Make the label invisible to the label's player
        this.spe.connection.send(
                new ClientboundRemoveEntitiesPacket(this.label.getId())
        );

        if ((this.label == null || this.label.isRemoved()) && this.recreateLabel && !isPlayerInvisible) {
            this.createCustomLabel();
        } else if (isPlayerInvisible) {
            this.destroyLabel();
        }

        if (sneaking == !this.playerSneaking) {
            this.playerSneaking = sneaking;
            this.updateLabel();
        }

        if (!this.spe.isVehicle()) {
            this.label.startRiding(this.spe, true, false);
        }

        if (!this.spe.isAlive()) {
            this.destroyLabel();
        }
    }

    public void destroyLabel() {
        this.label.remove(Entity.RemovalReason.DISCARDED);
    }

    public void destroyLabel(boolean persistent) {
        this.label.remove(Entity.RemovalReason.DISCARDED);
        if (persistent) this.recreateLabel = false;
    }
}
