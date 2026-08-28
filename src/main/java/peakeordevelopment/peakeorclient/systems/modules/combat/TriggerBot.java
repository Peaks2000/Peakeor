/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.systems.modules.combat;

import peakeordevelopment.peakeorclient.events.world.TickEvent;
import peakeordevelopment.peakeorclient.settings.*;
import peakeordevelopment.peakeorclient.systems.friends.Friends;
import peakeordevelopment.peakeorclient.systems.modules.Categories;
import peakeordevelopment.peakeorclient.systems.modules.Module;
import peakeordevelopment.peakeorclient.utils.Utils;
import peakeordevelopment.peakeorclient.utils.entity.EntityUtils;
import peakeordevelopment.peakeorclient.utils.entity.Target;
import peakeordevelopment.peakeorclient.utils.player.PlayerUtils;
import peakeordevelopment.peakeorclient.utils.player.Rotations;
import peakeordevelopment.peakeorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Set;

public class TriggerBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");

    // General

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards the entity in your crosshair.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> aimRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("aim-radius")
        .description("Random rotation offset in degrees applied while aiming to look more human.")
        .defaultValue(1.5)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    private final Setting<Boolean> attackCooldown = sgGeneral.add(new BoolSetting.Builder()
        .name("attack-cooldown")
        .description("Waits for the Minecraft attack cooldown of the weapon you are holding before attacking.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> randomizeDelay = sgGeneral.add(new BoolSetting.Builder()
        .name("randomize-delay")
        .description("Adds a small random delay on top of the cooldown so attacks don't look robotic.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> delayMin = sgGeneral.add(new IntSetting.Builder()
        .name("delay-min")
        .description("The minimum random delay between attacks in ticks.")
        .defaultValue(1)
        .range(0, 20)
        .sliderRange(0, 20)
        .visible(randomizeDelay::get)
        .build()
    );

    private final Setting<Integer> delayMax = sgGeneral.add(new IntSetting.Builder()
        .name("delay-max")
        .description("The maximum random delay between attacks in ticks.")
        .defaultValue(4)
        .range(0, 20)
        .sliderRange(0, 20)
        .visible(randomizeDelay::get)
        .build()
    );

    private final Setting<Boolean> onlyOnClick = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-click")
        .description("Only attacks when you are holding left click.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> pauseOnLag = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-lag")
        .description("Pauses if the server is lagging.")
        .defaultValue(true)
        .build()
    );

    // Targeting

    private final Setting<Set<EntityType<?>>> entities = sgTargeting.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to attack when they are in your crosshair.")
        .onlyAttackable()
        .defaultValue(EntityTypes.PLAYER)
        .build()
    );

    private final Setting<Double> range = sgTargeting.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range the entity can be to attack it.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Double> wallsRange = sgTargeting.add(new DoubleSetting.Builder()
        .name("walls-range")
        .description("The maximum range the entity can be attacked through walls.")
        .defaultValue(3.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Boolean> ignoreNamed = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-named")
        .description("Whether or not to attack mobs with a name.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignoreTamed = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-tamed")
        .description("Will avoid attacking mobs you tamed.")
        .defaultValue(false)
        .build()
    );

    private int attackTimer;
    private Entity target;

    public TriggerBot() {
        super(Categories.Combat, "trigger-bot", "Attacks entities as soon as they are in your crosshair.", "triggerbot", "shoot-on-sight");
    }

    @Override
    public void onActivate() {
        attackTimer = 0;
        target = null;
    }

    @Override
    public void onDeactivate() {
        target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        target = mc.crosshairPickEntity;

        if (!PlayerUtils.isAlive() || target == null || !entityCheck(target)) {
            attackTimer = 0;
            return;
        }
        if (onlyOnClick.get() && !mc.options.keyAttack.isDown()) {
            attackTimer = 0;
            return;
        }
        if (TickRate.INSTANCE.getTimeSinceLastTick() >= 1f && pauseOnLag.get()) return;

        if (rotate.get()) {
            double yaw = Rotations.getYaw(target) + Utils.random(-aimRadius.get(), aimRadius.get());
            double pitch = Rotations.getPitch(target, Target.Body) + Utils.random(-aimRadius.get(), aimRadius.get());
            Rotations.rotate(yaw, pitch);
        }

        if (attackTimer > 0) {
            attackTimer--;
            return;
        }
        if (attackCooldown.get() && mc.player.getAttackStrengthScale(0.5f) < 1f) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        attackTimer = randomizeDelay.get() ? Utils.random(delayMin.get(), delayMax.get() + 1) : 0;
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.getCameraEntity())) return false;
        if ((entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) || !entity.isAlive()) return false;

        if (!entities.get().contains(entity.getType())) return false;

        AABB hitbox = entity.getBoundingBox();
        if (!PlayerUtils.isWithin(
            Mth.clamp(mc.player.getX(), hitbox.minX, hitbox.maxX),
            Mth.clamp(mc.player.getY(), hitbox.minY, hitbox.maxY),
            Mth.clamp(mc.player.getZ(), hitbox.minZ, hitbox.maxZ),
            range.get()
        )) return false;

        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, wallsRange.get())) return false;
        if (ignoreNamed.get() && entity.hasCustomName()) return false;
        if (ignoreTamed.get()) {
            if (entity instanceof OwnableEntity tameable
                && tameable.getOwner() != null
                && tameable.getOwner().equals(mc.player)
            ) return false;
        }
        if (entity instanceof Player player) {
            if (player.isCreative()) return false;
            if (!Friends.get().shouldAttack(player)) return false;
        }
        return true;
    }

    @Override
    public String getInfoString() {
        if (target != null) return EntityUtils.getName(target);
        return null;
    }
}