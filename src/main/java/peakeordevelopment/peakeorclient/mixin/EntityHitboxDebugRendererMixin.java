/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import peakeordevelopment.peakeorclient.systems.modules.Modules;
import peakeordevelopment.peakeorclient.systems.modules.combat.Hitboxes;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class EntityHitboxDebugRendererMixin {
    @ModifyExpressionValue(method = "showHitboxes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;", ordinal = 0))
    private AABB peakeor$createHitbox(AABB original, Entity entity, float partialTicks, boolean isServerEntity) {
        double v = Modules.get().get(Hitboxes.class).getEntityValue(entity);
        if (v == 0) return original;

        return original.inflate(v);
    }
}
