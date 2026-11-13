/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.network;

import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import org.junit.jupiter.api.Test;
import peakeordevelopment.peakeorclient.utils.network.ResourcePackUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResourcePackBypassTest {
    @Test
    void acknowledgesOnlyTheChosenPackInOrder() {
        UUID id = UUID.randomUUID();
        List<ServerboundResourcePackPacket> packets = new ArrayList<>();
        ResourcePackUtils.bypass(id, packets::add);
        assertEquals(List.of(ServerboundResourcePackPacket.Action.ACCEPTED,
            ServerboundResourcePackPacket.Action.DOWNLOADED,
            ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED), packets.stream().map(ServerboundResourcePackPacket::action).toList());
        assertTrue(packets.stream().allMatch(packet -> packet.id().equals(id)));
    }

    @Test
    void multiplePacksKeepTheirOwnIds() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        List<ServerboundResourcePackPacket> packets = new ArrayList<>();
        ResourcePackUtils.bypass(first, packets::add);
        ResourcePackUtils.bypass(second, packets::add);
        assertEquals(List.of(first, first, first, second, second, second), packets.stream().map(ServerboundResourcePackPacket::id).toList());
    }
}
