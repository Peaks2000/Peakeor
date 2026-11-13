/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.compat;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.junit.jupiter.api.Test;
import peakeordevelopment.peakeorclient.events.peakeor.MouseClickEvent;
import peakeordevelopment.peakeorclient.gui.widgets.WKeybind;
import peakeordevelopment.peakeorclient.settings.KeybindSetting;
import peakeordevelopment.peakeorclient.settings.Setting;
import peakeordevelopment.peakeorclient.utils.misc.Keybind;
import peakeordevelopment.peakeorclient.utils.misc.input.KeyAction;
import sun.misc.Unsafe;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class KeybindCaptureTest {
    @Test
    void mouseCaptureDoesNotActivateUntilTheNextRelease() throws Exception {
        var unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        KeybindSetting setting = (KeybindSetting) unsafe.allocateInstance(KeybindSetting.class);
        Keybind bind = Keybind.none();
        var value = Setting.class.getDeclaredField("value");
        value.setAccessible(true);
        value.set(setting, bind);
        AtomicInteger actions = new AtomicInteger();
        var action = KeybindSetting.class.getDeclaredField("action");
        action.setAccessible(true);
        action.set(setting, (Runnable) actions::incrementAndGet);
        var pending = KeybindSetting.class.getDeclaredField("capturedMouseButton");
        pending.setAccessible(true);
        pending.setInt(setting, -1);
        setting.widget = new WKeybind(bind, Keybind.none()) {
            @Override
            public boolean onAction(boolean isKey, int button, int modifiers) {
                bind.set(isKey, button, modifiers);
                return true;
            }
        };
        var capture = KeybindSetting.class.getDeclaredMethod("onMouseClickBinding", MouseClickEvent.class);
        var activate = KeybindSetting.class.getDeclaredMethod("onMouseClick", MouseClickEvent.class);
        capture.setAccessible(true);
        activate.setAccessible(true);
        MouseButtonEvent mouse = new MouseButtonEvent(0, 0, new MouseButtonInfo(4, 0));
        capture.invoke(setting, MouseClickEvent.get(mouse, KeyAction.Press));
        activate.invoke(setting, MouseClickEvent.get(mouse, KeyAction.Release));
        assertEquals(0, actions.get());
        setting.widget = null;
        activate.invoke(setting, MouseClickEvent.get(mouse, KeyAction.Press));
        assertEquals(0, actions.get());
        activate.invoke(setting, MouseClickEvent.get(mouse, KeyAction.Release));
        assertEquals(1, actions.get());
        assertTrue(Keybind.none().fromTag(bind.toTag()).matches(false, 4, 0));
    }
}
