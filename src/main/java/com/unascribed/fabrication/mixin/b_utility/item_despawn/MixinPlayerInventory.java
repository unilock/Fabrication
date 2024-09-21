package com.unascribed.fabrication.mixin.b_utility.item_despawn;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.unascribed.fabrication.interfaces.SetFromPlayerDeath;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerInventory.class)
@EligibleIf(configAvailable="*.item_despawn")
public abstract class MixinPlayerInventory {

	@ModifyReturnValue(at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;"),
			method="dropAll()V")
	private ItemEntity fabrication$tagDroppedItem(ItemEntity e) {
		if (e instanceof SetFromPlayerDeath) {
			((SetFromPlayerDeath)e).fabrication$setFromPlayerDeath(true);
		}
		return e;
	}


}
