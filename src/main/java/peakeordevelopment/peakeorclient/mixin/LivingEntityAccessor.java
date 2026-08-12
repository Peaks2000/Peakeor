/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker("jumpInLiquid")
    void peakeor$swimUpwards(TagKey<Fluid> fluid);

    @Accessor("jumping")
    boolean peakeor$isJumping();

    @Accessor("noJumpDelay")
    int peakeor$getJumpCooldown();

    @Accessor("noJumpDelay")
    void peakeor$setJumpCooldown(int cooldown);
}
