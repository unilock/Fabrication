package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.server.MinecraftServer;

@EligibleIf(configAvailable="*.flammable_cobwebs")
public class FeatureFlammableCobwebs implements Feature {

	@Override
	public void apply(MinecraftServer server) {
		((FireBlock)Blocks.FIRE).registerFlammableBlock(Blocks.COBWEB, 60, 100);
	}

	@Override
	public boolean undo(MinecraftServer server) {
		((FireBlock)Blocks.FIRE).burnChances.remove(Blocks.COBWEB);
		((FireBlock)Blocks.FIRE).spreadChances.remove(Blocks.COBWEB);
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.flammable_cobwebs";
	}

}
