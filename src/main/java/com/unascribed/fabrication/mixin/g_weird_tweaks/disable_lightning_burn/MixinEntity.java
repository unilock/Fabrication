package com.unascribed.fabrication.mixin.g_weird_tweaks.disable_lightning_burn;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.disable_lightning_burn")
public class MixinEntity {


	@WrapWithCondition(method="onStruckByLightning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LightningEntity;)V", at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;setFireTicks(I)V"))
	private boolean fabrication$preventFire1(Entity instance, int fireTicks) {
		return !FabConf.isEnabled("*.disable_lightning_burn");
	}
	@WrapWithCondition(method="onStruckByLightning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LightningEntity;)V", at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;setOnFireFor(F)V"))
	private boolean fabrication$preventFire2(Entity instance, float seconds) {
		return !FabConf.isEnabled("*.disable_lightning_burn");
	}
}
