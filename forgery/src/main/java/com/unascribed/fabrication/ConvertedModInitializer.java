package com.unascribed.fabrication;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public abstract class ConvertedModInitializer {

	public ConvertedModInitializer(ModContainer modContainer) {
		onInitialize();
		try {
			ModMenuAdapter mma = (ModMenuAdapter) Class.forName("com.unascribed.fabrication.ModMenuInitializer").getConstructor().newInstance();
			modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, modListScreen) -> {
				return mma.getModConfigScreenFactory().create(modListScreen);
			});
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public abstract void onInitialize();

}
