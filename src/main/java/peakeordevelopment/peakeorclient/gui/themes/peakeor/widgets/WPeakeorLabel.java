/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui.themes.peakeor.widgets;

import peakeordevelopment.peakeorclient.gui.renderer.GuiRenderer;
import peakeordevelopment.peakeorclient.gui.themes.peakeor.PeakeorWidget;
import peakeordevelopment.peakeorclient.gui.widgets.WLabel;

public class WPeakeorLabel extends WLabel implements PeakeorWidget {
    public WPeakeorLabel(String text, boolean title) {
        super(text, title);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!text.isEmpty()) {
            renderer.text(text, x, y, color != null ? color : (title ? theme().titleTextColor.get() : theme().textColor.get()), title);
        }
    }
}
