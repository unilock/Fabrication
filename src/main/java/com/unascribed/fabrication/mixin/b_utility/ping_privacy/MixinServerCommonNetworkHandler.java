package com.unascribed.fabrication.mixin.b_utility.ping_privacy;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.PingPrivacyPersistentState;
import com.unascribed.fabrication.support.EligibleIf;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Mixin(ServerCommonNetworkHandler.class)
@EligibleIf(configAvailable="*.ping_privacy")
public class MixinServerCommonNetworkHandler {

	@Shadow @Final
	private MinecraftServer server;
	@Shadow @Final
	public ClientConnection connection;

	@Inject(at=@At("HEAD"), method="send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V")
	public void sendPacket(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
		if (FabConf.isEnabled("*.ping_privacy") && packet instanceof GameJoinS2CPacket) {
			SocketAddress addr = connection.getAddress();
			if (addr instanceof InetSocketAddress) {
				PingPrivacyPersistentState.get(server.getOverworld()).addKnownIp(((InetSocketAddress)addr).getAddress());
			}
		}
	}

}
