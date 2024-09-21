package com.unascribed.fabrication.mixin.g_weird_tweaks.blaze_fertilizer;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import org.spongepowered.asm.mixin.injection.Inject;
import com.unascribed.fabrication.util.BlazeFertilizerDispencerBehavior;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserBlock.class)
@EligibleIf(configAvailable="*.blaze_fertilizer")
public class MixinDispenserBlock {

	@Inject(at=@At("HEAD"), method="getBehaviorForItem(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/block/dispenser/DispenserBehavior;", cancellable=true)
	public void getBehaviorForItem(World world, ItemStack stack, CallbackInfoReturnable<DispenserBehavior> ci) {
		if (!FabConf.isEnabled("*.blaze_fertilizer")) return;
		if (stack.getItem() == Items.BLAZE_POWDER) {
			ci.setReturnValue(BlazeFertilizerDispencerBehavior.INSTANCE);
		}
	}

}
