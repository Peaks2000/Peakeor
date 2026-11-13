/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.*;
import peakeordevelopment.peakeorclient.PeakeorClient;

import java.util.ArrayList;
import java.util.List;

public abstract class PeakeorRenderPipelines {
    private static final List<RenderPipeline> PIPELINES = new ArrayList<>();

    // Snippets

    private static final BindGroupLayout MESH_BIND_GROUP = BindGroupLayout.builder()
        .withUniform("MeshData", UniformType.UNIFORM_BUFFER)
        .build();

    private static final RenderPipeline.Snippet MESH_UNIFORMS = RenderPipeline.builder()
        .withBindGroupLayout(MESH_BIND_GROUP)
        .buildSnippet();

    // World

    public static final RenderPipeline WORLD_COLORED = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(PeakeorClient.identifier("pipeline/world_colored"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withVertexShader(PeakeorClient.identifier("pos_color"))
        .withFragmentShader(PeakeorClient.identifier("pos_color"))
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(false)
        .build()
    );

    public static final RenderPipeline WORLD_COLORED_LINES = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLineSmooth()
        .withLocation(PeakeorClient.identifier("pipeline/world_colored_lines"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR).withPrimitiveTopology(PrimitiveTopology.DEBUG_LINES)
        .withVertexShader(PeakeorClient.identifier("pos_color"))
        .withFragmentShader(PeakeorClient.identifier("pos_color"))
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(false)
        .build()
    );

    public static final RenderPipeline WORLD_COLORED_DEPTH = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(PeakeorClient.identifier("pipeline/world_colored_depth"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withVertexShader(PeakeorClient.identifier("pos_color"))
        .withFragmentShader(PeakeorClient.identifier("pos_color"))
        .withDepthStencilState(new DepthStencilState(DepthStencilState.DEFAULT.depthTest(), false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(false)
        .build()
    );

    public static final RenderPipeline WORLD_COLORED_LINES_DEPTH = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLineSmooth()
        .withLocation(PeakeorClient.identifier("pipeline/world_colored_lines_depth"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR).withPrimitiveTopology(PrimitiveTopology.DEBUG_LINES)
        .withVertexShader(PeakeorClient.identifier("pos_color"))
        .withFragmentShader(PeakeorClient.identifier("pos_color"))
        .withDepthStencilState(new DepthStencilState(DepthStencilState.DEFAULT.depthTest(), false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(false)
        .build()
    );

    // UI

    public static final RenderPipeline UI_COLORED = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(PeakeorClient.identifier("pipeline/ui_colored"))
        .withVertexBinding(0, PeakeorVertexFormats.POS2_COLOR).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withVertexShader(PeakeorClient.identifier("pos_color"))
        .withFragmentShader(PeakeorClient.identifier("pos_color"))
        .withShaderDefine("UI")
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(true)
        .build()
    );

    public static final RenderPipeline UI_COLORED_LINES = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(PeakeorClient.identifier("pipeline/ui_colored_lines"))
        .withVertexBinding(0, PeakeorVertexFormats.POS2_COLOR).withPrimitiveTopology(PrimitiveTopology.DEBUG_LINES)
        .withVertexShader(PeakeorClient.identifier("pos_color"))
        .withFragmentShader(PeakeorClient.identifier("pos_color"))
        .withShaderDefine("UI")
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(true)
        .build()
    );

    public static final RenderPipeline UI_TEXTURED = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(PeakeorClient.identifier("pipeline/ui_textured"))
        .withVertexBinding(0, PeakeorVertexFormats.POS2_TEXTURE_COLOR).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withVertexShader(PeakeorClient.identifier("pos_tex_color"))
        .withFragmentShader(PeakeorClient.identifier("pos_tex_color"))
        .withBindGroupLayout(BindGroupLayout.builder()
            .withUniform("u_Texture", UniformType.COMBINED_IMAGE_SAMPLER)
            .build())
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(true)
        .build()
    );

    public static final RenderPipeline UI_TEXT = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(PeakeorClient.identifier("pipeline/ui_text"))
        .withVertexBinding(0, PeakeorVertexFormats.POS2_TEXTURE_COLOR).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withVertexShader(PeakeorClient.identifier("text"))
        .withFragmentShader(PeakeorClient.identifier("text"))
        .withBindGroupLayout(BindGroupLayout.builder()
            .withUniform("u_Texture", UniformType.COMBINED_IMAGE_SAMPLER)
            .build())
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(true)
        .build()
    );

    // Post Process

    public static final RenderPipeline POST_OUTLINE = add(new ExtendedRenderPipelineBuilder()
        .withLocation(PeakeorClient.identifier("pipeline/post/outline"))
        .withVertexBinding(0, PeakeorVertexFormats.POS2).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withVertexShader(PeakeorClient.identifier("post-process/base"))
        .withFragmentShader(PeakeorClient.identifier("post-process/outline"))
        .withBindGroupLayout(BindGroupLayout.builder()
            .withUniform("u_Texture", UniformType.COMBINED_IMAGE_SAMPLER)
            .withUniform("PostData", UniformType.UNIFORM_BUFFER)
            .withUniform("OutlineData", UniformType.UNIFORM_BUFFER)
            .build())
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(false)
        .build()
    );

    public static final RenderPipeline POST_IMAGE = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(PeakeorClient.identifier("pipeline/post/image"))
        .withVertexBinding(0, PeakeorVertexFormats.POS2).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withVertexShader(PeakeorClient.identifier("post-process/base"))
        .withFragmentShader(PeakeorClient.identifier("post-process/image"))
        .withBindGroupLayout(BindGroupLayout.builder()
            .withUniform("u_Texture", UniformType.COMBINED_IMAGE_SAMPLER)
            .withUniform("u_TextureI", UniformType.COMBINED_IMAGE_SAMPLER)
            .withUniform("PostData", UniformType.UNIFORM_BUFFER)
            .withUniform("ImageData", UniformType.UNIFORM_BUFFER)
            .build())
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(false)
        .build()
    );

    // Blur

    public static final RenderPipeline BLUR_DOWN = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(PeakeorClient.identifier("pipeline/blur/down"))
        .withVertexBinding(0, PeakeorVertexFormats.POS2).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withVertexShader(PeakeorClient.identifier("blur"))
        .withFragmentShader(PeakeorClient.identifier("blur_down"))
        .withBindGroupLayout(BindGroupLayout.builder()
            .withUniform("u_Texture", UniformType.COMBINED_IMAGE_SAMPLER)
            .withUniform("BlurData", UniformType.UNIFORM_BUFFER)
            .build())
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(false)
        .build()
    );

