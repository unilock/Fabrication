package com.unascribed.fabrication.mixin._general.fapi;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.FabricationEventsClient;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
@EligibleIf(specialConditions=SpecialEligibility.NOT_FORGE, envMatches=Env.CLIENT)
public class MixinInGameHud {

		@FabInject(method= "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at=@At(value="TAIL"))
		public void render(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
			FabricationEventsClient.hud(drawContext, tickCounter);
		}

}
