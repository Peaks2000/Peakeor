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
import net.minecraft.world.phys.Vec3;

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
        .description("Adds a random deviation to the base delay so attacks don't fall on a perfectly even cadence.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> randomDelay = sgGeneral.add(new IntSetting.Builder()
        .name("random-delay")
        .description("Maximum random ticks added on top of the base delay between attacks.")
        .defaultValue(3)
        .range(0, 20)
        .sliderRange(0, 20)
        .visible(randomizeDelay::get)
        .build()
    );

    private final Setting<Boolean> missChanceEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("miss-chance-enabled")
        .description("Toggles the random chance to miss a swing, simulating human error.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> missChance = sgGeneral.add(new DoubleSetting.Builder()
        .name("miss-chance")
        .description("Percent chance the bot misses a swing entirely, simulating human error.")
        .defaultValue(20)
        .range(0, 100)
        .sliderRange(0, 100)
        .visible(missChanceEnabled::get)
        .build()
    );

    private final Setting<Boolean> humanAim = sgGeneral.add(new BoolSetting.Builder()
        .name("human-aim")
        .description("Makes aim wander and ease like a real mouse instead of sitting dead-still on the target.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> aimSmoothness = sgGeneral.add(new DoubleSetting.Builder()
        .name("aim-smoothness")
        .description("How fast the crosshair eases toward the target. Lower is slower and more human.")
        .defaultValue(0.35)
        .range(0.05, 1)
        .sliderRange(0.05, 1)
        .visible(humanAim::get)
        .build()
    );

    private final Setting<Double> leadTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("lead-time")
        .description("How far in advance the crosshair predicts the target's position based on relative movement, in ticks. Lets it track moving targets instead of lagging behind.")
        .defaultValue(3)
        .range(0, 10)
        .sliderRange(0, 10)
        .visible(humanAim::get)
        .build()
    );

    private final Setting<Double> aimDrift = sgGeneral.add(new DoubleSetting.Builder()
        .name("aim-drift")
        .description("How far the crosshair is allowed to drift and wander off target between corrections.")
        .defaultValue(0.8)
        .range(0, 3)
        .sliderRange(0, 3)
        .visible(humanAim::get)
        .build()
    );

    private final Setting<Integer> movingMissChance = sgGeneral.add(new IntSetting.Builder()
        .name("moving-miss-chance")
        .description("Percent chance a swing whiffs past a target that is moving away, so you don't perfectly track a retreating player.")
        .defaultValue(25)
        .range(0, 100)
        .sliderRange(0, 100)
        .visible(humanAim::get)
        .build()
    );

    private final Setting<Double> movingWhiff = sgGeneral.add(new DoubleSetting.Builder()
        .name("moving-whiff")
        .description("How far the swing misses past a moving target, in blocks of lead offset.")
        .defaultValue(1.5)
        .range(0, 4)
        .sliderRange(0, 4)
        .visible(humanAim::get)
        .build()
    );

    private final Setting<Integer> reactionTime = sgGeneral.add(new IntSetting.Builder()
        .name("reaction-time")
        .description("Random delay in ticks before the bot first engages a target, emulating human reaction time.")
        .defaultValue(4)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Boolean> onlyOnClick = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-click")
        .description("Only attacks when you are holding left click.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> onlyCrits = sgGeneral.add(new BoolSetting.Builder()
        .name("only-crits")
        .description("Only attacks when a jump would land a critical hit, so hits feel earned and less systematic.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> baseDelay = sgGeneral.add(new IntSetting.Builder()
        .name("base-delay")
        .description("Base tick delay between attacks. The random delay is added on top of this.")
        .defaultValue(2)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Double> swingPoint = sgGeneral.add(new DoubleSetting.Builder()
        .name("swing-point")
        .description("Random weapon charge (0-1) at which the bot swings. Lower lets it strike before fully charged; higher waits for a fuller charge. Randomised each swing so timing isn't perfect.")
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
    private float aimYaw;
    private float aimPitch;
    private float velYaw;
    private float velPitch;
    private float noiseYaw;
    private float noisePitch;
    private float nextSwingCharge = 1f;
    private boolean whiffSwing;

    public TriggerBot() {
        super(Categories.Combat, "trigger-bot", "Attacks entities as soon as they are in your crosshair.", "triggerbot", "shoot-on-sight");
    }

    @Override
    public void onActivate() {
        attackTimer = 0;
        reactionTimer = 0;
        target = null;
        lockedTarget = null;
        aimYaw = mc.player.getYRot();
        aimPitch = mc.player.getXRot();
        velYaw = 0;
        velPitch = 0;
        noiseYaw = 0;
        noisePitch = 0;
        whiffSwing = false;
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
            whiffSwing = false;
            return;
        }
        if (onlyOnClick.get() && !mc.options.keyAttack.isDown()) {
            attackTimer = 0;
            reactionTimer = 0;
            lockedTarget = null;
            whiffSwing = false;
            return;
        }
        if (TickRate.INSTANCE.getTimeSinceLastTick() >= 1f && pauseOnLag.get()) return;

        if (lockedTarget != target) {
            lockedTarget = target;
            reactionTimer = reactionTime.get() > 0 ? Utils.random(1, reactionTime.get() + 1) : 0;
            attackTimer = Math.max(attackTimer, reactionTimer);
        }

        if (humanAim.get()) {
            updateNoise();
            Vec3 aimPos = predictAimPos(target);
            if (whiffSwing) {
                Vec3 dir = target.getDeltaMovement();
                if (dir.horizontalDistanceSqr() > 1.0E-6) {
                    aimPos = target.position().add(dir.normalize().scale(movingWhiff.get()));
                }
            }
            float targetYaw = Mth.wrapDegrees((float) (Rotations.getYaw(aimPos) + noiseYaw));
            float targetPitch = Mth.wrapDegrees((float) (Rotations.getPitch(aimPos) + noisePitch));
            updateSpring(targetYaw, targetPitch);
            Rotations.rotate(aimYaw, aimPitch);
        } else if (rotate.get()) {
            double yaw = Rotations.getYaw(target) + Utils.random(-aimRadius.get(), aimRadius.get());
            double pitch = Rotations.getPitch(target, Target.Body) + Utils.random(-aimRadius.get(), aimRadius.get());
            Rotations.rotate(yaw, pitch);
        }

        if (reactionTimer > 0) {
            reactionTimer--;
            return;
        }
        if (attackTimer > 0) {
            attackTimer--;
            if (attackTimer == 0) {
                nextSwingCharge = (float) (swingPoint.get() * Utils.random(0.85, 1.0));
                whiffSwing = isMovingAway() && movingMissChance.get() > 0 && Utils.random(0, 100) < movingMissChance.get();
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
        return baseDelay.get() + (randomizeDelay.get() ? Utils.random(0, randomDelay.get() + 1) : 0);
    }

    private void updateNoise() {
        float amp = (float) Math.max(0.05, aimDrift.get());
        noiseYaw = Mth.clamp(noiseYaw + (float) Utils.random(-0.06, 0.06), -amp * 0.35f, amp * 0.35f);
        noisePitch = Mth.clamp(noisePitch + (float) Utils.random(-0.04, 0.04), -amp * 0.22f, amp * 0.22f);
    }

    private void updateSpring(float targetYaw, float targetPitch) {
        float k = (float) (0.05 + aimSmoothness.get() * 0.12);
        float damp = (float) (0.82 - aimSmoothness.get() * 0.2);
        float dy = Mth.wrapDegrees(targetYaw - aimYaw);
        float dp = Mth.wrapDegrees(targetPitch - aimPitch);
        velYaw += dy * k;
        velPitch += dp * k;
        velYaw *= damp;
        velPitch *= damp;
        aimYaw = Mth.wrapDegrees(aimYaw + velYaw);
        aimPitch = Mth.wrapDegrees(aimPitch + velPitch);
    }

    private Vec3 predictAimPos(Entity ent) {
        Vec3 relVel = ent.getDeltaMovement().subtract(mc.player.getDeltaMovement());
        double lead = Math.max(0.0, leadTime.get()) / 20.0;
        Vec3 base = ent.position();
        double y = base.y + ent.getBbHeight() / 2;
        return new Vec3(base.x + relVel.x * lead, y, base.z + relVel.z * lead);
    }

    private boolean isMovingAway() {
        if (target == null) return false;
        Vec3 velocity = target.getDeltaMovement();
        if (velocity.horizontalDistanceSqr() == 0) return false;
        Vec3 toTarget = target.position().subtract(mc.player.position());
        return toTarget.dot(velocity) > 0;
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