package com.unascribed.fabrication.support.feature;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.Feature;
import net.minecraft.server.MinecraftServer;

import java.util.function.Predicate;

public abstract class SimplePredicateFeature implements Feature {
	public final String key;
	public final Predicate<?> predicate;

	public SimplePredicateFeature(String key, Predicate<?> predicate) {
		this.key = key;
		this.predicate = predicate;
	}

	@Override
	public void apply(MinecraftServer server) {
		ConfigPredicates.put(key, predicate);
	}

	@Override
	public boolean undo(MinecraftServer server) {
		ConfigPredicates.remove(key);
		return true;
	}

}
