/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixininterface;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

@SuppressWarnings("UnusedReturnValue")
public interface IVec3 {
    Vec3 peakeor$set(double x, double y, double z);

    default Vec3 peakeor$set(Vec3i vec) {
        return peakeor$set(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3 peakeor$set(Vector3d vec) {
        return peakeor$set(vec.x, vec.y, vec.z);
    }

    default Vec3 peakeor$set(Vec3 pos) {
        return peakeor$set(pos.x, pos.y, pos.z);
    }

    Vec3 peakeor$setXZ(double x, double z);

    Vec3 peakeor$setY(double y);
}
