package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.EarlyAgnos;
import com.unascribed.fabrication.interfaces.SetCrawling;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.util.ByteBufCustomPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@EligibleIf(configAvailable="*.crawling", envMatches=Env.CLIENT)
public class FeatureCrawling implements Feature {

	public static KeyBinding keybind;
	public static boolean forced = false;

	@Override
	public void apply(MinecraftServer minecraftServer, World world) {
		keybind = new KeyBinding("["+ MixinConfigPlugin.MOD_NAME+"] Crawl", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.movement") {
			@Override
			public void setPressed(boolean pressed) {
				if (EarlyAgnos.isForge() && pressed && MinecraftClient.getInstance().currentScreen != null) return;
				boolean send = !forced && isPressed() != pressed && MinecraftClient.getInstance().getNetworkHandler() != null;
				boolean state = pressed;
				boolean toggle = MinecraftClient.getInstance().options.getSneakToggled().getValue();
				if (toggle && !pressed) {
					send = false;
				}
				if (send) {
					if (toggle) {
						state = !((SetCrawling)MinecraftClient.getInstance().player).fabrication$isCrawling();
					}
					setCrawling(state, false);
				}
				super.setPressed(pressed);
			}
		};
		Agnos.registerKeyBinding(keybind);
	}

	public static void setCrawling(boolean state, boolean forced) {
		FeatureCrawling.forced = forced;
		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeBoolean(state);
		MinecraftClient.getInstance().getNetworkHandler().getConnection().send(new CustomPayloadC2SPacket(new ByteBufCustomPayload(Identifier.of("fabrication", "crawling"), data)));
		((SetCrawling)MinecraftClient.getInstance().player).fabrication$setCrawling(state);
	}

	@Override
	public boolean undo(MinecraftServer minecraftServer, World world) {
		return false;
	}

	@Override
	public String getConfigKey() {
		return "*.crawling";
	}

}
