package com.unascribed.fabrication.mixin.e_mechanics.swap_conflicting_enchants;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEnchantmentsComponent.class)
@EligibleIf(configAvailable="*.swap_conflicting_enchants")
public interface AccessorItemEnchantmentsComponent {
	@Accessor
	boolean getShowInTooltip();
}
