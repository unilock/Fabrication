package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
@EligibleIf(configAvailable="*.canhit")
public class MixinFireworkRocketEntity {

	@WrapWithCondition(at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), method="explode()V")
	private boolean fabrication$canDamage(LivingEntity subject, DamageSource source, float amount) {
		if (FabConf.isEnabled("*.canhit") && this instanceof SetCanHitList schl) {
			return CanHitUtil.canHit(schl.fabrication$getCanHitList(), subject) && CanHitUtil.canHit(schl.fabrication$getCanHitList2(), subject);
		}
		return true;
	}

}
