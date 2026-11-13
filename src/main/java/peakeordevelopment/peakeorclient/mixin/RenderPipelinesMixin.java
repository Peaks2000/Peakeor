/*
 * This file is part of the Meteor Client distribution (https://github.com/PeakeorDevelopment/peakeor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import peakeordevelopment.peakeorclient.renderer.PeakeorRenderPipelines;
import java.util.ArrayList;
import java.util.List;

@Mixin(RenderPipelines.class)
public abstract class RenderPipelinesMixin {
    @ModifyReturnValue(method = "requiredPipelines", at = @At("RETURN"))
    private static List<RenderPipeline> peakeor$includePipelines(List<RenderPipeline> original) {
        List<RenderPipeline> pipelines = new ArrayList<>(original);
        pipelines.addAll(PeakeorRenderPipelines.getPipelines());
        return pipelines;
    }
}
