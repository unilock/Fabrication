package com.unascribed.fabrication.mixin.f_balance.interrupting_damage;

import com.google.common.collect.ImmutableList;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.InterruptableRangedMob;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.interrupting_damage")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public abstract class MixinLivingEntity {

	@Shadow
	public abstract boolean isUsingItem();

	@Shadow
	public abstract void stopUsingItem();

	@Shadow
	public abstract boolean blockedByShield(DamageSource source);

	private static final Predicate<List<?>> fabrication$interruptingDamagePredicate = ConfigPredicates.getFinalPredicate("*.interrupting_damage");
	@Inject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
	public void interruptUsage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.interrupting_damage")) return;
		if (blockedByShield(source)) return;
		if (amount >= 2 && fabrication$interruptingDamagePredicate.test(ImmutableList.of(this, source))) {
			if (this instanceof InterruptableRangedMob)
				((InterruptableRangedMob) this).fabrication$interruptRangedMob();
			if (isUsingItem()) stopUsingItem();
		}
	}

}
