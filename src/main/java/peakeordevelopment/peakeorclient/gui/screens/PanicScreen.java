/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui.screens;

import peakeordevelopment.peakeorclient.gui.GuiTheme;
import peakeordevelopment.peakeorclient.gui.WindowScreen;
import peakeordevelopment.peakeorclient.settings.KeybindSetting;
import peakeordevelopment.peakeorclient.settings.Settings;
import peakeordevelopment.peakeorclient.systems.config.Config;

public class PanicScreen extends WindowScreen {
    private final KeybindSetting keybind = Config.get().panicKeybind;

    public PanicScreen(GuiTheme theme) {
        super(theme, "Panic");
    }

    @Override
    public void initWidgets() {
        Settings settings = new Settings();
        settings.getDefaultGroup().add(keybind);
        add(theme.settings(settings)).expandX();
    }

    @Override
    protected void onClosed() {
        if (keybind.widget != null) {
            keybind.widget.reset();
            keybind.widget = null;
        }
    }
}
