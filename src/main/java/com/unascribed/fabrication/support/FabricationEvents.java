package com.unascribed.fabrication.support;

import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.Agnos;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FabricationEvents {
	private static final Set<Agnos.CommandRegistrationCallback> commands = new HashSet<>();
	private static final Map<String, Agnos.DynamicRegistryReloadCallback> reloaders = new HashMap<>();

	public static void addCommand(Agnos.CommandRegistrationCallback c) {
		commands.add(c);
	}
	public static void addReloader(String id, Agnos.DynamicRegistryReloadCallback c) {
		reloaders.put(id, c);
	}
	public static void removeReloader(String id) {
		reloaders.remove(id);
	}

	public static void commands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
		for (Agnos.CommandRegistrationCallback c : commands) {
			c.register(dispatcher, commandRegistryAccess, true);
		}
	}
	public static void reload(DynamicRegistryManager.Immutable registries) {
		for (Agnos.DynamicRegistryReloadCallback c : reloaders.values()) {
			c.reload(registries);
		}
	}
}
