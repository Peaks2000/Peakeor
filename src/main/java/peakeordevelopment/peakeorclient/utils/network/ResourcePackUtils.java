/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.utils.network;

import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import java.util.UUID;
import java.util.function.Consumer;

public final class ResourcePackUtils {
    private ResourcePackUtils() {
    }

    public static void bypass(UUID id, Consumer<ServerboundResourcePackPacket> sender) {
        sender.accept(new ServerboundResourcePackPacket(id, ServerboundResourcePackPacket.Action.ACCEPTED));
        sender.accept(new ServerboundResourcePackPacket(id, ServerboundResourcePackPacket.Action.DOWNLOADED));
        sender.accept(new ServerboundResourcePackPacket(id, ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
    }
}
