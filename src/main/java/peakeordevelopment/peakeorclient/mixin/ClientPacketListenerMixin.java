/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import peakeordevelopment.peakeorclient.PeakeorClient;
import peakeordevelopment.peakeorclient.commands.Commands;
import peakeordevelopment.peakeorclient.events.entity.EntityDestroyEvent;
import peakeordevelopment.peakeorclient.events.entity.player.PickItemsEvent;
import peakeordevelopment.peakeorclient.events.game.GameJoinedEvent;
import peakeordevelopment.peakeorclient.events.game.GameLeftEvent;
import peakeordevelopment.peakeorclient.events.game.SendMessageEvent;
import peakeordevelopment.peakeorclient.events.packets.ContainerSlotUpdateEvent;
import peakeordevelopment.peakeorclient.events.packets.InventoryEvent;
import peakeordevelopment.peakeorclient.events.packets.PlaySoundPacketEvent;
import peakeordevelopment.peakeorclient.events.world.ChunkDataEvent;
import peakeordevelopment.peakeorclient.mixininterface.IClientboundExplodePacket;
import peakeordevelopment.peakeorclient.pathing.BaritoneUtils;
import peakeordevelopment.peakeorclient.systems.config.Config;
import peakeordevelopment.peakeorclient.systems.modules.Modules;
import peakeordevelopment.peakeorclient.systems.modules.movement.Velocity;
import peakeordevelopment.peakeorclient.systems.modules.player.NoRotate;
import peakeordevelopment.peakeorclient.systems.modules.render.NoRender;
import peakeordevelopment.peakeorclient.utils.player.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    @Shadow
    private ClientLevel level;

    protected ClientPacketListenerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "handleAddEntity", at = @At("HEAD"), cancellable = true)
    private void onHandleAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        if (packet != null && packet.getType() != null) {
            if (Modules.get().get(NoRender.class).noEntity(packet.getType()) && Modules.get().get(NoRender.class).getDropSpawnPacket()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void onHandleLoginHead(ClientboundLoginPacket packet, CallbackInfo ci, @Share("worldNotNull") LocalBooleanRef worldNotNull) {
        worldNotNull.set(level != null);
    }

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void onHandleLoginTail(ClientboundLoginPacket packet, CallbackInfo ci, @Share("worldNotNull") LocalBooleanRef worldNotNull) {
        if (worldNotNull.get()) {
            PeakeorClient.EVENT_BUS.post(GameLeftEvent.get());
        }

        PeakeorClient.EVENT_BUS.post(GameJoinedEvent.get());
    }

    // the server sends a GameJoin packet after the reconfiguration phase
    @Inject(method = "handleConfigurationStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER))
    private void onHandleConfigurationStart(ClientboundStartConfigurationPacket packet, CallbackInfo ci) {
        PeakeorClient.EVENT_BUS.post(GameLeftEvent.get());
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    private void onHandleSoundEvent(ClientboundSoundPacket packet, CallbackInfo ci) {
        PeakeorClient.EVENT_BUS.post(PlaySoundPacketEvent.get(packet));
    }

    @Inject(method = "handleLevelChunkWithLight", at = @At("TAIL"))
    private void onHandleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        LevelChunk chunk = minecraft.level.getChunk(packet.getX(), packet.getZ());
        PeakeorClient.EVENT_BUS.post(new ChunkDataEvent(chunk));
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void onHandleContainerSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        PeakeorClient.EVENT_BUS.post(ContainerSlotUpdateEvent.get(packet));
    }

    @Inject(method = "handleContainerContent", at = @At("TAIL"))
    private void onHandleContainerContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        PeakeorClient.EVENT_BUS.post(InventoryEvent.get(packet));
    }

    @Inject(method = "handleRemoveEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundRemoveEntitiesPacket;getEntityIds()Lit/unimi/dsi/fastutil/ints/IntList;"))
    private void onHandleRemoveEntities(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {
        for (int id : packet.getEntityIds()) {
            PeakeorClient.EVENT_BUS.post(EntityDestroyEvent.get(minecraft.level.getEntity(id)));
        }
    }

    @Inject(method = "handleExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER))
    private void onHandleExplosionVelocity(ClientboundExplodePacket packet, CallbackInfo ci) {
        Velocity velocity = Modules.get().get(Velocity.class);
        if (!velocity.shouldModifyExplosions()) return;

        IClientboundExplodePacket explosionPacket = (IClientboundExplodePacket) (Object) packet;
        explosionPacket.peakeor$setVelocityX((float) (packet.playerKnockback().orElse(Vec3.ZERO).x * velocity.getHorizontal(velocity.explosionsHorizontal)));
        explosionPacket.peakeor$setVelocityY((float) (packet.playerKnockback().orElse(Vec3.ZERO).y * velocity.getVertical(velocity.explosionsVertical)));
        explosionPacket.peakeor$setVelocityZ((float) (packet.playerKnockback().orElse(Vec3.ZERO).z * velocity.getHorizontal(velocity.explosionsHorizontal)));
    }

    @Inject(method = "handleTakeItemEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getEntity(I)Lnet/minecraft/world/entity/Entity;", ordinal = 0))
    private void onHandleTakeItemEntity(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        Entity itemEntity = minecraft.level.getEntity(packet.getItemId());
        Entity entity = minecraft.level.getEntity(packet.getPlayerId());

        if (itemEntity instanceof ItemEntity item && entity == minecraft.player) {
            PeakeorClient.EVENT_BUS.post(PickItemsEvent.get(item.getItem(), packet.getAmount()));
        }
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void onHandleMovePlayerHead(ClientboundPlayerPositionPacket packet, CallbackInfo ci, @Share("noRotateYaw") LocalFloatRef yawRef, @Share("noRotatePitch") LocalFloatRef pitchRef) {
        NoRotate noRotate = Modules.get().get(NoRotate.class);
        if (!noRotate.isActive() || minecraft.player == null) return;

        yawRef.set(minecraft.player.getYRot());
        pitchRef.set(minecraft.player.getXRot());
    }

    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    private void onHandleMovePlayerReturn(ClientboundPlayerPositionPacket packet, CallbackInfo ci, @Share("noRotateYaw") LocalFloatRef yawRef, @Share("noRotatePitch") LocalFloatRef pitchRef) {
        NoRotate noRotate = Modules.get().get(NoRotate.class);
        if (!noRotate.isActive() || minecraft.player == null) return;

        float savedYaw = yawRef.get();
        float savedPitch = pitchRef.get();

        //not noticeable by player but forces a server update
        minecraft.player.setYRot(savedYaw + 0.000001f);
        minecraft.player.setXRot(savedPitch + 0.000001f);
        minecraft.player.yHeadRot = savedYaw;
        minecraft.player.yBodyRot = savedYaw;
    }

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci, @Local(argsOnly = true, name = "content") LocalRef<String> messageRef) {
        if (!message.startsWith(Config.get().prefix.get()) && !(BaritoneUtils.IS_AVAILABLE && message.startsWith(BaritoneUtils.getPrefix()))) {
            SendMessageEvent event = PeakeorClient.EVENT_BUS.post(SendMessageEvent.get(message));

            if (!event.isCancelled()) {
                messageRef.set(event.message);
            } else {
                ci.cancel();
            }

            return;
        }

        if (message.startsWith(Config.get().prefix.get())) {
            try {
                Commands.dispatch(message.substring(Config.get().prefix.get().length()));
            } catch (CommandSyntaxException e) {
                ChatUtils.error(e.getMessage());
            }

            minecraft.gui.hud.getChat().addRecentChat(message);
            ci.cancel();
        }
    }
}
