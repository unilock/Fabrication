package com.unascribed.fabrication.mixin.a_fixes.no_night_vision_flash;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
@EligibleIf(configAvailable="*.no_night_vision_flash", envMatches=Env.CLIENT)
public class MixinGameRenderer {

	@ModifyReturnValue(at=@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;sin(F)F"), method="getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F")
	private static float fabrication$removeFlash(float original, float f) {
		if (FabConf.isEnabled("*.no_night_vision_flash")) {
			float time = (f/((float)Math.PI*0.2f));
			if (time < 0) time = 0;
			float a = (time/200f);
			a = a*a; // exponential falloff
			return (a-0.7f)/0.3f;
		}
		return original;
	}

}
