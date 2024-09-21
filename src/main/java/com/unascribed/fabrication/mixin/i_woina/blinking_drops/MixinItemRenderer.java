package com.unascribed.fabrication.mixin.i_woina.blinking_drops;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.BlinkingDropsOverlay;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemRenderer.class)
@EligibleIf(configAvailable="*.blinking_drops", envMatches=Env.CLIENT)
@FailOn(modLoaded="forge:obfuscate")
public class MixinItemRenderer {

	@Hijack(target="Lnet/minecraft/client/render/RenderLayers;getItemLayer(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/client/render/RenderLayer;",
			method="renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V")
	private static HijackReturn fabrication$blink(ItemStack stack, boolean direct){
		if (!FabConf.isEnabled("*.blinking_drops") || stack.getItem() instanceof BlockItem || !BlinkingDropsOverlay.isDropped) return null;
		return BlinkingDropsOverlay.renderLayer;
	}

}
