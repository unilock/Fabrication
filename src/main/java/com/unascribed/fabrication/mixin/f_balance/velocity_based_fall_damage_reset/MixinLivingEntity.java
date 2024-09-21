package com.unascribed.fabrication.mixin.f_balance.velocity_based_fall_damage_reset;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;


@Mixin(Entity.class)
@EligibleIf(configAvailable="*.velocity_based_fall_damage_reset")
public abstract class MixinLivingEntity {

	@Shadow
	public float fallDistance;

	@Shadow
	public abstract void onLanding();

	@Shadow
	private Vec3d velocity;

	private static final Predicate<Entity> fabrication$FallDamageResetPredicate = ConfigPredicates.getFinalPredicate("*.velocity_based_fall_damage_reset");

	@WrapWithCondition(method={"move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", "checkWaterState()V"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;onLanding()V"))
	private boolean fabrication$altFallReset(Entity entity) {
		if (!FabConf.isEnabled("*.velocity_based_fall_damage_reset")) return true;
		if (fabrication$FallDamageResetPredicate.test(entity)) {
			if (this.velocity.y > 0.2) return true;
			this.fallDistance *= Math.max(-this.velocity.y, 5)/5.9;
			if (this.fallDistance < 1.1) {
				this.fallDistance = 0f;
				entity.onLanding();
			}
			return false;
		}
		return true;
	}
}
