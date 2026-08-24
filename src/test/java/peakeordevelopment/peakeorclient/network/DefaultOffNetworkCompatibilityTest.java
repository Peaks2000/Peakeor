/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.network;

import org.junit.jupiter.api.Test;
import peakeordevelopment.peakeorclient.systems.modules.movement.Velocity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultOffNetworkCompatibilityTest {
    @Test
    void disabledVelocityNeverMutatesExplosionPackets() {
        assertFalse(Velocity.shouldModifyExplosions(false, true));
        assertFalse(Velocity.shouldModifyExplosions(false, false));
    }

    @Test
    void disabledExplosionFeatureNeverMutatesExplosionPackets() {
        assertFalse(Velocity.shouldModifyExplosions(true, false));
    }

    @Test
    void activeExplosionFeatureCanApplyItsExplicitPacketMutation() {
        assertTrue(Velocity.shouldModifyExplosions(true, true));
    }
}
