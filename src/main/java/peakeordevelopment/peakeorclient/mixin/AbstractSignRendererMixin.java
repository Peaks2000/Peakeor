/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import peakeordevelopment.peakeorclient.systems.modules.Modules;
import peakeordevelopment.peakeorclient.systems.modules.render.NoRender;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignRendererMixin {
    @Inject(method = "submitSignText", at = @At("HEAD"), cancellable = true)
    private void onSubmitSignText(SignRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, SignText signText, CallbackInfo ci) {
        Modules modules = Modules.get();
        if (modules == null) return;

        NoRender noRender = modules.get(NoRender.class);
        if (noRender != null && noRender.noSignText()) ci.cancel();
    }
}
