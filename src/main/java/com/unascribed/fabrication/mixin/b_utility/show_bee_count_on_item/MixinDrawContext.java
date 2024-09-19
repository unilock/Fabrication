package com.unascribed.fabrication.mixin.b_utility.show_bee_count_on_item;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import javax.xml.crypto.Data;

@Mixin(DrawContext.class)
@EligibleIf(configAvailable="*.show_bee_count_on_item", envMatches=Env.CLIENT)
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
		if (!(FabConf.isEnabled("*.show_bee_count_on_item") && stack.contains(DataComponentTypes.BLOCK_ENTITY_DATA))) return;
		NbtCompound tag = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA).copyNbt();
		if (tag == null || !tag.contains("Bees", NbtElement.LIST_TYPE)) return;

		VertexConsumerProvider.Immediate vc = this.getVertexConsumers();
		String count = String.valueOf(((NbtList)tag.get("Bees")).size());
		matrices.push();
		matrices.translate(0, 0, 200);
		this.drawText(renderer, count, (x + 19 - 2 - renderer.getWidth(count)), (y), 16777045, true);
		matrices.pop();
		vc.draw();
	}
}
