/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.junit.jupiter.api.Test;
import peakeordevelopment.peakeorclient.gui.widgets.pressable.WPressable;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.blaze3d.platform.InputConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class PanicButtonTest {
    @Test
    void rightClickConfiguresAndLeftClickRunsTheAction() {
        WPressable button = new WPressable() {};
        button.mouseOver = true;
        AtomicInteger actions = new AtomicInteger();
        AtomicInteger settings = new AtomicInteger();
        button.action = actions::incrementAndGet;
        button.rightClickAction = settings::incrementAndGet;
        click(button, MOUSE_BUTTON_RIGHT);
        assertEquals(0, actions.get());
        assertEquals(1, settings.get());
        click(button, MOUSE_BUTTON_LEFT);
        assertEquals(1, actions.get());
        assertEquals(1, settings.get());
    }

    @Test
    void existingButtonsKeepTheirRightClickAction() {
        WPressable button = new WPressable() {};
        button.mouseOver = true;
        AtomicInteger actions = new AtomicInteger();
        button.action = actions::incrementAndGet;
        click(button, MOUSE_BUTTON_RIGHT);
        assertEquals(1, actions.get());
    }

    private void click(WPressable button, int mouseButton) {
        MouseButtonEvent event = new MouseButtonEvent(0, 0, new MouseButtonInfo(mouseButton, 0));
        assertTrue(button.onMouseClicked(event, false));
        button.onMouseReleased(event);
    }
}
