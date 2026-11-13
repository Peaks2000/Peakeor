/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.compat;

import org.junit.jupiter.api.Test;
import peakeordevelopment.peakeorclient.utils.misc.Version;
import static org.junit.jupiter.api.Assertions.*;

class HotfixVersionTest {
    @Test
    void hotfixVersionKeepsItsFullDisplayAndOrdering() {
        Version hotfix = new Version("5.1.5.1");
        assertEquals("5.1.5.1", hotfix.toString());
        assertTrue(hotfix.isHigherThan(new Version("5.1.5")));
        assertFalse(new Version("5.1.5").isHigherThan(hotfix));
        assertTrue(new Version("5.1.6").isHigherThan(hotfix));
    }

    @Test
    void threePartVersionsKeepTheirOrderingAndZeroPadding() {
        assertFalse(new Version("5.1.5.0").isHigherThan(new Version("5.1.5")));
        assertFalse(new Version("5.1.5").isHigherThan(new Version("5.1.5.0")));
        assertTrue(new Version("0.0.0").isZero());
        assertTrue(new Version("0.0.0.0").isZero());
        assertFalse(new Version("0.0.0.1").isZero());
    }
}
