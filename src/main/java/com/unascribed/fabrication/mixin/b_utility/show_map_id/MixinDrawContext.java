package com.unascribed.fabrication.mixin.b_utility.show_map_id;

import com.unascribed.fabrication.FabConf;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(DrawContext.class)
@EligibleIf(anyConfigAvailable={"*.show_map_id"}, envMatches=Env.CLIENT)
public abstract class MixinDrawContext {

	@Shadow
	@Final
	private MatrixStack matrices;

	@Shadow
	public abstract int drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow);

	@Shadow
	public abstract VertexConsumerProvider.Immediate getVertexConsumers();

	@FabInject(at=@At("TAIL"), method="drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
	public void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
		if (FabConf.isEnabled("*.show_map_id") && stack.getItem() == Items.FILLED_MAP){
			VertexConsumerProvider.Immediate vc = this.getVertexConsumers();
			String id = String.valueOf(stack.get(DataComponentTypes.MAP_ID).id());
			matrices.push();
			matrices.translate(0, 0, 200);
			drawText(renderer, id, (x + 19 - 2 - renderer.getWidth(id)), y, 16777215, true);
			matrices.pop();
			vc.draw();
		}
	}
}
