package com.unascribed.fabrication.mixin.i_woina.blinking_drops;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.BlinkingDropsOverlay;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.FailOn;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
@EligibleIf(configAvailable="*.blinking_drops", envMatches=Env.CLIENT)
@FailOn(modLoaded="forge:obfuscate")
public class MixinItemRenderer {

	@ModifyReturnValue(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/RenderLayers;getItemLayer(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/client/render/RenderLayer;"),
			method="renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V")
	private RenderLayer fabrication$blink(RenderLayer original, ItemStack stack, boolean direct){
		if (!FabConf.isEnabled("*.blinking_drops") || stack.getItem() instanceof BlockItem || !BlinkingDropsOverlay.isDropped) return original;
		return BlinkingDropsOverlay.renderLayer;
	}

}
