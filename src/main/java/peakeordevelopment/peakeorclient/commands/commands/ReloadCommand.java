/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import peakeordevelopment.peakeorclient.commands.Command;
import peakeordevelopment.peakeorclient.renderer.Fonts;
import peakeordevelopment.peakeorclient.systems.Systems;
import peakeordevelopment.peakeorclient.systems.friends.Friend;
import peakeordevelopment.peakeorclient.systems.friends.Friends;
import peakeordevelopment.peakeorclient.utils.network.Capes;
import peakeordevelopment.peakeorclient.utils.network.PeakeorExecutor;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "Reloads many systems.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(_ -> {
            warning("Reloading systems, this may take a while.");

            Systems.load();
            Capes.init();
            Fonts.refresh();
            PeakeorExecutor.execute(() -> Friends.get().forEach(Friend::updateInfo));

            return SINGLE_SUCCESS;
        });
    }
}
