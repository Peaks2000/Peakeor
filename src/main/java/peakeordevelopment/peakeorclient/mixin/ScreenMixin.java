/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import peakeordevelopment.peakeorclient.PeakeorClient;
import peakeordevelopment.peakeorclient.commands.Commands;
import peakeordevelopment.peakeorclient.systems.config.Config;
import peakeordevelopment.peakeorclient.systems.modules.Modules;
import peakeordevelopment.peakeorclient.systems.modules.movement.GUIMove;
import peakeordevelopment.peakeorclient.systems.modules.render.NoRender;
import peakeordevelopment.peakeorclient.utils.Utils;
import peakeordevelopment.peakeorclient.utils.misc.text.PeakeorClickEvent;
import peakeordevelopment.peakeorclient.utils.misc.text.RunnableClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.glfw.GLFW.*;

@Mixin(value = Screen.class, priority = 500) // needs to be before baritone
public abstract class ScreenMixin {

    @Unique
    private static boolean peakeor$isArray(int key) {
        return key == GLFW_KEY_RIGHT || key == GLFW_KEY_LEFT || key == GLFW_KEY_DOWN || key == GLFW_KEY_UP;
    }

    @Inject(method = "extractTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void onExtractTransparentBackground(CallbackInfo ci) {
        if (Utils.canUpdate() && Modules.get().get(NoRender.class).noGuiBackground())
            ci.cancel();
    }

    @Inject(method = "defaultHandleClickEvent", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", remap = false), cancellable = true)
    private static void onDefaultHandleClickEvent(ClickEvent event, Minecraft minecraft, Screen activeScreen, CallbackInfo ci) {
        if (event instanceof RunnableClickEvent runnableClickEvent) {
            runnableClickEvent.runnable.run();
            ci.cancel();
        } else if (event instanceof PeakeorClickEvent peakeorClickEvent && peakeorClickEvent.value.startsWith(Config.get().prefix.get())) {
            try {
                Commands.dispatch(peakeorClickEvent.value.substring(Config.get().prefix.get().length()));
            } catch (CommandSyntaxException e) {
                PeakeorClient.LOG.error("Failed to run command", e);
            } finally {
                ci.cancel();
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) (this) instanceof ChatScreen) return;
        GUIMove guiMove = Modules.get().get(GUIMove.class);
        if ((guiMove.disableArrows() && peakeor$isArray(event.key())) || (guiMove.disableSpace() && event.key() == GLFW_KEY_SPACE)) {
            cir.setReturnValue(true);
        }
    }
}
