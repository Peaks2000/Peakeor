/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderPassBackend;
import peakeordevelopment.peakeorclient.mixininterface.IGpuDevice;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GpuDevice.class)
public abstract class GpuDeviceMixin implements IGpuDevice {
    @Shadow
    @Final
    private GpuDeviceBackend backend;

    @Override
    public void peakeor$pushScissor(int x, int y, int width, int height) {
        ((IGpuDevice) backend).peakeor$pushScissor(x, y, width, height);
    }

    @Override
    public void peakeor$popScissor() {
        ((IGpuDevice) backend).peakeor$popScissor();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void peakeor$onCreateRenderPass(RenderPassBackend backend) {
        ((IGpuDevice) this.backend).peakeor$onCreateRenderPass(backend);
    }
}
