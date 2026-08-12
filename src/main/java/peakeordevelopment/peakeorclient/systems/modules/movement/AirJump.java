/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.systems.modules.movement;

import peakeordevelopment.peakeorclient.events.peakeor.KeyInputEvent;
import peakeordevelopment.peakeorclient.events.world.TickEvent;
import peakeordevelopment.peakeorclient.settings.BoolSetting;
import peakeordevelopment.peakeorclient.settings.Setting;
import peakeordevelopment.peakeorclient.settings.SettingGroup;
import peakeordevelopment.peakeorclient.systems.modules.Categories;
import peakeordevelopment.peakeorclient.systems.modules.Module;
import peakeordevelopment.peakeorclient.systems.modules.Modules;
import peakeordevelopment.peakeorclient.systems.modules.render.Freecam;
import peakeordevelopment.peakeorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;

public class AirJump extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> maintainLevel = sgGeneral.add(new BoolSetting.Builder()
        .name("maintain-level")
        .description("Maintains your current Y level when holding the jump key.")
        .defaultValue(false)
        .build()
    );

    private int level;

    public AirJump() {
        super(Categories.Movement, "air-jump", "Lets you jump in the air.");
    }

    @Override
    public void onActivate() {
        level = mc.player.blockPosition().getY();
    }

    @EventHandler
    private void onKey(KeyInputEvent event) {
        if (Modules.get().isActive(Freecam.class) || mc.gui.screen() != null || mc.player.onGround()) return;

        if (event.action != KeyAction.Press) return;

        if (mc.options.keyJump.matches(event.input)) {
            level = mc.player.blockPosition().getY();
            mc.player.jumpFromGround();
        } else if (mc.options.keyShift.matches(event.input)) {
            level--;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (Modules.get().isActive(Freecam.class) || mc.player.onGround()) return;

        if (maintainLevel.get() && mc.player.blockPosition().getY() == level && mc.options.keyJump.isDown()) {
            mc.player.jumpFromGround();
        }
    }
}
