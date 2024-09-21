package com.unascribed.fabrication;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import com.unascribed.fabrication.interfaces.ByteBufCustomPayloadReceiver;
import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.ConfigValues;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.FabricationDefaultResources;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.unascribed.fabrication.util.ByteBufCustomPayload;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FabricationMod implements ModInitializer {

	private static final Map<String, Feature> features = Maps.newHashMap();
	private static final List<Feature> unconfigurableFeatures = Lists.newArrayList();
	private static final Set<String> enabledFeatures = Sets.newHashSet();

	public static final long LAUNCH_ID = ThreadLocalRandom.current().nextLong();

	public static SoundEvent LEVELUP_LONG;
	public static SoundEvent OOF;
	public static SoundEvent ABSORPTION_HURT;

	@Override
	public void onInitialize() {
		MixinConfigPlugin.loadComplete = true;
		for (String str : MixinConfigPlugin.discoverClassesInPackage("com.unascribed.fabrication.loaders", false)) {
			try {
				FabConf.introduce((ConfigLoader)Class.forName(str).newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
//		if (EarlyAgnos.isModLoaded("fscript")) OptionalFScript.reload();
		for (String s : MixinConfigPlugin.discoverClassesInPackage("com.unascribed.fabrication.features", false)) {
			try {
				Feature r = (Feature)Class.forName(s).newInstance();
				String key = FabConf.remap(r.getConfigKey());
				if (key == null || FabConf.isEnabled(key)) {
					try {
						r.apply(null);
						if (key != null) {
							enabledFeatures.add(key);
						}
					} catch (Throwable t) {
						featureError(r, t, "Unknown");
						continue;
					}
				}
				if (key != null) {
					features.put(key, r);
				} else {
					unconfigurableFeatures.add(r);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to initialize feature "+s, e);
			}
		}
		if (EarlyAgnos.getCurrentEnv() == Env.CLIENT) {
			FabricationClientCommands.registerCommands();
			LEVELUP_LONG = SoundEvent.of(Identifier.of("fabrication", "levelup_long"));
			OOF = SoundEvent.of(Identifier.of("fabrication", "oof"));
			ABSORPTION_HURT = SoundEvent.of(Identifier.of("fabrication", "absorption_hurt"));
			FabricationDefaultResources.apply();
		}
		PayloadTypeRegistry.playC2S().register(ByteBufCustomPayload.ID, ByteBufCustomPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ByteBufCustomPayload.ID, ByteBufCustomPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ByteBufCustomPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				((ByteBufCustomPayloadReceiver) context.player().networkHandler).fabrication$onCustomPayload(payload);
			});
		});

	}

	public static void featureError(Feature f, Throwable t, String reason) {
		featureError(f.getClass(), f.getConfigKey(), t, reason);
	}

	public static void featureError(Class<?> clazz, String configKey, Throwable t, String reason) {
		FabLog.debug("Original feature error", t);
		if (configKey == null) {
			FabLog.warn("Feature "+clazz.getName()+" failed to apply!");
		} else {
			FabLog.warn("Feature "+clazz.getName()+" failed to apply! Force-disabling "+configKey);
		}
		FabConf.addFailure(configKey, reason);
	}

	public static Identifier createIdWithCustomDefault(String namespace, String pathOrId) {
		if (pathOrId.contains(":")) {
			return Identifier.of(pathOrId);
		}
		return Identifier.of(namespace, pathOrId);
	}

	public static boolean isAvailableFeature(String configKey) {
		return features.containsKey(FabConf.remap(configKey));
	}

	public static boolean updateFeature(String configKey, MinecraftServer server) {
		configKey = FabConf.remap(configKey);
		boolean enabled = FabConf.isEnabled(configKey);
		if (enabledFeatures.contains(configKey) == enabled) return true;
		if (enabled) {
			features.get(configKey).apply(server);
			enabledFeatures.add(configKey);
			return true;
		} else {
			boolean b = features.get(configKey).undo(server);
			if (b) {
				enabledFeatures.remove(configKey);
			}
			return b;
		}
	}

	public static Set<PlayerAssociatedNetworkHandler> getTrackers(Entity entity) {
		ServerChunkManager cm = ((ServerWorld)entity.getWorld()).getChunkManager();
		ServerChunkLoadingManager sclm = cm.chunkLoadingManager;
		Int2ObjectMap<ServerChunkLoadingManager.EntityTracker> entityTrackers = sclm.entityTrackers;
		ServerChunkLoadingManager.EntityTracker tracker = entityTrackers.get(entity.getId());
		if (tracker == null) return Collections.emptySet();
		return tracker.listeners;
	}

	public static void sendToTrackersMatching(Entity entity, CustomPayloadS2CPacket pkt, Predicate<ServerPlayerEntity> predicate) {
		if (entity.getWorld().isClient) return;
		Set<PlayerAssociatedNetworkHandler> playersTracking = getTrackers(entity);
		if (entity instanceof ServerPlayerEntity) {
			ServerPlayerEntity spe = (ServerPlayerEntity)entity;
			if (predicate.test(spe)) {
				spe.networkHandler.sendPacket(pkt);
			}
		}
		for (PlayerAssociatedNetworkHandler etl : playersTracking) {
			ServerPlayerEntity spe = etl.getPlayer();
			if (predicate.test(spe)) {
				spe.networkHandler.sendPacket(pkt);
			}
		}
	}

	public static void sendConfigUpdate(MinecraftServer server, String key) {
		for (ServerPlayerEntity spe : server.getPlayerManager().getPlayerList()) {
			if (spe instanceof SetFabricationConfigAware && ((SetFabricationConfigAware)spe).fabrication$getReqVer() > 0) {
				sendConfigUpdate(server, key, spe, ((SetFabricationConfigAware) spe).fabrication$getReqVer());
			}
		}
	}

	public static void sendConfigUpdate(MinecraftServer server, String key, ServerPlayerEntity spe, int reqVer) {
		if (key != null && key.startsWith("general.category")) key = null;
		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		if (reqVer > 0) {
			data.writeVarInt(1);
		}
		if (key == null) {
			Map<String, ConfigValues.ResolvedFeature> trileans = Maps.newHashMap();
			Map<String, String> strings = Maps.newHashMap();
			for (String k : FabConf.getAllKeys()) {
				if (k.startsWith("general.category")) {
					strings.put(k, FabConf.getRawValue(k));
				} else {
					trileans.put(k, FabConf.getResolvedValue(k));
				}
			}
			data.writeVarInt(trileans.size());
			trileans.entrySet().forEach(en -> data.writeString(en.getKey()).writeByte(en.getValue().ordinal()));
			data.writeVarInt(strings.size());
			strings.entrySet().forEach(en -> data.writeString(en.getKey()).writeString(en.getValue()));
			data.writeLong(LAUNCH_ID);
		} else {
			data.writeVarInt(1);
			data.writeString(key);
			data.writeByte(FabConf.getResolvedValue(key).ordinal());
			data.writeVarInt(0);
			data.writeLong(LAUNCH_ID);
		}
		data.writeString(EarlyAgnos.getModVersion());
		data.writeVarInt(FabConf.getAllFailures().size());
		if (reqVer == 1) {
			for (Map.Entry<String, String> k : FabConf.getAllFailures().entrySet()) {
				data.writeString(k.getKey());
				data.writeString(k.getValue());
			}
		} else if (reqVer == 0) {
			for (String k : FabConf.getAllFailures().keySet()) {
				data.writeString(k);
			}
		}
		data.writeVarInt(FabConf.getAllBanned().size());
		for (String k : FabConf.getAllBanned()) {
			data.writeString(k);
		}
		CustomPayloadS2CPacket pkt = new CustomPayloadS2CPacket(new ByteBufCustomPayload(Identifier.of("fabrication", reqVer > 0 ? "config2" :"config"), data));
		spe.networkHandler.sendPacket(pkt);
	}

	private static final BlockPos.Mutable scratchpos1 = new BlockPos.Mutable();
	private static final BlockPos.Mutable scratchpos2 = new BlockPos.Mutable();
	private static final BlockPos.Mutable scratchpos3 = new BlockPos.Mutable();
	private static final BlockPos.Mutable scratchpos4 = new BlockPos.Mutable();

	public interface BlockScanCallback {
		boolean invoke(World w, BlockPos.Mutable bp, BlockPos.Mutable scratch, Direction dir);
	}

	public static void forAllAdjacentBlocks(Entity entity, BlockScanCallback callback) {
		World w = entity.getWorld();
		Box box = entity.getBoundingBox();
		if (!scanBlocks(w, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, Direction.DOWN, callback)) return;
		if (!scanBlocks(w, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, Direction.UP, callback)) return;

		if (!scanBlocks(w, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ, Direction.WEST, callback)) return;
		if (!scanBlocks(w, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, Direction.EAST, callback)) return;

		if (!scanBlocks(w, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, Direction.NORTH, callback)) return;
		if (!scanBlocks(w, box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, Direction.SOUTH, callback)) return;
	}

	private static boolean scanBlocks(World w, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Direction dir,
			BlockScanCallback callback) {
		BlockPos min = scratchpos1.set(minX+dir.getOffsetX(), minY+dir.getOffsetY(), minZ+dir.getOffsetZ());
		BlockPos max = scratchpos2.set(maxX+dir.getOffsetX(), maxY+dir.getOffsetY(), maxZ+dir.getOffsetZ());
		BlockPos.Mutable mut = scratchpos3;
		if (w.isRegionLoaded(min, max)) {
			for (int x = min.getX(); x <= max.getX(); x++) {
				for (int y = min.getY(); y <= max.getY(); y++) {
					for (int z = min.getZ(); z <= max.getZ(); z++) {
						mut.set(x, y, z);
						scratchpos4.set(mut);
						if (!callback.invoke(w, mut, scratchpos4, dir)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

}
