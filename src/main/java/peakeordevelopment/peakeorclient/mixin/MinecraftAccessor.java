/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.server.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("fps")
    static int peakeor$getFps() {
        return 0;
    }

    @Mutable
    @Accessor("user")
    void peakeor$setUser(User session);

    @Accessor("reloadStateTracker")
    ResourceLoadStateTracker peakeor$getReloadStateTracker();

    @Accessor("missTime")
    int peakeor$getMissTime();

    @Accessor("missTime")
    void peakeor$setMissTime(int attackCooldown);

    @Invoker("startAttack")
    boolean peakeor$leftClick();

    @Mutable
    @Accessor("profileKeyPairManager")
    void peakeor$setProfileKeyPairManager(ProfileKeyPairManager keys);

    @Mutable
    @Accessor("userApiService")
    void peakeor$setUserApiService(UserApiService apiService);

    @Mutable
    @Accessor("skinManager")
    void peakeor$setSkinManager(SkinManager skinProvider);

    @Mutable
    @Accessor("playerSocialManager")
    void peakeor$setPlayerSocialManager(PlayerSocialManager socialInteractionsManager);

    @Mutable
    @Accessor("reportingContext")
    void peakeor$setReportingContext(ReportingContext abuseReportContext);

    @Mutable
    @Accessor("profileFuture")
    void peakeor$setProfileFuture(CompletableFuture<ProfileResult> future);

    @Mutable
    @Accessor("services")
    void peakeor$setServices(Services apiServices);

    @Invoker("handleKeybinds")
    void peakeor$handleInputEvents();
}
