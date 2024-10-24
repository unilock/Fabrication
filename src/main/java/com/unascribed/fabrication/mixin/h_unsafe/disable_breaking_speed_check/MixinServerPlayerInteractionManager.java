package com.unascribed.fabrication.mixin.h_unsafe.disable_breaking_speed_check;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabModifyConst;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.server.network.ServerPlayerInteractionManager;

@Mixin(ServerPlayerInteractionManager.class)
@EligibleIf(configAvailable="*.disable_breaking_speed_check")
public abstract class MixinServerPlayerInteractionManager {

	@FabModifyConst(constant=@Constant(floatValue=0.7F), method="processBlockBreakingAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Lnet/minecraft/util/math/Direction;II)V")
	private float disableBreakingSpeedCheck(float old) {
		if (!FabConf.isEnabled("*.disable_breaking_speed_check")) return old;
		return 0.1F;
	}

}
