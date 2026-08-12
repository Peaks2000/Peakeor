/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui.themes.peakeor.widgets;

import peakeordevelopment.peakeorclient.gui.renderer.GuiRenderer;
import peakeordevelopment.peakeorclient.gui.themes.peakeor.PeakeorWidget;
import peakeordevelopment.peakeorclient.gui.widgets.WWidget;
import peakeordevelopment.peakeorclient.gui.widgets.containers.WSection;
import peakeordevelopment.peakeorclient.gui.widgets.pressable.WTriangle;

public class WPeakeorSection extends WSection {
    public WPeakeorSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override
    protected WHeader createHeader() {
        return new WPeakeorHeader(title);
    }

    protected class WPeakeorHeader extends WHeader {
        private WTriangle triangle;

        public WPeakeorHeader(String title) {
            super(title);
        }

        @Override
        public void init() {
            add(theme.horizontalSeparator(title)).expandX();

            if (headerWidget != null) add(headerWidget);

            triangle = new WHeaderTriangle();
            triangle.theme = theme;
            triangle.action = this::onClick;

            add(triangle);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            triangle.rotation = (1 - animProgress) * -90;
        }
    }

    protected static class WHeaderTriangle extends WTriangle implements PeakeorWidget {
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.rotatedQuad(x, y, width, height, rotation, GuiRenderer.TRIANGLE, theme().textColor.get());
        }
    }
}
