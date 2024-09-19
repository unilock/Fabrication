package com.unascribed.fabrication.mixin._general.sync;

import com.unascribed.fabrication.EarlyAgnos;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.interfaces.ByteBufCustomPayloadReceiver;
import com.unascribed.fabrication.interfaces.SetCrawling;
import com.unascribed.fabrication.interfaces.SetItemDespawnAware;
import com.unascribed.fabrication.util.ByteBufCustomPayload;
import com.unascribed.fabrication.util.SwappingEnchants;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.features.FeatureHideArmor;
import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;
import com.unascribed.fabrication.loaders.LoaderFScript;
import com.unascribed.fabrication.support.OptionalFScript;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

@Mixin(ServerCommonNetworkHandler.class)
public class MixinServerCommonNetworkHandler implements ByteBufCustomPayloadReceiver {

	@Override
	public void fabrication$onCustomPayload(ByteBufCustomPayload payload) {
		Object self = this;
		if (!(self instanceof ServerPlayNetworkHandler)) return;
		ServerPlayerEntity player = ((ServerPlayNetworkHandler) self).getPlayer();
		if (!(payload instanceof ByteBufCustomPayload)) return;
		Identifier channel = payload.id();
		if (channel.getNamespace().equals("fabrication")) {
			if (channel.getPath().equals("config")) {
				PacketByteBuf recvdData = payload.buf();
				int id = recvdData.readVarInt();
				if (id == 0) {
					// hello
					int reqVer = 0;
					if (recvdData.isReadable(4)) reqVer = recvdData.readVarInt();
					if (player instanceof SetFabricationConfigAware) {
						((SetFabricationConfigAware) player).fabrication$setReqVer(reqVer);
						FabricationMod.sendConfigUpdate(player.server, null, player, reqVer);
						if (FabConf.isEnabled("*.hide_armor")) {
							FeatureHideArmor.sendSuppressedSlotsForSelf(player);
						}
					}
				} else if (id == 1 || id == 2) {
					// set
					if (player.hasPermissionLevel(2)) {
						String key = recvdData.readString(32767);
						if (FabConf.isValid(key)) {
							String value = recvdData.readString(32767);
							if (id == 1) FabConf.set(key, value);
							else FabConf.worldSet(key, value);
							if (FabricationMod.isAvailableFeature(key)) {
								FabricationMod.updateFeature(key, player.getWorld());
							}
							FabricationMod.sendConfigUpdate(player.server, key);
							fabrication$sendCommandFeedback(
									Text.translatable("chat.type.admin", player.getDisplayName(), Text.literal(key + " is now set to " + value))
									.formatted(Formatting.GRAY, Formatting.ITALIC));
						}
					}
				}
			} else if (channel.getPath().equals("fscript")) {
				PacketByteBuf recvdData = payload.buf();
				int id = recvdData.readVarInt();
				if(id == 0){
					// get
					String key = recvdData.readString(32767);
					if (FabConf.isValid(key)) {
						PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
						data.writeVarInt(0);
						data.writeString(LoaderFScript.get(key));
						player.networkHandler.sendPacket(new CustomPayloadS2CPacket(new ByteBufCustomPayload(Identifier.of("fabrication", "fscript"), data)));
					}
				}else if (id == 1) {
					// set
					if (player.hasPermissionLevel(2)) {
						String key = FabConf.remap(recvdData.readString(32767));
						if (FabConf.isValid(key) && FeaturesFile.get(key).fscript != null) {
							String value = recvdData.readString(32767);
							if (EarlyAgnos.isModLoaded("fscript") && OptionalFScript.set(key, value, player)) {
								fabrication$sendCommandFeedback(
										Text.translatable("chat.type.admin", player.getDisplayName(), Text.literal(key + " script is now set to " + value))
										.formatted(Formatting.GRAY, Formatting.ITALIC));
							}
						}
					}
				}else if (id == 2) {
					// unset
					if (player.hasPermissionLevel(2)) {
						String key = FabConf.remap(recvdData.readString(32767));
						if (FabConf.isValid(key) && FeaturesFile.get(key).fscript != null && EarlyAgnos.isModLoaded("fscript")) {
							OptionalFScript.restoreDefault(key);
							fabrication$sendCommandFeedback(
									Text.translatable("chat.type.admin", player.getDisplayName(), Text.literal(key + " script has been unset"))
									.formatted(Formatting.GRAY, Formatting.ITALIC));
						}
					}
				}else if (id == 3) {
					// TODO currently unused
					// reload
					if (player.hasPermissionLevel(2)) {
						LoaderFScript.reload();
						if (EarlyAgnos.isModLoaded("fscript")) OptionalFScript.reload();
						fabrication$sendCommandFeedback(
								Text.translatable("chat.type.admin", player.getDisplayName(), Text.literal(" scripts have been reloaded"))
								.formatted(Formatting.GRAY, Formatting.ITALIC));
					}
				}
				// TODO id 4 world local SET
			} else if (channel.getPath().equals("crawling") && FabConf.isEnabled("*.crawling")) {
				PacketByteBuf recvdData = payload.buf();
				boolean crawling = recvdData.readBoolean();
				if (player instanceof SetCrawling) {
					((SetCrawling)player).fabrication$setCrawling(crawling);
				}
			} else if (channel.getPath().equals("item_despawn") && FabConf.isEnabled("*.despawning_items_blink")) {
				if (player instanceof SetItemDespawnAware) {
					FabLog.debug("Enabling item despawn syncing for "+player.getName());
					((SetItemDespawnAware)player).fabrication$setItemDespawnAware(true);
				}
			} else if (channel.getPath().equals("swap_conflicting_enchants") && FabConf.isEnabled("*.swap_conflicting_enchants")) {
				PacketByteBuf recvdData = payload.buf();
				if (recvdData.readBoolean()) {
					ItemStack stack = player.getMainHandStack();
					World world = player.getWorld();
					if (stack != null && !stack.isEmpty() && world != null) {
						SwappingEnchants.swapEnchants(stack, world, player);
					}
				}
			}
		}
	}
	public void fabrication$sendCommandFeedback(Text text){
		Object self = this;
		if (!(self instanceof ServerPlayNetworkHandler)) return;
		ServerPlayerEntity player = ((ServerPlayNetworkHandler) self).getPlayer();
		if (player.server.getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
			for (ServerPlayerEntity spe : player.server.getPlayerManager().getPlayerList()) {
				if (player.server.getPlayerManager().isOperator(spe.getGameProfile())) {
					spe.sendMessage(text);
				}
			}
		}
		if (player.server.getGameRules().getBoolean(GameRules.LOG_ADMIN_COMMANDS)) {
			player.server.sendMessage(text);
		}
	}

}
