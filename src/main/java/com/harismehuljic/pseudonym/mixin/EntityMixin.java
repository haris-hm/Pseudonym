package com.harismehuljic.pseudonym.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {
    /**
     * Redirect the call to EntityType#isSaveable that appears in
     * Entity#startRiding(Entity, boolean).
     */
    @Redirect(
            method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"
            )
    )
    private boolean pseudonym$allowPlayerVehicles(EntityType instance) {
        // Pretend players are saveable so the condition evaluates to false
        if (instance == EntityType.PLAYER) {
            return true;
        }
        // All other entity types keep their normal behaviour
        return instance.canSerialize();
    }
}
