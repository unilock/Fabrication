package com.unascribed.fabrication.mixin._general.sync;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Inject(at=@At("HEAD"), method="copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V")
	public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		if (this instanceof SetFabricationConfigAware) {
			if (oldPlayer instanceof SetFabricationConfigAware) {
				SetFabricationConfigAware us = (SetFabricationConfigAware)this;
				SetFabricationConfigAware them = (SetFabricationConfigAware)oldPlayer;
				us.fabrication$setReqVer(them.fabrication$getReqVer());
			}
		}
	}

}