    public static final RenderPipeline BLUR_UP = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(PeakeorClient.identifier("pipeline/blur/up"))
        .withVertexBinding(0, PeakeorVertexFormats.POS2).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withVertexShader(PeakeorClient.identifier("blur"))
        .withFragmentShader(PeakeorClient.identifier("blur_up"))
        .withBindGroupLayout(BindGroupLayout.builder()
            .withUniform("u_Texture", UniformType.COMBINED_IMAGE_SAMPLER)
            .withUniform("BlurData", UniformType.UNIFORM_BUFFER)
            .build())
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(false)
        .build()
    );

    public static final RenderPipeline BLUR_PASSTHROUGH = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(PeakeorClient.identifier("pipeline/blur/passthrough"))
        .withVertexBinding(0, PeakeorVertexFormats.POS2).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withVertexShader(PeakeorClient.identifier("passthrough"))
        .withFragmentShader(PeakeorClient.identifier("passthrough"))
        .withBindGroupLayout(BindGroupLayout.builder().withUniform("u_Texture", UniformType.COMBINED_IMAGE_SAMPLER).build())
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(false)
        .build()
    );

    private static RenderPipeline add(RenderPipeline pipeline) {
        PIPELINES.add(pipeline);
        return pipeline;
    }

    public static List<RenderPipeline> getPipelines() {
        return List.copyOf(PIPELINES);
    }

    private PeakeorRenderPipelines() {
    }
}