package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.feature.ResourcePackFeature;
import com.unascribed.fabrication.util.GrayscaleResourcePack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class FeatureFoliageCreepers extends ResourcePackFeature {
	public FeatureFoliageCreepers() {
		super("foliage_creepers");
	}
	@Override
	public void register(Consumer<ResourcePackProfile> consumer) {
		consumer.accept(ResourcePackProfile.of(MixinConfigPlugin.MOD_NAME + " grayscale", Text.literal("Internal " + MixinConfigPlugin.MOD_NAME + " grayscale resources"), true,
				new ResourcePackProfile.PackFactory() {
					@Override
					public ResourcePack open(String name) {
						return new GrayscaleResourcePack();
					}

					@Override
					public ResourcePack openWithOverlays(String name, ResourcePackProfile.Metadata metadata) {
						return open(name);
					}
				},
				new ResourcePackProfile.Metadata(Text.of(MixinConfigPlugin.MOD_NAME+" grayscale internal pack"), ResourcePackCompatibility.COMPATIBLE, FeatureSet.empty(), null),
			ResourcePackProfile.InsertionPosition.TOP, false, ResourcePackSource.BUILTIN));
	}
	@Override
	public void apply(World world) {
	}
	@Override
	public boolean undo(World world) {
		return true;
	}
}
