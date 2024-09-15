package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.EarlyAgnos;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.world.World;

@EligibleIf(configAvailable="*.toggle_sprint", envMatches=Env.CLIENT)
public class FeatureToggleSprint implements Feature {

	public static KeyBinding keybind;
	public static boolean sprinting = false;

	@Override
	public void apply(World world) {
		keybind = new KeyBinding("["+ MixinConfigPlugin.MOD_NAME+"] Toggle/Hold Sprint", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.movement") {
			@Override
			public void setPressed(boolean pressed) {
				if (EarlyAgnos.isForge() && pressed && MinecraftClient.getInstance().currentScreen != null) return;
				if (!pressed && MinecraftClient.getInstance().getNetworkHandler() == null) {
					// reset() was probably called, so, reset
					sprinting = false;
				}
				if(MinecraftClient.getInstance().options.getSprintToggled().getValue()){
					sprinting = pressed;
				}else if (!isPressed() && pressed) {
					sprinting = !sprinting;
				}
				super.setPressed(pressed);
			}
		};
		Agnos.registerKeyBinding(keybind);
	}

	@Override
	public boolean undo(World world) {
		return false;
	}

	@Override
	public String getConfigKey() {
		return "*.toggle_sprint";
	}

}
