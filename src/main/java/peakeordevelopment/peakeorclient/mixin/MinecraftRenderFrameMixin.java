/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import peakeordevelopment.peakeorclient.renderer.MeshUniforms;
import peakeordevelopment.peakeorclient.systems.modules.Modules;
import peakeordevelopment.peakeorclient.systems.modules.misc.InventoryTweaks;
import peakeordevelopment.peakeorclient.utils.render.postprocess.ChamsShader;
import peakeordevelopment.peakeorclient.utils.render.postprocess.OutlineUniforms;
import peakeordevelopment.peakeorclient.utils.render.postprocess.PostProcessShader;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static peakeordevelopment.peakeorclient.PeakeorClient.mc;

@Mixin(Minecraft.class)
public abstract class MinecraftRenderFrameMixin {
    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;endFrame()V", shift = At.Shift.AFTER))
    private void peakeor$afterRenderFrame(boolean advanceGameTime, CallbackInfo ci) {
        MeshUniforms.flipFrame();
        PostProcessShader.flipFrame();
        ChamsShader.flipFrame();
        OutlineUniforms.flipFrame();

        Modules modules = Modules.get();
        if (modules == null || mc.player == null) return;

        InventoryTweaks inventoryTweaks = modules.get(InventoryTweaks.class);
        if (inventoryTweaks != null && inventoryTweaks.frameInput()) {
            ((MinecraftAccessor) mc).peakeor$handleInputEvents();
        }
    }
}
