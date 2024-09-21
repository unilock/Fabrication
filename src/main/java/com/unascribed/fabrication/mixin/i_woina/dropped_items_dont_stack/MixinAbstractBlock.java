package com.unascribed.fabrication.mixin.i_woina.dropped_items_dont_stack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractBlock.class)
@EligibleIf(configAvailable="*.dropped_items_dont_stack")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public abstract class MixinAbstractBlock {

	@ModifyReturnValue(method="getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/loot/context/LootContextParameterSet$Builder;)Ljava/util/List;", at=@At(value="TAIL"))
	private List<ItemStack> splitLoot(List<ItemStack> inp) {
		if(!FabConf.isEnabled("*.dropped_items_dont_stack") || inp == null) return inp;
		List<ItemStack> ret = new ArrayList<>();
		for (ItemStack stack : inp) {
			ItemStack single = stack.copy();
			single.setCount(1);
			for (int i = 0; i < stack.getCount()-1; i++) {
				ret.add(single.copy());
			}
			ret.add(single);
		}
		return ret;
	}

}
