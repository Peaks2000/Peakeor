/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import peakeordevelopment.peakeorclient.commands.Command;
import peakeordevelopment.peakeorclient.commands.arguments.ModuleArgumentType;
import peakeordevelopment.peakeorclient.gui.GuiThemes;
import peakeordevelopment.peakeorclient.settings.Setting;
import peakeordevelopment.peakeorclient.systems.hud.Hud;
import peakeordevelopment.peakeorclient.systems.modules.Module;
import peakeordevelopment.peakeorclient.systems.modules.Modules;
import peakeordevelopment.peakeorclient.utils.player.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ResetCommand extends Command {

    public ResetCommand() {
        super("reset", "Resets specified settings.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(literal("settings")
            .then(argument("module", ModuleArgumentType.create()).executes(context -> {
                Module module = context.getArgument("module", Module.class);
                module.settings.forEach(group -> group.forEach(Setting::reset));
                module.info("Reset all settings.");
                return SINGLE_SUCCESS;
            }))
            .then(literal("all").executes(_ -> {
                Modules.get().getAll().forEach(module -> module.settings.forEach(group -> group.forEach(Setting::reset)));
                ChatUtils.infoPrefix("Modules", "Reset all module settings");
                return SINGLE_SUCCESS;
            }))
        ).then(literal("gui").executes(_ -> {
            GuiThemes.get().clearWindowConfigs();
            GuiThemes.get().settings.reset();
            ChatUtils.info("Reset all GUI settings.");
            return SINGLE_SUCCESS;
        })).then(literal("bind")
            .then(argument("module", ModuleArgumentType.create()).executes(context -> {
                Module module = context.getArgument("module", Module.class);

                module.keybind.reset();
                module.info("Reset bind.");

                return SINGLE_SUCCESS;
            }))
            .then(literal("all").executes(_ -> {
                Modules.get().getAll().forEach(module -> module.keybind.reset());
                ChatUtils.infoPrefix("Modules", "Reset all binds.");
                return SINGLE_SUCCESS;
            }))
        ).then(literal("hud").executes(_ -> {
            Hud.get().resetToDefaultElements();
            ChatUtils.infoPrefix("HUD", "Reset all elements.");
            return SINGLE_SUCCESS;
        }));
    }
}
