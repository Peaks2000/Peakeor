/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixininterface;

import net.minecraft.core.BlockPos;

public interface IAABB {
    void peakeor$expand(double v);

    void peakeor$set(double x1, double y1, double z1, double x2, double y2, double z2);

    default void peakeor$set(BlockPos pos) {
        peakeor$set(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
}
