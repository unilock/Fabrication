package com.unascribed.fabrication.mixin.f_balance.block_lit_mob_spawning;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HostileEntity.class)
@EligibleIf(configAvailable="*.block_lit_mob_spawning")
public class MixinHostileEntity {
	@WrapOperation(method="isSpawnDark(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z", at=@At(value="INVOKE", target="Lnet/minecraft/world/ServerWorldAccess;getLightLevel(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I"))
	private static int fabrication$oldDimMobSpawning(ServerWorldAccess world, LightType type, BlockPos blockPos, Operation<Integer> original) {
		if (FabConf.isEnabled("*.block_lit_mob_spawning") && type == LightType.BLOCK) {
			return 0;
		}
		return original.call(world, type, blockPos);
	}
}
