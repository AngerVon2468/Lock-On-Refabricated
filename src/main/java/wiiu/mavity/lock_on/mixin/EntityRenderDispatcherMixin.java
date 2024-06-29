package wiiu.mavity.lock_on.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import org.joml.Quaternionf;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import wiiu.mavity.lock_on.LockOnHandler;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    public abstract Quaternionf getRotation();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", shift = At.Shift.AFTER))
    private void renderLockOnIcon(Entity entity, double worldX, double worldY, double worldZ, float entityYRot, float partialTicks, MatrixStack poseStack, VertexConsumerProvider buffers, int light, CallbackInfo ci) {
        LockOnHandler.renderWorldLast(entity, poseStack, buffers, getRotation());
    }
}