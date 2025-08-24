package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Blocks;

@EligibleIf(configAvailable="*.flammable_cobwebs")
public class FeatureFlammableCobwebs implements Feature {

	@Override
	public void apply() {
		FlammableBlockRegistry.getInstance(Blocks.FIRE).add(Blocks.COBWEB, 60, 100);
	}

	@Override
	public boolean undo() {
		FlammableBlockRegistry.getInstance(Blocks.FIRE).remove(Blocks.COBWEB);
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.flammable_cobwebs";
	}

}
