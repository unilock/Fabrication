package com.unascribed.fabrication.mixin._general.fapi;

import com.unascribed.fabrication.support.FabricationEvents;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(DataPackContents.class)
public class MixinDataPackContents {
	// TODO: @ModifyReturnValue would be better
	@FabInject(at=@At("RETURN"), method="reload", cancellable=true)
	private static void reload(ResourceManager manager, CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<DataPackContents>> cir) {
		cir.setReturnValue(cir.getReturnValue().whenComplete((dataPackContents, throwable) -> {
			if (dataPackContents != null && throwable == null) {
				FabricationEvents.reload(dataPackContents.getReloadableRegistries().getRegistryManager());
			}
		}));
	}
}
