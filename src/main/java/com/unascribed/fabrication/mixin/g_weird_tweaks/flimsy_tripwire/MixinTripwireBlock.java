package com.unascribed.fabrication.mixin.g_weird_tweaks.flimsy_tripwire;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.block.TripwireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TripwireBlock.class)
@EligibleIf(configAvailable="*.flimsy_tripwire")
public abstract class MixinTripwireBlock {

	@WrapWithCondition(at=@At(value="INVOKE", target="Lnet/minecraft/block/TripwireBlock;updatePowered(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"),
			method="scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V")
	private boolean fabrication$breakWire(TripwireBlock self, World world, BlockPos pos){
		if (!(FabConf.isEnabled("*.flimsy_tripwire") && world.getBlockState(pos).get(TripwireBlock.ATTACHED))) return true;
		world.breakBlock(pos, true);
		return false;
	}

}
