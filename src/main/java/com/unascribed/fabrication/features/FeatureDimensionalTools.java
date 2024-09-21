package com.unascribed.fabrication.features;

import java.text.NumberFormat;
import java.util.Set;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.EarlyAgnos;
import com.unascribed.fabrication.loaders.LoaderDimensionalTools;
import com.unascribed.fabrication.loaders.LoaderDimensionalTools.MohsIdentifier;
import com.unascribed.fabrication.loaders.LoaderDimensionalTools.NameSubstitution;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Language;

@EligibleIf(configAvailable="*.dimensional_tools")
public class FeatureDimensionalTools implements Feature {

	private static final NumberFormat format = NumberFormat.getNumberInstance();
	static {
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(2);
	}

	private boolean applied = false;
	private boolean active = false;

	@Override
	public void apply(MinecraftServer server) {
		active = true;
		if (!applied) {
			applied = true;
			if (EarlyAgnos.getCurrentEnv() == Env.CLIENT) {
				applyClient();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private void applyClient() {
		Agnos.runForTooltipRender((stack, lines) -> {
			if (active && !stack.isEmpty() && (stack.contains(DataComponentTypes.CUSTOM_DATA) && stack.get(DataComponentTypes.CUSTOM_DATA).getNbt().contains("fabrication:PartialDamage"))) {
				for (int i = 0; i < lines.size(); i++) {
					Object t = lines.get(i);
					double part = stack.get(DataComponentTypes.CUSTOM_DATA).getNbt().getDouble("fabrication:PartialDamage");
					if (t instanceof MutableText && ((MutableText) t).getContent() instanceof TranslatableTextContent) {
						if (((TranslatableTextContent) ((MutableText) t).getContent()).getKey().equals("item.durability")) {
							lines.set(i, Text.translatable("item.durability",
									format.format((stack.getMaxDamage() - stack.getDamage()) + (1 - part)), stack.getMaxDamage()));
						}
					}
				}
			}
		});
	}

	@Override
	public boolean undo(MinecraftServer server) {
		active = false;
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.dimensional_tools";
	}

	public static void handleCraft(CraftingRecipeInput inv, ItemStack stack) {
		if (LoaderDimensionalTools.isSubstitutable(stack.getItem())) {
			int count = 0;
			Multiset<MohsIdentifier> dimensions = null;
			for (int i = 0; i < inv.getSize(); i++) {
				ItemStack ing = inv.getStackInSlot(i);
				if (!ing.isEmpty()) {
					Set<MohsIdentifier> assoc = LoaderDimensionalTools.getAssociatedDimensionsForIngredient(ing);
					if (assoc.isEmpty()) return;
					if (assoc.contains(null)) continue;
					count++;
					if (dimensions == null) dimensions = HashMultiset.create();
					dimensions.addAll(assoc);
				}
			}
			if (dimensions != null) {
				Set<MohsIdentifier> finalDimensions = Sets.newHashSet();
				for (Multiset.Entry<MohsIdentifier> en : dimensions.entrySet()) {
					if (en.getCount() == count) {
						finalDimensions.add(en.getElement());
					}
				}
				if (!finalDimensions.isEmpty()) {
					String s = Language.getInstance().get(stack.getTranslationKey());
					for (NameSubstitution sub : LoaderDimensionalTools.nameSubstitutions) {
						if (finalDimensions.contains(new MohsIdentifier(true, sub.dimId)) ||
								finalDimensions.contains(new MohsIdentifier(false, sub.dimId))) {
							s = s.replace(sub.find, sub.replace);
						}
					}
					stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§f"+s));
					NbtList li = new NbtList();
					for (MohsIdentifier dim : finalDimensions) {
						li.add(NbtString.of(dim.toString()));
					}
					NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
					nbt.put("fabrication:HonoraryDimensions", li);
					stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
				}
			}
		}
	}

}
