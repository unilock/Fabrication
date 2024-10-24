package com.unascribed.fabrication.mixin.g_weird_tweaks.chaining_creepers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.chaining_creepers")
public abstract class MixinLivingEntity {

	@FabInject(method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at=@At("HEAD"), cancellable=true)
	public void lightCreepersOnExplosion(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		Object self = this;
		if (!(FabConf.isEnabled("*.chaining_creepers") && self instanceof CreeperEntity && source.isIn(DamageTypeTags.IS_EXPLOSION))) return;
		((CreeperEntity)self).ignite();
		cir.setReturnValue(false);
	}
}
