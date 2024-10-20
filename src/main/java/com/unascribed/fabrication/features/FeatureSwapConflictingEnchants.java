package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.EarlyAgnos;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

@EligibleIf(configAvailable="*.swap_conflicting_enchants", envMatches=Env.CLIENT)
public class FeatureSwapConflictingEnchants implements Feature {

	private boolean applied = false;

	@Override
	public void apply(MinecraftServer minecraftServer, World world) {
		if (!applied) {
			applied = true;
			if (EarlyAgnos.getCurrentEnv() == Env.CLIENT) {
				applyClient(world);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private void applyClient(World world) {
		Agnos.runForTooltipRender((stack, lines) -> {
			if (!stack.isEmpty() && stack.contains(DataComponentTypes.CUSTOM_DATA)) {
				//TODO?
				//if ((FabRefl.ItemStack_getHideFlags(stack) & ItemStack.TooltipSection.ENCHANTMENTS.getFlag()) != 0) return;
				NbtCompound lTag = stack.get(DataComponentTypes.CUSTOM_DATA).getNbt().getCompound("fabrication#conflictingEnchants");
				if (lTag == null || lTag.isEmpty()) return;
				int ii=0;
				for (int i=0;i<lines.size();i++) {
					Text o = lines.get(i);
					if (o instanceof MutableText) {
						TextContent tc = o.getContent();
						if (!(tc instanceof TranslatableTextContent)) continue;
						String ke = ((TranslatableTextContent) tc).getKey();
						if (ke != null && ke.startsWith("enchantment")) {
							ii=i;
						}
					}
				}
				ii++;
				for (String key : lTag.getKeys()) {
					Optional<RegistryEntry.Reference<Enchantment>> e = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Identifier.tryParse(key));
					if (e.isPresent()) {
						Text o = Enchantment.getName(e.get(), lTag.getInt(key));
						if (o instanceof MutableText) {
							((MutableText) o).formatted(Formatting.DARK_GRAY);
						}
						lines.add(ii, o);
					}
				}
			}
		});
	}

	@Override
	public boolean undo(MinecraftServer minecraftServer, World world) {
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.swap_conflicting_enchants";
	}


}
