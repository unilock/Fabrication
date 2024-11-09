package com.unascribed.fabrication;

import com.unascribed.fabrication.support.Env;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

import java.nio.file.Path;

// Forge implementation of Agnos. For linguistic and philosophical waffling, see the Fabric version.
public final class EarlyAgnos {

	public static Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	public static Env getCurrentEnv() {
		return FMLEnvironment.dist == Dist.CLIENT ? Env.CLIENT : Env.SERVER;
	}
	public static boolean isDev() {
		return "true".equals(System.getProperty("fml.deobfuscatedEnvironment"));
	}
	public static boolean isModLoaded(String modid) {
		if (modid.startsWith("fabric:")) return false;
		if (modid.startsWith("forge:")) modid = modid.substring(6);
		if (ModList.get() != null) try {
			return ModList.get().isLoaded(modid);
		} catch (java.lang.NullPointerException ignore) {}
		return FMLLoader.getLoadingModList().getModFileById(modid) != null;
	}

	public static String getModVersion() {
		return ModList.get().getModContainerById("fabrication").get().getModInfo().getVersion().toString();
	}

	public static boolean isForge() {
		return true;
	}

	public static String getLoaderVersion() {
		return NeoForgeVersion.getVersion();
	}
}
