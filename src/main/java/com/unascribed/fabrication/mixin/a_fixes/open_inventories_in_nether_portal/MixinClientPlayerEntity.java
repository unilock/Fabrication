package com.unascribed.fabrication.mixin.a_fixes.open_inventories_in_nether_portal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
@EligibleIf(configAvailable="*.open_inventories_in_nether_portal", envMatches=Env.CLIENT)
public class MixinClientPlayerEntity {

	@ModifyExpressionValue(method="tickNausea(Z)V", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/screen/Screen;shouldPause()Z"))
	private boolean fabrication$preventClosingScreen(boolean old) {
		return FabConf.isEnabled("*.open_inventories_in_nether_portal") || old;
	}
}
