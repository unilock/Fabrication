package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//lower priority fixes an origins issue on forge caused by them using a redirect
@Mixin(value=LivingEntityRenderer.class, priority=900)
@EligibleIf(configAvailable="*.foliage_creepers", envMatches=Env.CLIENT)
public abstract class MixinLivingEntityRenderer extends EntityRenderer<LivingEntity> {

	private static final Identifier fabrication$creeperTexture = Identifier.of("textures/entity/creeper/creeper.png");
	int fabrication$colorFoliageCreeper = -1;

	protected MixinLivingEntityRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Inject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void captureEntity(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
		if (FabConf.isEnabled("*.foliage_creepers") && fabrication$creeperTexture.equals(this.getTexture(livingEntity))) {
			fabrication$colorFoliageCreeper = livingEntity.getWorld().getColor(livingEntity.getBlockPos(), BiomeColors.FOLIAGE_COLOR);
		} else if (fabrication$colorFoliageCreeper != -1) {
			fabrication$colorFoliageCreeper = -1;
		}
	}

	@ModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index=4,
			method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public float changeColor4(float f) {
		if (fabrication$colorFoliageCreeper == -1) return f;
		return (fabrication$colorFoliageCreeper >> 16 & 255) / 255f;
	}
	@ModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index=5,
			method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public float changeColor5(float f) {
		if (fabrication$colorFoliageCreeper == -1) return f;
		return (fabrication$colorFoliageCreeper >> 8 & 255) / 255f;
	}
	@ModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index=6,
			method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public float changeColor6(float f) {
		if (fabrication$colorFoliageCreeper == -1) return f;
		return (fabrication$colorFoliageCreeper & 255) / 255f;
	}

	@ModifyVariable(at=@At("STORE"), method="getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;")
	public Identifier transformCreeperIdentifier(Identifier id){
		if (FabConf.isEnabled("*.foliage_creepers") && fabrication$creeperTexture.equals(id)) {
			return Identifier.of("fabrication_grayscale", id.getPath());
		}
		return id;
	}

}
