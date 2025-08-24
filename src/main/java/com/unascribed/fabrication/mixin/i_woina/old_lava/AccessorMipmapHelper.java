package com.unascribed.fabrication.mixin.i_woina.old_lava;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.texture.MipmapHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MipmapHelper.class)
@EligibleIf(configAvailable="*.old_lava", envMatches=Env.CLIENT)
public interface AccessorMipmapHelper {
	@Invoker("blend")
	static int fabrication$blend(int one, int two, int three, int four, boolean checkAlpha) {
		throw new AssertionError();
	}
}
