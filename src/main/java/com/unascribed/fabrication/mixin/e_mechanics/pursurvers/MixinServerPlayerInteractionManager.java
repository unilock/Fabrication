package com.unascribed.fabrication.mixin.e_mechanics.pursurvers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(ServerPlayerInteractionManager.class)
@EligibleIf(configAvailable="*.pursurvers")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public class MixinServerPlayerInteractionManager {

	@Shadow
	protected ServerWorld world;

	@FabInject(at=@At(value="INVOKE", target="net/minecraft/block/BlockState.onBlockBreakStart(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)V"),
			method="processBlockBreakingAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Lnet/minecraft/util/math/Direction;II)V")
	public void processBlockBreakingAction(BlockPos pos, Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
		if (FabConf.isEnabled("*.pursurvers") && action == Action.START_DESTROY_BLOCK) {
			BlockPos.Mutable mut = new BlockPos.Mutable();
			for (Direction d : Direction.values()) {
				mut.set(pos).move(d);
				BlockState bs = world.getBlockState(mut);
				if (bs.getBlock() == Blocks.OBSERVER) {
					if (bs.get(ObserverBlock.FACING) == d.getOpposite()) {
						for (Direction d2 : Direction.values()) {
							if (d2 == d.getOpposite()) continue;
							mut.set(pos).move(d).move(d2);
							if (world.getBlockState(mut).getBlock() == Blocks.PURPUR_BLOCK) {
								mut.set(pos).move(d);
								bs.scheduledTick(world, mut.toImmutable(), world.random);
							}
						}
					}
				}
			}
		}
	}


}
