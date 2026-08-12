/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import peakeordevelopment.peakeorclient.mixininterface.IServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerboundMovePlayerPacket.class)
public abstract class ServerboundMovePlayerPacketMixin implements IServerboundMovePlayerPacket {
    @Unique
    private int tag;

    @Override
    public void peakeor$setTag(int tag) {
        this.tag = tag;
    }

    @Override
    public int peakeor$getTag() {
        return this.tag;
    }
}
