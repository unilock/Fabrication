package com.unascribed.fabrication.mixin._general.sync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.unascribed.fabrication.util.ByteBufCustomPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CustomPayloadS2CPacket.class)
public class MixinCustomPayloadS2CPacket {

	@WrapOperation(method="method_56460", at=@At(value="INVOKE", target="Lnet/minecraft/network/packet/UnknownCustomPayload;createCodec(Lnet/minecraft/util/Identifier;I)Lnet/minecraft/network/codec/PacketCodec;"))
	private static PacketCodec<PacketByteBuf, ? extends CustomPayload> oldPayloadConfiguration(Identifier id, int maxBytes, Operation<PacketCodec<PacketByteBuf, ? extends CustomPayload>> original){
		if ("fabrication".equals(id.getNamespace())) {
			return ByteBufCustomPayload.CODEC;
		}
		return original.call(id, maxBytes);
	}

	@WrapOperation(method="method_56461", at=@At(value="INVOKE", target="Lnet/minecraft/network/packet/UnknownCustomPayload;createCodec(Lnet/minecraft/util/Identifier;I)Lnet/minecraft/network/codec/PacketCodec;"))
	private static PacketCodec<PacketByteBuf, ? extends CustomPayload> oldPayloadPlay(Identifier id, int maxBytes, Operation<PacketCodec<PacketByteBuf, ? extends CustomPayload>> original){
		if ("fabrication".equals(id.getNamespace())) {
			return ByteBufCustomPayload.CODEC;
		}
		return original.call(id, maxBytes);
	}
}
