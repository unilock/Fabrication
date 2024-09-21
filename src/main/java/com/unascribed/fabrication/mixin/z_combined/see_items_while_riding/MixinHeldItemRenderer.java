package com.unascribed.fabrication.mixin.z_combined.see_items_while_riding;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemRenderer.class)
@EligibleIf(anyConfigAvailable={"*.see_items_while_riding", "*.use_items_while_riding"}, envMatches=Env.CLIENT)
public class MixinHeldItemRenderer {
	@ModifyExpressionValue(method="updateHeldItems()V", at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z"))
	private boolean fabrication$heldItemView(boolean original){
		return !(FabConf.isEnabled("*.see_items_while_riding") || FabConf.isEnabled("*.use_items_while_riding")) && original;
	}
}
