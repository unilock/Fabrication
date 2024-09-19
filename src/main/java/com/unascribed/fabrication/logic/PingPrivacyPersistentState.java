package com.unascribed.fabrication.logic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Maps;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class PingPrivacyPersistentState extends PersistentState {

	private static final String name = "fabrication_ping_privacy";

	private final Map<InetAddress, Long> knownIps = Maps.newHashMap();
	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	public static Type<PingPrivacyPersistentState> TYPE = new Type<>(PingPrivacyPersistentState::new, PingPrivacyPersistentState::fromNbt, DataFixTypes.OPTIONS);

	public static PingPrivacyPersistentState get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, name);
	}

	public void addKnownIp(InetAddress addr) {
		try {
			rwl.writeLock().lock();
			knownIps.put(addr, System.currentTimeMillis());
		} finally {
			rwl.writeLock().unlock();
		}
		markDirty();
	}

	public boolean isKnownAndRecent(InetAddress addr) {
		try {
			rwl.readLock().lock();
			return isRecent(knownIps.getOrDefault(addr, 0L));
		} finally {
			rwl.readLock().unlock();
		}
	}

	private static boolean isRecent(long time) {
		return System.currentTimeMillis()-time < TimeUnit.DAYS.toMillis(7);
	}

	public static PingPrivacyPersistentState fromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		PingPrivacyPersistentState rtrn = new PingPrivacyPersistentState();
		NbtList li = tag.getList("KnownIPs", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < li.size(); i++) {
			NbtCompound c = li.getCompound(i);
			long time = c.getLong("Time");
			if (!isRecent(time)) {
				// don't load it, it'll get dropped on next save
				continue;
			}
			InetAddress addr;
			try {
				addr = InetAddress.getByAddress(c.getByteArray("IP"));
			} catch (UnknownHostException e) {
				// ????????
				continue;
			}
			rtrn.knownIps.put(addr, time);
		}
		return rtrn;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		NbtList li = new NbtList();
		for (Map.Entry<InetAddress, Long> en : knownIps.entrySet()) {
			NbtCompound c = new NbtCompound();
			c.putByteArray("IP", en.getKey().getAddress());
			c.putLong("Time", en.getValue());
			li.add(c);
		}
		tag.put("KnownIPs", li);
		return tag;
	}
}
