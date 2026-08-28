/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.systems.modules.misc;

import peakeordevelopment.peakeorclient.events.entity.EntityAddedEvent;
import peakeordevelopment.peakeorclient.settings.BoolSetting;
import peakeordevelopment.peakeorclient.settings.Setting;
import peakeordevelopment.peakeorclient.settings.SettingGroup;
import peakeordevelopment.peakeorclient.settings.StringSetting;
import peakeordevelopment.peakeorclient.systems.friends.Friends;
import peakeordevelopment.peakeorclient.systems.modules.Categories;
import peakeordevelopment.peakeorclient.systems.modules.Module;
import peakeordevelopment.peakeorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.player.Player;

public class MessageAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> message = sgGeneral.add(new StringSetting.Builder()
        .name("message")
        .description("The specified message sent to the player.")
        .defaultValue("Peakeor is amazing!")
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Will not send any messages to people friended.")
        .defaultValue(false)
        .build()
    );

    public MessageAura() {
        super(Categories.Misc, "message-aura", "Sends a specified message to any player that enters render distance.");
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof Player) || event.entity.getUUID().equals(mc.player.getUUID())) return;

        if (!ignoreFriends.get() || (ignoreFriends.get() && !Friends.get().isFriend((Player) event.entity))) {
            ChatUtils.sendPlayerMsg("/msg " + event.entity.getName().getString() + " " + message.get());
        }
    }
}
