/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui.themes.peakeor.widgets.pressable;

import peakeordevelopment.peakeorclient.gui.renderer.GuiRenderer;
import peakeordevelopment.peakeorclient.gui.renderer.packer.GuiTexture;
import peakeordevelopment.peakeorclient.gui.themes.peakeor.PeakeorGuiTheme;
import peakeordevelopment.peakeorclient.gui.themes.peakeor.PeakeorWidget;
import peakeordevelopment.peakeorclient.gui.widgets.pressable.WButton;

public class WPeakeorButton extends WButton implements PeakeorWidget {
    public WPeakeorButton(String text, GuiTexture texture) {
        super(text, texture);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        PeakeorGuiTheme theme = theme();
        double pad = pad();

        renderBackground(renderer, this, pressed, mouseOver);

        if (text != null) {
            renderer.text(text, x + width / 2 - textWidth / 2, y + pad, theme.textColor.get(), false);
        }
        else {
            double ts = theme.textHeight();
            renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, texture, theme.textColor.get());
        }
    }
}
