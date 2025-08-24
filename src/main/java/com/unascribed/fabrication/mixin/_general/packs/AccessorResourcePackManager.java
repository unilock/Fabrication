package com.unascribed.fabrication.mixin._general.packs;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ResourcePackManager.class)
@EligibleIf(envMatches = Env.CLIENT)
public interface AccessorResourcePackManager {
	@Accessor
	Set<ResourcePackProvider> getProviders();

	@Accessor
	void setProviders(Set<ResourcePackProvider> value);
}
