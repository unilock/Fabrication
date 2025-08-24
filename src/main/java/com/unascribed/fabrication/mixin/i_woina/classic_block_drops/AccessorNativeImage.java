package com.unascribed.fabrication.mixin.i_woina.classic_block_drops;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NativeImage.class)
@EligibleIf(envMatches=Env.CLIENT, configAvailable="*.classic_block_drops")
public interface AccessorNativeImage {
	@Invoker("<init>")
	static NativeImage fabrication$new(NativeImage.Format format, int width, int height, boolean useStb, long pointer) {
		throw new AssertionError();
	}
}
