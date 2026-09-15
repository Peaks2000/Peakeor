/*
 * This file is part of the Meteor Client distribution (https://github.com/PeakeorDevelopment/peakeor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.renderer;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import peakeordevelopment.peakeorclient.mixininterface.IRenderPipeline;
import org.jspecify.annotations.NonNull;

public class ExtendedRenderPipelineBuilder extends RenderPipeline.Builder {
    private boolean lineSmooth;

    public ExtendedRenderPipelineBuilder(RenderPipeline.Snippet... snippets) {
        for (RenderPipeline.Snippet snippet : snippets) {
            withSnippet(snippet);
        }
    }

    public ExtendedRenderPipelineBuilder withLineSmooth() {
        lineSmooth = true;
        return this;
    }

    @Override
    public @NonNull RenderPipeline build() {
        RenderPipeline pipeline = super.build();
        ((IRenderPipeline) pipeline).peakeor$setLineSmooth(lineSmooth);

        return pipeline;
    }
}
