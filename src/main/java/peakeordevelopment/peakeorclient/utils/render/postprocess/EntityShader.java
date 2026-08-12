package peakeordevelopment.peakeorclient.utils.render.postprocess;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import peakeordevelopment.peakeorclient.mixininterface.ILevelRenderer;
import net.minecraft.world.entity.Entity;

import static peakeordevelopment.peakeorclient.PeakeorClient.mc;

public abstract class EntityShader extends PostProcessShader {
    protected EntityShader(RenderPipeline pipeline) {
        super(pipeline);
    }

    public abstract boolean shouldDraw(Entity entity);

    @Override
    protected void preDraw() {
        ((ILevelRenderer) mc.levelRenderer).peakeor$pushEntityOutlineFramebuffer(framebuffer);
    }

    @Override
    protected void postDraw() {
        ((ILevelRenderer) mc.levelRenderer).peakeor$popEntityOutlineFramebuffer();
    }

    public void submitVertices() {
    }
}
