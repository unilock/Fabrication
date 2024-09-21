package com.unascribed.fabrication.support;

import com.google.common.collect.Sets;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabricationResourcePack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackPosition;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProfile.InsertionPosition;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class FabricationDefaultResources implements ResourcePackProvider {

	public static void apply() {
		Set<ResourcePackProvider> providers = MinecraftClient.getInstance().getResourcePackManager().providers;
		try {
			providers.add(new FabricationDefaultResources());
		} catch (UnsupportedOperationException e) {
			FabLog.info("Injecting mutable resource pack provider set, as no-one else has yet.");
			providers = Sets.newHashSet(providers);
			MinecraftClient.getInstance().getResourcePackManager().providers = providers;
		}
	}
	@Override
	public void register(Consumer<ResourcePackProfile> consumer) {
		consumer.accept(
				ResourcePackProfile.create(new ResourcePackInfo(MixinConfigPlugin.MOD_NAME, Text.literal("Internal " + MixinConfigPlugin.MOD_NAME + " resources"), ResourcePackSource.BUILTIN, Optional.empty()),
						//this _cannot_ be a lambda or forgery throws a fit
						new ResourcePackProfile.PackFactory() {
							@Override
							public ResourcePack open(ResourcePackInfo info) {
								return new FabricationResourcePack("default");
							}

							@Override
							public ResourcePack openWithOverlays(ResourcePackInfo info, ResourcePackProfile.Metadata metadata) {
								return open(info);
							}
						}, ResourceType.CLIENT_RESOURCES, new ResourcePackPosition(true, InsertionPosition.TOP, false)));
	}

}
