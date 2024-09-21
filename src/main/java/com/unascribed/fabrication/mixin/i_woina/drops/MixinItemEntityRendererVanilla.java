package com.unascribed.fabrication.mixin.i_woina.drops;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.Hijack;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.logic.WoinaDrops;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

@Mixin(ItemEntityRenderer.class)
@EligibleIf(configAvailable="*.classic_block_drops", envMatches=Env.CLIENT, modNotLoaded="forge:obfuscate")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public class MixinItemEntityRendererVanilla {

	@Hijack(target="Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
			method="renderStack(Lnet/minecraft/client/render/item/ItemRenderer;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/BakedModel;ZLnet/minecraft/util/math/random/Random;)V")
	private static boolean fabrication$renderClassicBlockDrops(ItemRenderer subject, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
		if (FabConf.isEnabled("*.classic_block_drops")) {
			WoinaDrops.interceptRender(subject, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
			return true;
		}
		return false;
	}

}
