package wiiu.mavity.lock_on;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;

import org.jetbrains.annotations.*;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import org.lwjgl.glfw.GLFW;

import wiiu.mavity.lock_on.config.LockOnConfig;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public class LockOnHandler {

    public static List<LivingEntity> list = new ArrayList<>();
    public static boolean lockedOn;
    private static @Nullable LivingEntity targeted;
    private static final Predicate<LivingEntity> ENTITY_PREDICATE = entity -> entity.isAlive() && entity.isAttackable();
    private static int cycle = -1;

    // Categories (Translation keys)
    public static final String CATEGORY_LOCK_ON = "key.category.lock_on.lock_on";

    // Keybinds (Translation keys)
    public static final String LOCK_ON_KEY = "key.lock_on.lock_on";
    public static final String SWITCH_TARGET_KEY = "key.lock_on.switch_target";

    // Keybinds
    public static KeyBinding LOCK_ON = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            LOCK_ON_KEY,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            CATEGORY_LOCK_ON
    ));
    public static KeyBinding SWITCH_TARGET = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            SWITCH_TARGET_KEY,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            CATEGORY_LOCK_ON
    ));

    public static void registerLockOnKeybinds() {
        registerKeyInputs();
        LockOnRefabricatedClient.LOGGER.info(LockOnRefabricatedClient.NAME + " has registered its keybinds.");
    }

    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            ClientPlayerEntity clientPlayer = client.player;
            if (LOCK_ON.wasPressed()) {

                if (lockedOn) {

                    leaveLockOn();

                } else {

                    attemptEnterLockOn(clientPlayer);

                }

            }
            if (SWITCH_TARGET.wasPressed() && lockedOn) {

                tabToNextEnemy(clientPlayer);

            }
            tickLockedOn();
        });
    }

    public static boolean handleKeyPress(PlayerEntity player, double d2, double d3) {
        if (player != null && !MinecraftClient.getInstance().isPaused()) {
            if (targeted != null) {
                Vec3d targetPos = targeted.getPos().add(0,targeted.getEyeHeight(targeted.getPose()),0);
                Vec3d targetVec = targetPos.subtract(player.getPos().add(0, player.getEyeHeight(player.getPose()),0)).normalize();
                double targetAngleX = MathHelper.wrapDegrees(Math.atan2(-targetVec.x, targetVec.z) * 180 / Math.PI);
                double targetAngleY = Math.atan2(targetVec.y , targetVec.horizontalLength()) * 180 / Math.PI;
                double xRot = MathHelper.wrapDegrees(player.getPitch());
                double yRot = MathHelper.wrapDegrees(player.getYaw());
                double toTurnX = MathHelper.wrapDegrees(yRot - targetAngleX);
                double toTurnY = MathHelper.wrapDegrees(xRot + targetAngleY);

                player.changeLookDirection(-toTurnX,-toTurnY);
                return true;
            }
        }
        return false;
    }

    private static void tickLockedOn() {
        list.removeIf(livingEntity -> !livingEntity.isAlive());
        if (targeted != null && !targeted.isAlive()) {
            targeted = null;
            lockedOn = false;
        }
    }

    public static @Nullable LivingEntity findNearby(@NotNull PlayerEntity player) {

        int r = 16;

        final TargetPredicate ENEMY_CONDITION = TargetPredicate.createAttackable().setBaseMaxDistance(r).setPredicate(ENTITY_PREDICATE);

        List<LivingEntity> entities = player.getWorld()
                .getTargets(LivingEntity.class, ENEMY_CONDITION, player, player.getBoundingBox().expand(r)).stream().filter(player::canSee).toList();
        if (lockedOn) {
            cycle++;
            for (LivingEntity entity : entities) {
                if (!list.contains(entity)) {
                    list.add(entity);
                    return entity;
                }
            }

            // Cycle existing entity
            if (cycle >= list.size()) {
                cycle = 0;
            }
            return list.get(cycle);
        } else {
            if (!entities.isEmpty()) {
                LivingEntity first = entities.get(0);
                list.add(first);
                return entities.get(0);
            } else {
                return null;
            }
        }
    }

    private static void leaveLockOn() {
        targeted = null;
        lockedOn = false;
        list.clear();
    }

    private static void attemptEnterLockOn(PlayerEntity player) {
        tabToNextEnemy(player);
        if (targeted != null) {
            lockedOn = true;
        }
    }

    private static void tabToNextEnemy(PlayerEntity player) {
        targeted = findNearby(player);
    }

    public static void renderWorldLast(Entity entity, MatrixStack poseStack, VertexConsumerProvider buffers, Quaternionf quaternion) {
        if (targeted == entity) {
            VertexConsumer builder = buffers.getBuffer(LockOnRenderType.RENDER_TYPE);
            poseStack.push();

            poseStack.translate(0, entity.getHeight() / 2, 0);

            poseStack.multiply(quaternion);


            float rotate = (Util.getMeasuringTimeNano() / -8_000_000f);

            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotate));


            float width = (float) LockOnConfig.triangleWidth;
            float height = (float) LockOnConfig.triangleHeight;

            int color = 0xffffff00;

            try {
                color = Integer.decode("#FFFFFF00");
            } catch (NumberFormatException numberFormatException) {
                // Oof
            }
            RenderSystem.disableCull();
            fillTriangles(builder, poseStack.peek().getPositionMatrix(), 0, entity.getHeight() / 2f, -width / 2f, height,entity.getHeight() / 2f, 0, color);
            poseStack.pop();
        }
    }

    public enum Dir {
        up, down, left, right
    }

    public static void fillTriangles(VertexConsumer builder, Matrix4f matrix4f, float x, float y, float width, float height, float bbHeight, float z, int aarrggbb) {
        float a = (aarrggbb >> 24 & 0xff) / 255f;
        float r = (aarrggbb >> 16 & 0xff) / 255f;
        float g = (aarrggbb >> 8 & 0xff) / 255f;
        float b = (aarrggbb & 0xff) / 255f;

        fillTriangle(builder, matrix4f, x, y, width, height, bbHeight, z, r, g, b, a, Dir.up);
        fillTriangle(builder, matrix4f, x, -y, width, height, bbHeight, z, r, g, b, a, Dir.down);
        fillTriangle(builder, matrix4f, x, 0, width, height, bbHeight, z, r, g, b, a, Dir.left);
        fillTriangle(builder, matrix4f, x, 0, width, height, bbHeight, z, r, g, b, a, Dir.right);
    }

    public static void fillTriangle(VertexConsumer builder, Matrix4f matrix4f, float x, float y, float width, float height, float bbHeight, float z, float r, float g, float b, float a, Dir dir) {

        switch (dir) {
            case up -> {
                builder.vertex(matrix4f, x, y, z).color(r, g, b, a).next();
                builder.vertex(matrix4f, x + width / 2, y + height, z).color(r, g, b, a).next();
                builder.vertex(matrix4f, x - width / 2, y + height, z).color(r, g, b, a).next();
            }
            case down -> {
                builder.vertex(matrix4f, x, y, z).color(r, g, b, a).next();
                builder.vertex(matrix4f, x + width / 2, y - height, z).color(r, g, b, a).next();
                builder.vertex(matrix4f, x - width / 2, y - height, z).color(r, g, b, a).next();
            }

            case left -> {
                builder.vertex(matrix4f, x - bbHeight, y, z).color(r, g, b, a).next();
                builder.vertex(matrix4f, x - bbHeight - height, y - width / 2, z).color(r, g, b, a).next();
                builder.vertex(matrix4f, x - bbHeight - height, y + width / 2, z).color(r, g, b, a).next();
            }

            case right -> {
                builder.vertex(matrix4f, x + bbHeight, y, z).color(r, g, b, a).next();
                builder.vertex(matrix4f, x + bbHeight + height, y - width / 2, z).color(r, g, b, a).next();
                builder.vertex(matrix4f, x + bbHeight + height, y + width / 2, z).color(r, g, b, a).next();
            }
        }
    }
}