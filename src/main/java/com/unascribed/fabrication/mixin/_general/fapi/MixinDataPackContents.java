package com.unascribed.fabrication.mixin._general.fapi;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.unascribed.fabrication.support.FabricationEvents;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(DataPackContents.class)
public class MixinDataPackContents {
	@ModifyReturnValue(at=@At("RETURN"), method="reload")
	private static CompletableFuture<DataPackContents> reload(CompletableFuture<DataPackContents> original, ResourceManager manager, CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel, Executor prepareExecutor, Executor applyExecutor) {
		return original.whenComplete((dataPackContents, throwable) -> {
			if (dataPackContents != null && throwable == null) {
				FabricationEvents.reload(dataPackContents.getReloadableRegistries().getRegistryManager());
			}
		});
	}
}
