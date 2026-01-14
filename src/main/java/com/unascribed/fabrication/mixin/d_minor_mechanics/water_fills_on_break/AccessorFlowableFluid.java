package com.unascribed.fabrication.mixin.d_minor_mechanics.water_fills_on_break;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FlowableFluid.class)
@EligibleIf(anyConfigAvailable={"*.water_fills_on_break", "*.water_fills_on_break_strict"})
public interface AccessorFlowableFluid {
	@Invoker("isInfinite")
	boolean fabrication$isInfinite(World world);
}
