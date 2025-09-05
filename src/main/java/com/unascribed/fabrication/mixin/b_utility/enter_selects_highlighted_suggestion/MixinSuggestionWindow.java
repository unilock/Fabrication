package com.unascribed.fabrication.mixin.b_utility.enter_selects_highlighted_suggestion;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;

@Mixin(ChatInputSuggestor.class)
@EligibleIf(configAvailable="*.enter_selects_highlighted_suggestion", envMatches=Env.CLIENT)
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public abstract class MixinSuggestionWindow {

	@Shadow
	@Nullable
	private ChatInputSuggestor.SuggestionWindow window;

	@ModifyExpressionValue(method="keyPressed(III)Z", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/screen/ChatInputSuggestor$SuggestionWindow;keyPressed(III)Z"))
	private boolean fabrication$enterAcceptsSuggestion(boolean original, int keyCode, int scanCode, int modifiers) {
		if (!FabConf.isEnabled("*.enter_selects_highlighted_suggestion")) return original;
		if ((keyCode == GLFW_KEY_ENTER || keyCode == GLFW_KEY_KP_ENTER) && this.window instanceof AccessorSuggestionWindow && !((AccessorSuggestionWindow) this.window).fabrication$getCompleated()){
			this.window.complete();
			return false;
		}
		return original;
	}


}
