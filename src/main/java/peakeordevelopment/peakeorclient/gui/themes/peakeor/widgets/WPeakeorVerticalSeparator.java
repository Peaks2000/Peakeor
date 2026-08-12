/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui.themes.peakeor.widgets;

import peakeordevelopment.peakeorclient.gui.renderer.GuiRenderer;
import peakeordevelopment.peakeorclient.gui.themes.peakeor.PeakeorGuiTheme;
import peakeordevelopment.peakeorclient.gui.themes.peakeor.PeakeorWidget;
import peakeordevelopment.peakeorclient.gui.widgets.WVerticalSeparator;
import peakeordevelopment.peakeorclient.utils.render.color.Color;

public class WPeakeorVerticalSeparator extends WVerticalSeparator implements PeakeorWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        PeakeorGuiTheme theme = theme();
        Color colorEdges = theme.separatorEdges.get();
        Color colorCenter = theme.separatorCenter.get();

        double s = theme.scale(1);
        double offsetX = Math.round(width / 2.0);

        renderer.quad(x + offsetX, y, s, height / 2, colorEdges, colorEdges, colorCenter, colorCenter);
        renderer.quad(x + offsetX, y + height / 2, s, height / 2, colorCenter, colorCenter, colorEdges, colorEdges);
    }
}
