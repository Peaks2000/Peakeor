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

import java.util.Set;

public class TriggerBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");

    // General

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards the entity in your crosshair with a small random offset. Only use if you actually want the bot to move your aim.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> aimRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("aim-radius")
        .description("Random rotation offset in degrees applied while aiming.")
        .defaultValue(1.5)
        .range(0, 10)
        .sliderRange(0, 10)
        .visible(rotate::get)
        .build()
    );

    private final Setting<Boolean> attackCooldown = sgGeneral.add(new BoolSetting.Builder()
        .name("attack-cooldown")
        .description("Waits for the Minecraft attack cooldown of the weapon you are holding before attacking.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> baseDelay = sgGeneral.add(new IntSetting.Builder()
        .name("base-delay")
        .description("Base tick delay between attacks, like the natural delay between your clicks.")
        .defaultValue(2)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Integer> randomDelay = sgGeneral.add(new IntSetting.Builder()
        .name("random-delay")
        .description("Maximum random ticks added on top of the base delay so your clicking speed varies naturally.")
        .defaultValue(3)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Boolean> missChanceEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("miss-chance-enabled")
        .description("Toggles the random chance to skip a swing, simulating human error.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> missChance = sgGeneral.add(new DoubleSetting.Builder()
        .name("miss-chance")
        .description("Percent chance the bot skips a swing entirely, like accidentally clicking early or missing.")
        .defaultValue(10)
        .range(0, 100)
        .sliderRange(0, 100)
        .visible(missChanceEnabled::get)
        .build()
    );

    private final Setting<Integer> reactionTime = sgGeneral.add(new IntSetting.Builder()
        .name("reaction-time")
        .description("Random delay in ticks before the bot first clicks on a new target, emulating human reaction time.")
        .defaultValue(4)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Boolean> onlyOnClick = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-click")
        .description("Only attacks when you are actually holding left click.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> onlyCrits = sgGeneral.add(new BoolSetting.Builder()
        .name("only-crits")
        .description("Only attacks when a jump would land a critical hit.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> swingPoint = sgGeneral.add(new DoubleSetting.Builder()
        .name("swing-point")
        .description("Random weapon charge (0-1) at which the bot clicks. Lower clicks before fully charged; higher waits for a fuller charge.")
        .defaultValue(0.9)
        .range(0.5, 1)
        .sliderRange(0.5, 1)
        .visible(attackCooldown::get)
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
    private Entity lockedTarget;
    private int reactionTimer;
    private float nextSwingCharge = 1f;

    public TriggerBot() {
        super(Categories.Combat, "trigger-bot", "Attacks entities as soon as they are in your crosshair.", "triggerbot", "shoot-on-sight");
    }

    @Override
    public void onActivate() {
        attackTimer = 0;
        reactionTimer = 0;
        target = null;
        lockedTarget = null;
    }

    @Override
    public void onDeactivate() {
        target = null;
        lockedTarget = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        target = mc.crosshairPickEntity;

        if (!PlayerUtils.isAlive() || target == null || !entityCheck(target)) {
            attackTimer = 0;
            reactionTimer = 0;
            lockedTarget = null;
            return;
        }
        if (onlyOnClick.get() && !mc.options.keyAttack.isDown()) {
            attackTimer = 0;
            reactionTimer = 0;
            lockedTarget = null;
            return;
        }
        if (TickRate.INSTANCE.getTimeSinceLastTick() >= 1f && pauseOnLag.get()) return;

        if (rotate.get()) {
            double yaw = Rotations.getYaw(target) + Utils.random(-aimRadius.get(), aimRadius.get());
            double pitch = Rotations.getPitch(target, Target.Body) + Utils.random(-aimRadius.get(), aimRadius.get());
            Rotations.rotate(yaw, pitch);
        }

        if (lockedTarget != target) {
            lockedTarget = target;
            reactionTimer = reactionTime.get() > 0 ? Utils.random(1, reactionTime.get() + 1) : 0;
            attackTimer = Math.max(attackTimer, reactionTimer);
        }

        if (reactionTimer > 0) {
            reactionTimer--;
            return;
        }
        if (attackTimer > 0) {
            attackTimer--;
            if (attackTimer == 0) {
                nextSwingCharge = (float) (swingPoint.get() * Utils.random(0.85, 1.0));
            }
            return;
        }
        if (attackCooldown.get() && mc.player.getAttackStrengthScale(0.5f) < nextSwingCharge) return;

        if (onlyCrits.get()) {
            boolean falling = mc.player.fallDistance > 0
                && !mc.player.onGround()
                && !mc.player.onClimbable()
                && !mc.player.isInWater()
                && !mc.player.isInLava();
            if (!falling) return;
        }

        if (missChanceEnabled.get() && Utils.random(0, 100) < missChance.get()) {
            attackTimer = nextAttackTimer();
            return;
        }

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        attackTimer = nextAttackTimer();
    }

    private int nextAttackTimer() {
        return baseDelay.get() + Utils.random(0, randomDelay.get() + 1);
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.getCameraEntity())) return false;
        if ((entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) || !entity.isAlive()) return false;

        if (!entities.get().contains(entity.getType())) return false;

        if (mc.player.distanceTo(entity) > range.get()) return false;

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
