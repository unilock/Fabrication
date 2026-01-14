package com.unascribed.fabrication.mixin.h_unsafe.disable_moved_too_quickly;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.disable_moved_too_quickly")
public abstract class MixinServerPlayNetworkHandler {

	@ModifyExpressionValue(method={"onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V", "onVehicleMove(Lnet/minecraft/network/packet/c2s/play/VehicleMoveC2SPacket;)V"},
		at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerPlayNetworkHandler;isHost()Z"))
	private boolean disableMoveTooQuick(boolean old) {
		return FabConf.isEnabled("*.disable_moved_too_quickly") || old;
	}

}
