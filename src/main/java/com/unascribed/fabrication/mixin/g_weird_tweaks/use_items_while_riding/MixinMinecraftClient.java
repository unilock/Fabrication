package com.unascribed.fabrication.mixin.g_weird_tweaks.use_items_while_riding;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
@EligibleIf(configAvailable="*.use_items_while_riding", envMatches=Env.CLIENT)
public class MixinMinecraftClient {
	@ModifyExpressionValue(method="doItemUse()V", at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z"))
	private boolean fabrication$allowUsageWhileRiding$doItemUse(boolean old){
		return !FabConf.isEnabled("*.use_items_while_riding") && old;
	}

	@ModifyExpressionValue(method="doAttack()Z", at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z"))
	private boolean fabrication$allowUsageWhileRiding$doAttack(boolean old){
		return !FabConf.isEnabled("*.use_items_while_riding") && old;
	}
}
