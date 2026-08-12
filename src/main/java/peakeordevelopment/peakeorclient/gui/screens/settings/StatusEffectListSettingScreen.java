/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui.screens.settings;

import peakeordevelopment.peakeorclient.gui.GuiTheme;
import peakeordevelopment.peakeorclient.gui.screens.settings.base.CollectionListSettingScreen;
import peakeordevelopment.peakeorclient.gui.widgets.WWidget;
import peakeordevelopment.peakeorclient.settings.Setting;
import peakeordevelopment.peakeorclient.utils.misc.Names;
import peakeordevelopment.peakeorclient.utils.render.DisplayItemUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.List;
import java.util.Optional;

public class StatusEffectListSettingScreen extends CollectionListSettingScreen<MobEffect> {
    public StatusEffectListSettingScreen(GuiTheme theme, Setting<List<MobEffect>> setting) {
        super(theme, "Select Effects", setting, setting.get(), BuiltInRegistries.MOB_EFFECT);
    }

    @Override
    protected WWidget getValueWidget(MobEffect value) {
        return theme.itemWithLabel(getPotionStack(value), Names.get(value));
    }

    @Override
    protected String[] getValueNames(MobEffect value) {
        return new String[]{
            Names.get(value),
            BuiltInRegistries.MOB_EFFECT.getKey(value).toString()
        };
    }

    private ItemStack getPotionStack(MobEffect effect) {
        ItemStack potion = DisplayItemUtils.toStack(Items.POTION);

        potion.set(
            DataComponents.POTION_CONTENTS,
            new PotionContents(
                Optional.empty(),
                Optional.of(effect.getColor()),
                List.of(),
                Optional.empty()
            )
        );

        return potion;
    }
}
