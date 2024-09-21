package com.unascribed.fabrication.mixin.d_minor_mechanics.infibows;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value=CrossbowItem.class, priority=1001)
@EligibleIf(anyConfigAvailable="*.infibows")
public class MixinCrossbowItem {

	@WrapOperation(at= @At(value="INVOKE", target="Ljava/util/List;isEmpty()Z"), method="loadProjectiles(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)Z")
	private static boolean fabrication$modifyCreativeModeLoadProjectile(List<ItemStack> instance, Operation<Boolean> original, LivingEntity shooter, ItemStack crossbow) {
		if (FabConf.isAnyEnabled("*.infibows") && EnchantmentHelperHelper.getLevel(shooter.getRegistryManager(), Enchantments.INFINITY, crossbow) > 0 && original.call(instance)) {
			instance.add(Items.ARROW.getDefaultStack());
		}
		return original.call(instance);
	}

}
