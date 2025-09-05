package com.unascribed.fabrication.mixin.b_utility.toggle_sprint;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.features.FeatureToggleSprint;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
@EligibleIf(configAvailable="*.toggle_sprint", envMatches=Env.CLIENT)
public class MixinClientPlayerEntity {

	@WrapOperation(at=@At(value="INVOKE", target="Lnet/minecraft/client/option/KeyBinding;isPressed()Z"), method="tickMovement()V")
	private boolean fabrication$toggleSprint(KeyBinding keyBinding, Operation<Boolean> original) {
		if (FabConf.isEnabled("*.toggle_sprint") && keyBinding.getTranslationKey().equals("key.sprint") && FeatureToggleSprint.sprinting) {
			return true;
		}
		return original.call(keyBinding);
	}

}
