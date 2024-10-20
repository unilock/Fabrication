package com.unascribed.fabrication.mixin.c_tweaks.campfires_cook_entities;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.campfires_cook_entities")
public class MixinLivingEntity {

	@FabInject(at=@At("HEAD"), method="drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V")
	public void dropLoot(ServerWorld world, DamageSource source, CallbackInfo ci) {
		if (FabConf.isEnabled("*.campfires_cook_entities") && source.isOf(DamageTypes.IN_FIRE)) ((LivingEntity)(Object)this).setFireTicks(1);
	}

}
