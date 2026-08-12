/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixininterface;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface ILevelRenderer {
    void peakeor$pushEntityOutlineFramebuffer(RenderTarget framebuffer);

    void peakeor$popEntityOutlineFramebuffer();
}
