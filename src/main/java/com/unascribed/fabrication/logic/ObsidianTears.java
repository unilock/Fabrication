package com.unascribed.fabrication.logic;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStack.TooltipSection;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ObsidianTears {

	public static final ItemDispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior() {

		@Override
		protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			BlockPos pos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
			List<ServerPlayerEntity> players = pointer.world().getEntitiesByClass(ServerPlayerEntity.class, new Box(pos), EntityPredicates.EXCEPT_SPECTATOR);
			if (players.isEmpty()) {
				return stack;
			} else {
				ObsidianTears.setSpawnPoint(players.get(ThreadLocalRandom.current().nextInt(players.size())), stack);
				return new ItemStack(Items.GLASS_BOTTLE);
			}
		}

	};

	public static void setSpawnPoint(ServerPlayerEntity p, ItemStack stack) {
		World world = p.getWorld();
		RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(stack.getNbt().getString("fabrication:ObsidianTearsOriginDim")));
		BlockPos pos = BlockPos.fromLong(stack.getNbt().getLong("fabrication:ObsidianTearsOrigin"));
		if (world instanceof ServerWorld) {
			((ServerWorld)world).spawnParticles(ParticleTypes.FALLING_OBSIDIAN_TEAR, p.getPos().x, p.getPos().y+p.getBoundingBox().getLengthY()/2, p.getPos().z, 16,
					p.getBoundingBox().getLengthX()/2, p.getBoundingBox().getLengthY()/2, p.getBoundingBox().getLengthZ()/2,
					0.5);
		}
		p.setSpawnPoint(key, pos, p.getYaw(), false, true);
	}

	public static ItemStack createStack(World world, BlockPos blockPos) {
		ItemStack stack = PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.THICK);
		stack.setCustomName(Text.literal("§fObsidian Tears"));
		NbtCompound tag = stack.getOrCreateNbt();
		tag.putBoolean("fabrication:ObsidianTears", true);
		tag.putLong("fabrication:ObsidianTearsOrigin", blockPos.asLong());
		tag.putString("fabrication:ObsidianTearsOriginDim", world.getRegistryKey().getValue().toString());
		tag.putInt("CustomPotionColor", 0x540CB7);
		tag.putInt("HideFlags", TooltipSection.ADDITIONAL.getFlag());
		return stack;
	}

}
