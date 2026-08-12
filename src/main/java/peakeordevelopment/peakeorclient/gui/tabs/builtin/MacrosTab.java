/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui.tabs.builtin;

import peakeordevelopment.peakeorclient.gui.GuiTheme;
import peakeordevelopment.peakeorclient.gui.renderer.GuiRenderer;
import peakeordevelopment.peakeorclient.gui.screens.EditSystemScreen;
import peakeordevelopment.peakeorclient.gui.tabs.Tab;
import peakeordevelopment.peakeorclient.gui.tabs.TabScreen;
import peakeordevelopment.peakeorclient.gui.tabs.WindowTabScreen;
import peakeordevelopment.peakeorclient.gui.widgets.containers.WTable;
import peakeordevelopment.peakeorclient.gui.widgets.pressable.WButton;
import peakeordevelopment.peakeorclient.gui.widgets.pressable.WConfirmedMinus;
import peakeordevelopment.peakeorclient.settings.Settings;
import peakeordevelopment.peakeorclient.systems.macros.Macro;
import peakeordevelopment.peakeorclient.systems.macros.Macros;
import peakeordevelopment.peakeorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screens.Screen;

import static peakeordevelopment.peakeorclient.PeakeorClient.mc;

public class MacrosTab extends Tab {
    public MacrosTab() {
        super("Macros");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new MacrosScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof MacrosScreen;
    }

    private static class MacrosScreen extends WindowTabScreen {
        public MacrosScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            WTable table = add(theme.table()).expandX().minWidth(400).widget();
            initTable(table);

            add(theme.horizontalSeparator()).expandX();

            WButton create = add(theme.button("Create")).expandX().widget();
            create.action = () -> mc.gui.setScreen(new EditMacroScreen(theme, null, this::reload));
        }

        private void initTable(WTable table) {
            table.clear();
            if (Macros.get().isEmpty()) return;

            for (Macro macro : Macros.get()) {
                table.add(theme.label(macro.name.get() + " (" + macro.keybind.get() + ")"));

                WButton edit = table.add(theme.button(GuiRenderer.EDIT)).expandCellX().right().widget();
                edit.action = () -> mc.gui.setScreen(new EditMacroScreen(theme, macro, this::reload));

                WConfirmedMinus remove = table.add(theme.confirmedMinus()).widget();
                remove.action = () -> {
                    Macros.get().remove(macro);
                    reload();
                };

                table.row();
            }
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Macros.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Macros.get());
        }
    }

    private static class EditMacroScreen extends EditSystemScreen<Macro> {
        public EditMacroScreen(GuiTheme theme, Macro value, Runnable reload) {
            super(theme, value, reload);
        }

        @Override
        public Macro create() {
            return new Macro();
        }

        @Override
        public boolean save() {
            if (value.name.get().isBlank()
                || value.messages.get().isEmpty()
            ) return false;

            if (isNew) {
                for (Macro m : Macros.get()) {
                    if (value.equals(m)) return false;
                }
            }

            if (isNew) Macros.get().add(value);
            else Macros.get().save();

            return true;
        }

        @Override
        public Settings getSettings() {
            return value.settings;
        }
    }
}
