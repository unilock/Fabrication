package com.unascribed.fabrication.mixin.c_tweaks.scares_creepers;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FleeEntityGoal.class)
@EligibleIf(configAvailable="*.scares_creepers")
public interface AccessorFleeEntityGoal {
	@Accessor
	TargetPredicate getWithinRangePredicate();
}
