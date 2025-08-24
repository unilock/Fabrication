package com.unascribed.fabrication.mixin.g_weird_tweaks.instant_pickup;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
@EligibleIf(configAvailable="*.instant_pickup")
public interface AccessorItemEntity {
	@Accessor
	int getPickupDelay();
}
