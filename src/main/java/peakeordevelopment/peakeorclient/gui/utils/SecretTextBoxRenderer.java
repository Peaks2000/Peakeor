/*
 * This file is part of the Peakeor Client distribution.
 * Copyright (c) Peakeor contributors.
 */

package peakeordevelopment.peakeorclient.gui.utils;

import peakeordevelopment.peakeorclient.gui.renderer.GuiRenderer;
import peakeordevelopment.peakeorclient.gui.widgets.input.WTextBox;
import peakeordevelopment.peakeorclient.utils.render.color.Color;

public final class SecretTextBoxRenderer implements WTextBox.Renderer {
    @Override
    public void render(GuiRenderer renderer, double x, double y, String text, Color color) {
        renderer.text("•".repeat(text.length()), x, y, color, false);
    }
}
