package com.unascribed.fabrication.support.feature;

import com.google.common.collect.Lists;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.Feature;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.SaveProperties;

import java.util.Collection;

public class DataPackFeature implements Feature {
	private final String configKey;

	public boolean active = false;

	public DataPackFeature(String path) {
		this.configKey = "*."+path;
	}

	@Override
	public void apply(MinecraftServer server) {
		if (!active && (server == null || reload(server))) {
			active = true;
		}
	}

	@Override
	public boolean undo(MinecraftServer server) {
		if (active && (server == null || reload(server))) {
			active = false;
			return true;
		}
		return false;
	}

	@Override
	public String getConfigKey() {
		return configKey;
	}

	private boolean reload(MinecraftServer server) {
		ResourcePackManager resourcePackManager = server.getDataPackManager();
		SaveProperties saveProperties = server.getSaveProperties();
		Collection<String> collection = resourcePackManager.getEnabledIds();
		Collection<String> collection2 = findNewDataPacks(resourcePackManager, saveProperties, collection);
		tryReloadDataPacks(collection2, server);
		return true;
	}

	private static void tryReloadDataPacks(Collection<String> dataPacks, MinecraftServer server) {
		server.reloadResources(dataPacks).exceptionally(throwable -> {
			FabLog.warn("Failed to execute reload", throwable);
			return null;
		});
	}

	private static Collection<String> findNewDataPacks(ResourcePackManager dataPackManager, SaveProperties saveProperties, Collection<String> enabledDataPacks) {
		dataPackManager.scanPacks();
		Collection<String> collection = Lists.newArrayList(enabledDataPacks);
		Collection<String> collection2 = saveProperties.getDataConfiguration().dataPacks().getDisabled();

		for (String string : dataPackManager.getIds()) {
			if (!collection2.contains(string) && !collection.contains(string)) {
				collection.add(string);
			}
		}

		return collection;
	}
}
