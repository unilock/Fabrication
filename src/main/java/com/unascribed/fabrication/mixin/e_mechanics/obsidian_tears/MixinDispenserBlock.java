package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.logic.ObsidianTears;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(DispenserBlock.class)
@EligibleIf(configAvailable="*.obsidian_tears")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public class MixinDispenserBlock {

	@FabInject(at=@At("HEAD"), method="getBehaviorForItem(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/block/dispenser/DispenserBehavior;",
			cancellable=true)
	public void getBehaviorForItem(World world, ItemStack stack, CallbackInfoReturnable<DispenserBehavior> ci) {
		if (!FabConf.isEnabled("*.obsidian_tears")) return;
		if (stack.getItem() == Items.POTION && stack.contains(DataComponentTypes.CUSTOM_DATA) && stack.get(DataComponentTypes.CUSTOM_DATA).getNbt().getBoolean("fabrication:ObsidianTears")) {
			ci.setReturnValue(ObsidianTears.DISPENSER_BEHAVIOR);
		}
	}

}
