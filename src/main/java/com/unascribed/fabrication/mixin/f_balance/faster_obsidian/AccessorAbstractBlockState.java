package com.unascribed.fabrication.mixin.f_balance.faster_obsidian;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBlock.AbstractBlockState.class)
@EligibleIf(configAvailable="*.faster_obsidian")
public interface AccessorAbstractBlockState {
	@Accessor
	float getHardness();

	@Accessor
	@Mutable
	void setHardness(float value);
}
