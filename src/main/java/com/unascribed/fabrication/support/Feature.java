package com.unascribed.fabrication.support;

import net.minecraft.server.MinecraftServer;

public interface Feature {
	void apply(MinecraftServer world);
	boolean undo(MinecraftServer world);
	String getConfigKey();
}
