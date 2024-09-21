package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;

@EligibleIf(configAvailable="*.faster_obsidian")
public class FeatureFasterObsidian implements Feature {

	private final ImmutableList<Block> BLOCKS = ImmutableList.of(
			Blocks.OBSIDIAN,
			Blocks.CRYING_OBSIDIAN,
			Blocks.ENDER_CHEST
			);

	@Override
	public void apply(MinecraftServer server) {
		amendHardness(1/3f);
	}

	@Override
	public boolean undo(MinecraftServer server) {
		amendHardness(3);
		return true;
	}

	private void amendHardness(float m) {
		for (Block b : BLOCKS) {
			for (BlockState bs : b.getStateManager().getStates()) {
				try {
					float base = bs.hardness;
					float nw = base*m;
					bs.hardness = nw;
				} catch (Exception e) {
					throw new RuntimeException("Can't update hardness", e);
				}
			}
		}
	}

	@Override
	public String getConfigKey() {
		return "*.faster_obsidian";
	}

}
