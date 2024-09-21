package com.unascribed.fabrication.mixin.d_minor_mechanics.launching_pistons;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.PistonBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonBlockEntity.class)
@EligibleIf(configAvailable="*.launching_pistons")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public class MixinPistonBlockEntity {

	@WrapOperation(at=@At(value="INVOKE", target="Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"),
			method="pushEntities(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;FLnet/minecraft/block/entity/PistonBlockEntity;)V")
	private static boolean fabrication$launchPlayer(BlockState instance, Block block, Operation<Boolean> original) {
		return original.call(instance, block) || FabConf.isEnabled("*.launching_pistons") && block == Blocks.SLIME_BLOCK;
	}

}
