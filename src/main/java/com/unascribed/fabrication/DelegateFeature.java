package com.unascribed.fabrication;

import com.unascribed.fabrication.support.Feature;
import net.minecraft.server.MinecraftServer;

public class DelegateFeature implements Feature {

	private final Feature delegate;

	public DelegateFeature(String className) {
		try {
			delegate = (Feature)Class.forName(className).getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void apply(MinecraftServer server) {
		delegate.apply(server);
	}

	@Override
	public boolean undo(MinecraftServer server) {
		return delegate.undo(server);
	}

	@Override
	public String getConfigKey() {
		return delegate.getConfigKey();
	}

}
