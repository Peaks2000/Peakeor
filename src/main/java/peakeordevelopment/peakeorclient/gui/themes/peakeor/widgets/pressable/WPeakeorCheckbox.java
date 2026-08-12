/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui.themes.peakeor.widgets.pressable;

import peakeordevelopment.peakeorclient.gui.renderer.GuiRenderer;
import peakeordevelopment.peakeorclient.gui.themes.peakeor.PeakeorGuiTheme;
import peakeordevelopment.peakeorclient.gui.themes.peakeor.PeakeorWidget;
import peakeordevelopment.peakeorclient.gui.widgets.pressable.WCheckbox;
import net.minecraft.util.Mth;

public class WPeakeorCheckbox extends WCheckbox implements PeakeorWidget {
    private double animProgress;

    public WPeakeorCheckbox(boolean checked) {
        super(checked);
        animProgress = checked ? 1 : 0;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        PeakeorGuiTheme theme = theme();

        animProgress += (checked ? 1 : -1) * delta * 14;
        animProgress = Mth.clamp(animProgress, 0, 1);

        renderBackground(renderer, this, pressed, mouseOver);

        if (animProgress > 0) {
            double cs = (width - theme.scale(2)) / 1.75 * animProgress;
            renderer.quad(x + (width - cs) / 2, y + (height - cs) / 2, cs, cs, theme.checkboxColor.get());
        }
    }
}
