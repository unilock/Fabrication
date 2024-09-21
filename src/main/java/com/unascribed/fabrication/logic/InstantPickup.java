package com.unascribed.fabrication.logic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class InstantPickup {

	public static void slurp(World world, Box box, PlayerEntity breaker) {
		for (ItemEntity ie : world.getEntitiesByType(EntityType.ITEM, box, (e) -> ((Entity)e).age == 0)) {
			if (!ie.isAlive()) continue;
			int oldPickupDelay = ie.pickupDelay;
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
