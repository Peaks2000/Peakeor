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
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

public class ParticleTypeListSettingScreen extends CollectionListSettingScreen<ParticleType<?>> {
    public ParticleTypeListSettingScreen(GuiTheme theme, Setting<List<ParticleType<?>>> setting) {
        super(theme, "Select Particles", setting, setting.get(), BuiltInRegistries.PARTICLE_TYPE);
    }

    @Override
    protected WWidget getValueWidget(ParticleType<?> value) {
        return theme.label(Names.get(value));
    }

    @Override
    protected String[] getValueNames(ParticleType<?> value) {
        return new String[]{
            Names.get(value),
            BuiltInRegistries.PARTICLE_TYPE.getKey(value).toString()
        };
    }
}
