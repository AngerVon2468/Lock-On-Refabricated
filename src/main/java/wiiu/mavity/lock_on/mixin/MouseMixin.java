package wiiu.mavity.lock_on.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import wiiu.mavity.lock_on.LockOnHandler;

@Mixin(Mouse.class)
public class MouseMixin {

	@Shadow @Final private MinecraftClient client;

	@Inject(method = "updateMouse",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), cancellable = true)
	private void onTurn(CallbackInfo ci, @Local(ordinal = 1) double d2, @Local(ordinal = 2) double d3) {
		if (LockOnHandler.handleKeyPress(this.client.player, d2, d3)) {
			ci.cancel();
		}
	}
}