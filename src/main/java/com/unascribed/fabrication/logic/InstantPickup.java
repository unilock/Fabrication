package com.unascribed.fabrication.logic;

import com.unascribed.fabrication.mixin.g_weird_tweaks.instant_pickup.AccessorItemEntity;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.util.FakePlayerChecker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class InstantPickup {

	private static final Predicate<PlayerEntity> fabrication$instantPickupPredicate = ConfigPredicates.getFinalPredicate("*.instant_pickup");
	public static void slurp(World world, Box box, PlayerEntity breaker) {
		if (FakePlayerChecker.isFakePlayer(breaker)) return;
		if (!fabrication$instantPickupPredicate.test(breaker)) return;
		for (ItemEntity ie : world.getEntitiesByType(EntityType.ITEM, box, (e) -> e.age == 0)) {
			if (!ie.isAlive()) continue;
			int oldPickupDelay = ((AccessorItemEntity) ie).getPickupDelay();
			ie.setPickupDelay(0);
			ie.getCommandTags().add("interactic.ignore_auto_pickup_rule");
			ie.onPlayerCollision(breaker);
			if (ie.isAlive()) {
				ie.setPickupDelay(oldPickupDelay);
				ie.getCommandTags().remove("interactic.ignore_auto_pickup_rule");
			}
		}
	}

}
