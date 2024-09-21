package com.unascribed.fabrication.mixin.f_balance.anvil_damage_only_on_fall;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configAvailable="*.anvil_damage_only_on_fall", specialConditions=SpecialEligibility.NOT_FORGE)
public class MixinAnvilScreenHandler {

	@WrapOperation(at=@At(value="INVOKE", target="Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"),
			method="method_24922(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V")
	private static boolean fabrication$preventAnvilDmg(BlockState instance, TagKey tagKey, Operation<Boolean> original) {
		return !FabConf.isEnabled("*.anvil_damage_only_on_fall") && original.call(instance, tagKey);
	}

}
