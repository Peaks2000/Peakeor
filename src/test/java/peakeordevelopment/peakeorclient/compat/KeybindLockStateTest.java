/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.compat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;
import peakeordevelopment.peakeorclient.utils.misc.Keybind;

import static org.junit.jupiter.api.Assertions.*;

class KeybindLockStateTest {
    private static final int LOCKS = InputConstants.MOD_NUM_LOCK | InputConstants.MOD_CAPS_LOCK;

    @Test
    void equalsBindingIgnoresLockStatesDuringCaptureAndMatching() {
        Keybind bind = Keybind.none();
        assertTrue(bind.canBindTo(true, InputConstants.KEY_EQUALS, LOCKS));
        bind.set(true, InputConstants.KEY_EQUALS, LOCKS);

        assertEquals(InputConstants.KEY_EQUALS, bind.getValue());
        assertFalse(bind.hasMods());
        assertTrue(bind.matches(true, InputConstants.KEY_EQUALS, 0));
        assertTrue(bind.matches(true, InputConstants.KEY_EQUALS, LOCKS));
    }

    @Test
    void realHeldModifiersRemainRequiredRegardlessOfLockStates() {
        Keybind bind = Keybind.fromKeys(InputConstants.KEY_EQUALS, InputConstants.MOD_CONTROL | LOCKS);

        assertTrue(bind.hasMods());
        assertTrue(bind.matches(true, InputConstants.KEY_EQUALS, InputConstants.MOD_CONTROL));
        assertTrue(bind.matches(true, InputConstants.KEY_EQUALS, InputConstants.MOD_CONTROL | LOCKS));
        assertFalse(bind.matches(true, InputConstants.KEY_EQUALS, LOCKS));
        assertFalse(bind.matches(true, InputConstants.KEY_EQUALS, InputConstants.MOD_SHIFT | LOCKS));
    }

    @Test
    void savedAccidentalLockModifiersAreRemovedWhenLoaded() {
        CompoundTag tag = Keybind.fromKey(InputConstants.KEY_EQUALS).toTag();
        ListTag modifiers = new ListTag();
        modifiers.add(StringTag.valueOf("NUM_LOCK"));
        modifiers.add(StringTag.valueOf("CAPS_LOCK"));
        modifiers.add(StringTag.valueOf("SHIFT"));
        tag.put("modifiers", modifiers);

        Keybind bind = Keybind.none().fromTag(tag);
        assertTrue(bind.matches(true, InputConstants.KEY_EQUALS, InputConstants.MOD_SHIFT));
        assertFalse(bind.matches(true, InputConstants.KEY_EQUALS, 0));
        assertEquals(1, bind.toTag().getListOrEmpty("modifiers").size());
    }

    @Test
    void lockKeysCanStillBeUsedAsThePrimaryKey() {
        Keybind bind = Keybind.none();
        assertTrue(bind.canBindTo(true, InputConstants.KEY_NUMLOCK, LOCKS));
        bind.set(true, InputConstants.KEY_NUMLOCK, LOCKS);
        assertFalse(bind.hasMods());
        assertTrue(bind.matches(true, InputConstants.KEY_NUMLOCK, 0));
    }
}
