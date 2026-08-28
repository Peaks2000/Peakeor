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

import java.util.Random;
import java.util.Set;

public class TriggerBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");

    // General

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards the entity in your crosshair using a human-like aim model. Optional, off by default.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> aimRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("aim-radius")
        .description("How much hand tremor appears around the target while aiming, in degrees.")
        .defaultValue(1.5)
        .range(0, 10)
        .sliderRange(0, 5)
        .visible(rotate::get)
        .build()
    );

    private final Setting<Boolean> attackCooldown = sgGeneral.add(new BoolSetting.Builder()
        .name("attack-cooldown")
        .description("Times clicks around the weapon cooldown so most swings land near full strength, with a drifting, imperfect rhythm.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> clickSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("click-speed")
        .description("Baseline clicking speed in clicks per second when attack-cooldown is off. The bot drifts around this like a real hand.")
        .defaultValue(4.5)
        .range(0.5, 15)
        .sliderRange(0.5, 15)
        .visible(() -> !attackCooldown.get())
        .build()
    );

    private final Setting<Integer> clickVariation = sgGeneral.add(new IntSetting.Builder()
        .name("click-variation")
        .description("How much click timing fluctuates. High values feel erratic, low values feel like a metronome.")
        .defaultValue(20)
        .range(0, 60)
        .sliderRange(0, 60)
        .build()
    );

    private final Setting<Integer> doubleClick = sgGeneral.add(new IntSetting.Builder()
        .name("double-click")
        .description("Percent chance of an accidental double swing, like a finger slip. The second swing whiffs and wastes the cooldown.")
        .defaultValue(8)
        .range(0, 40)
        .sliderRange(0, 40)
        .build()
    );

    private final Setting<Integer> pauseChance = sgGeneral.add(new IntSetting.Builder()
        .name("pause-chance")
        .description("Percent chance of a short readjustment pause, like lifting the finger to re-track.")
        .defaultValue(12)
        .range(0, 50)
        .sliderRange(0, 50)
        .visible(() -> !attackCooldown.get())
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
        .description("Median reaction time in ticks before the bot first clicks on a new target. Drawn from a human-shaped distribution.")
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
        .description("Center of the drifting swing charge (0-1) the bot clicks at. The charge wanders around this, so pacing changes naturally.")
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

    private static final Random random = new Random();

    private Entity target;
    private Entity lockedTarget;
    private int reactionTimer;
    private long awaySince;

    private float cps;
    private long cpsUpdateMillis;
    private int clickTimer;
    private int lastGap;
    private int postMissTimer;
    private boolean doublePending;
    private float chargeWalk;

    private float aimYaw;
    private float aimPitch;
    private float tremorX;
    private float tremorY;

    public TriggerBot() {
        super(Categories.Combat, "trigger-bot", "Attacks entities as soon as they are in your crosshair, clicking with a fluid, human-like cadence.", "triggerbot", "shoot-on-sight");
    }

    @Override
    public void onActivate() {
        target = null;
        lockedTarget = null;
        reactionTimer = 0;
        awaySince = 0;
        cps = clickSpeed.get().floatValue();
        cpsUpdateMillis = System.currentTimeMillis();
        clickTimer = 0;
        lastGap = 10;
        postMissTimer = 0;
        doublePending = false;
        chargeWalk = swingPoint.get().floatValue();
        aimYaw = mc.player.getYRot();
        aimPitch = mc.player.getXRot();
        tremorX = 0;
        tremorY = 0;
    }

    @Override
    public void onDeactivate() {
        target = null;
        lockedTarget = null;
        doublePending = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        target = mc.crosshairPickEntity;

        if (!PlayerUtils.isAlive() || target == null || !entityCheck(target)) {
            onTargetLost();
            return;
        }
        if (onlyOnClick.get() && !mc.options.keyAttack.isDown()) {
            onTargetLost();
            return;
        }
        if (TickRate.INSTANCE.getTimeSinceLastTick() >= 1f && pauseOnLag.get()) return;

        if (rotate.get()) updateAim();

        if (lockedTarget != target) {
            lockedTarget = target;
            reactionTimer = sampleReaction();
            awaySince = 0;
        }

        if (reactionTimer > 0) {
            reactionTimer--;
            return;
        }

        updateCps();

        if (postMissTimer > 0) {
            postMissTimer--;
            return;
        }

        if (onlyCrits.get()) {
            boolean falling = mc.player.fallDistance > 0
                && !mc.player.onGround()
                && !mc.player.onClimbable()
                && !mc.player.isInWater()
                && !mc.player.isInLava();
            if (!falling) return;
        }

        if (missChanceEnabled.get() && Utils.random(0, 100) < missChance.get()) {
            postMissTimer = 2 + Utils.random(0, 3);
            return;
        }

        if (attackCooldown.get()) handleCooldownClicking();
        else handleFreeClicking();
    }

    private void handleCooldownClicking() {
        if (doublePending) {
            swing();
            doublePending = false;
            chargeWalk = 1f;
            return;
        }

        float minWalk = Math.max(0.55f, (float) (swingPoint.get() - 0.25));
        chargeWalk = Mth.clamp(chargeWalk + 0.03f * (float) random.nextGaussian(), minWalk, 1f);

        if (mc.player.getAttackStrengthScale(0.5f) < chargeWalk) return;

        swing();
        chargeWalk = swingPoint.get().floatValue();
        doublePending = Utils.random(0, 100) < doubleClick.get();
    }

    private void handleFreeClicking() {
        if (clickTimer > 0) {
            clickTimer--;
            return;
        }

        swing();
        clickTimer = handGap();
    }

    private int handGap() {
        double base = 20.0 / Math.max(1.0E-3, cps);
        float sigma = 0.12f + clickVariation.get() * 0.003f;
        float jitter = (float) Math.exp(sigma * (float) random.nextGaussian());
        int delay = (int) Math.round(base * jitter);

        if (Utils.random(0, 100) < doubleClick.get()) {
            return 1;
        }

        if (Utils.random(0, 100) < pauseChance.get()) {
            delay += Utils.random(3, 9);
        }

        delay = (int) Math.round(delay * 0.6 + lastGap * 0.4);
        delay = Mth.clamp(delay, 1, 30);
        lastGap = delay;
        return delay;
    }

    private void updateCps() {
        long now = System.currentTimeMillis();
        if (now - cpsUpdateMillis < 1000) return;
        cpsUpdateMillis = now;
        float variation = clickVariation.get() * 0.05f;
        cps = Mth.clamp(cps + (float) random.nextGaussian() * variation,
            clickSpeed.get().floatValue() * 0.55f,
            clickSpeed.get().floatValue() * 1.7f);
    }

    private int sampleReaction() {
        float median = Math.max(1f, reactionTime.get());
        int ticks = (int) Math.round(Math.exp(Math.log(median) + 0.45 * random.nextGaussian()));
        if (awaySince != 0 && System.currentTimeMillis() - awaySince < 2000) {
            ticks = Math.max(1, (int) Math.round(ticks * 0.5));
        }
        return Mth.clamp(ticks, 0, 25);
    }

    private void onTargetLost() {
        if (target != null || lockedTarget != null) awaySince = System.currentTimeMillis();
        target = null;
        lockedTarget = null;
        reactionTimer = 0;
        clickTimer = 0;
        postMissTimer = 0;
        doublePending = false;
    }

    private void updateAim() {
        double desiredYaw = Rotations.getYaw(target);
        double desiredPitch = Rotations.getPitch(target, Target.Body);

        float deltaYaw = Mth.wrapDegrees((float) (desiredYaw - aimYaw));
        float deltaPitch = Mth.wrapDegrees((float) (desiredPitch - aimPitch));

        float maxStepYaw = 2.2f + Math.abs(deltaYaw) * 0.6f;
        float maxStepPitch = 2.2f + Math.abs(deltaPitch) * 0.6f;

        aimYaw += Mth.clamp(deltaYaw, -maxStepYaw, maxStepYaw);
        aimPitch += Mth.clamp(deltaPitch, -maxStepPitch, maxStepPitch);

        tremorX = tremorX * 0.8f + (float) random.nextGaussian() * 0.08f * aimRadius.get().floatValue();
        tremorY = tremorY * 0.8f + (float) random.nextGaussian() * 0.08f * aimRadius.get().floatValue();

        Rotations.rotate(Mth.wrapDegrees(aimYaw + tremorX), Mth.wrapDegrees(aimPitch + tremorY));
    }

    private void swing() {
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
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