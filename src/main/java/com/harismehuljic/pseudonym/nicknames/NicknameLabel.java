package com.harismehuljic.pseudonym.nicknames;

import com.harismehuljic.pseudonym.nicknames.impl.NickPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.AffineTransformation;
import org.joml.Vector3f;

public class NicknameLabel {
    private final ServerPlayerEntity spe;
    private final NickPlayer nickPlayer;
    private DisplayEntity.TextDisplayEntity label;
    private boolean sneakChecked = false;

    public NicknameLabel(ServerPlayerEntity spe) {
        this.spe = spe;
        this.nickPlayer = (NickPlayer) spe;
    }

    public void createCustomLabel() {
        ServerWorld world = this.spe.getWorld();

        this.label = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
        this.label.setPosition(this.spe.getPos());
        this.tickLabel(true);

        world.spawnEntity(this.label);
    }

    public void updateLabel() {
        Text formattedName = this.nickPlayer.pseudonym$getNickname().getFinalStylizedName();
        this.label.setText(formattedName);
        this.label.setBillboardMode(DisplayEntity.BillboardMode.CENTER);

        if (this.sneakChecked) {
            this.label.setDisplayFlags((byte) 4);
            this.label.setTextOpacity((byte) 180);
            this.label.setTransformation(new AffineTransformation(new Vector3f(0.0f, 0.1f, 0.0f), null, null, null));
        } else {
            this.label.setDisplayFlags((byte) 2);
            this.label.setTextOpacity((byte) 225);
            this.label.setTransformation(new AffineTransformation(new Vector3f(0.0f, 0.25f, 0.0f), null, null, null));
        }
    }

    public void tickLabel(boolean sneaking) {
        if (this.label == null || this.label.isRemoved()) {
            this.createCustomLabel();
        }

        if (sneaking == !this.sneakChecked) {
            this.sneakChecked = sneaking;
            this.updateLabel();
        }

        if (!this.spe.hasPassengers()) {
            this.label.startRiding(this.spe, true);
        }

        if (!this.spe.isAlive()) {
            this.destroyLabel();
        }
    }

    public void destroyLabel() {
        this.label.remove(Entity.RemovalReason.DISCARDED);
    }
}
