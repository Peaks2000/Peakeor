/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.settings;

import peakeordevelopment.peakeorclient.gui.GuiTheme;
import peakeordevelopment.peakeorclient.gui.WidgetScreen;
import peakeordevelopment.peakeorclient.utils.misc.IChangeable;
import peakeordevelopment.peakeorclient.utils.misc.ICopyable;
import peakeordevelopment.peakeorclient.utils.misc.ISerializable;
import net.minecraft.world.level.block.Block;

public interface IBlockData<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> {
    WidgetScreen createScreen(GuiTheme theme, Block block, BlockDataSetting<T> setting);
}
