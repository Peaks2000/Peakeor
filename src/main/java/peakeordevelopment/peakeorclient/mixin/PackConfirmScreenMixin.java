/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import peakeordevelopment.peakeorclient.utils.network.ResourcePackUtils;
import java.util.List;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl$PackConfirmScreen")
public abstract class PackConfirmScreenMixin extends ConfirmScreen {
    @Shadow @Final private List<?> requests;
    @Shadow @Final private Screen parentScreen;
    @Shadow @Final private ClientCommonPacketListenerImpl this$0;

    protected PackConfirmScreenMixin(BooleanConsumer callback, Component title, Component message) {
        super(callback, title, message);
    }

    @Override
    protected void addButtons(LinearLayout buttons) {
        super.addButtons(buttons);
        layout.addChild(Button.builder(Component.literal("Bypass"), button -> {
            button.active = false;
            requests.stream()
                .map(request -> ((ResourcePackPendingRequestAccessor) request).peakeor$getId())
                .distinct()
                .forEach(id -> ResourcePackUtils.bypass(id, this$0::send));
            minecraft.gui.setScreen(parentScreen);
        }).width(150).build());
    }
}
