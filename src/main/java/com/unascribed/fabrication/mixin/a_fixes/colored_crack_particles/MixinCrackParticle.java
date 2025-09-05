package com.unascribed.fabrication.mixin.a_fixes.colored_crack_particles;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.particle.CrackParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrackParticle.class)
@EligibleIf(configAvailable="*.colored_crack_particles", envMatches=Env.CLIENT)
public abstract class MixinCrackParticle extends SpriteBillboardParticle {

	protected MixinCrackParticle(ClientWorld arg, double d, double e, double f) {
		super(arg, d, e, f);
	}

	@Inject(at=@At("TAIL"), method="<init>(Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/item/ItemStack;)V")
	public void construct(ClientWorld world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.colored_crack_particles")) return;
		if (stack.getItem() instanceof PotionItem) return;
		int c = ColorProviderRegistry.ITEM.get(stack.getItem()).getColor(stack, 0);
		setColor(((c>>16)&0xFF)/255f, ((c>>8)&0xFF)/255f, (c&0xFF)/255f);
	}

}
