package com.unascribed.fabrication.mixin.d_minor_mechanics.furnace_minecart_resupplying;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(configAvailable="*.furnace_minecart_resupplying")
public interface AccessorFurnaceMinecartEntity {
	@Accessor
	static Ingredient getACCEPTABLE_FUEL() {
		throw new IllegalStateException();
	}
}
