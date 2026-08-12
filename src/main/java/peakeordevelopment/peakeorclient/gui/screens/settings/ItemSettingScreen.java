/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui.screens.settings;

import peakeordevelopment.peakeorclient.gui.GuiTheme;
import peakeordevelopment.peakeorclient.gui.WindowScreen;
import peakeordevelopment.peakeorclient.gui.widgets.WItemWithLabel;
import peakeordevelopment.peakeorclient.gui.widgets.containers.WTable;
import peakeordevelopment.peakeorclient.gui.widgets.input.WTextBox;
import peakeordevelopment.peakeorclient.gui.widgets.pressable.WButton;
import peakeordevelopment.peakeorclient.settings.ItemSetting;
import peakeordevelopment.peakeorclient.utils.misc.Names;
import peakeordevelopment.peakeorclient.utils.render.DisplayItemUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.Strings;

public class ItemSettingScreen extends WindowScreen {
    private final ItemSetting setting;

    private WTable table;

    private WTextBox filter;
    private String filterText = "";

    public ItemSettingScreen(GuiTheme theme, ItemSetting setting) {
        super(theme, "Select item");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();
        initTable();
    }

    public void initTable() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (setting.filter != null && !setting.filter.test(item)) continue;
            if (item == Items.AIR) continue;

            WItemWithLabel itemLabel = theme.itemWithLabel(DisplayItemUtils.toStack(item), Names.get(item));
            if (!filterText.isEmpty() && !Strings.CI.contains(itemLabel.getLabelText(), filterText)) continue;
            table.add(itemLabel);

            WButton select = table.add(theme.button("Select")).expandCellX().right().widget();
            select.action = () -> {
                setting.set(item);
                onClose();
            };

            table.row();
        }
    }
}
