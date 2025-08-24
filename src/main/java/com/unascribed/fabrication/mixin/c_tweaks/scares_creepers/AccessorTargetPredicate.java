package com.unascribed.fabrication.mixin.c_tweaks.scares_creepers;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.ai.TargetPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TargetPredicate.class)
@EligibleIf(configAvailable="*.scares_creepers")
public interface AccessorTargetPredicate {
	@Accessor
	@Mutable
	void setAttackable(boolean value);
}
