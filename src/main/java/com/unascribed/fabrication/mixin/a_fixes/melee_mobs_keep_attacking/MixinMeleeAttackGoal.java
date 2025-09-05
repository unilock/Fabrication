package com.unascribed.fabrication.mixin.a_fixes.melee_mobs_keep_attacking;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MeleeAttackGoal.class)
@EligibleIf(configAvailable="*.melee_mobs_keep_attacking")
public abstract class MixinMeleeAttackGoal {

	@Shadow @Final
	protected PathAwareEntity mob;

	@Shadow @Final
	private double speed;

	@Shadow
	private Path path;

	@ModifyExpressionValue(at=@At(value="INVOKE", target="Lnet/minecraft/entity/ai/pathing/EntityNavigation;isIdle()Z"), method="shouldContinue()Z")
	public boolean fabrication$keepAttacking(boolean old) {
		if (!FabConf.isEnabled("*.melee_mobs_keep_attacking")) return old;
		if (old && this.mob.distanceTo(this.mob.getTarget()) < 10) {
			this.path = this.mob.getNavigation().findPathTo(this.mob.getTarget(), 0);
			this.mob.getNavigation().startMovingAlong(this.path, this.speed);
		}
		return false;
	}

}
