package net.rasanovum.endersender.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.rasanovum.endersender.EnderSender;
import net.rasanovum.endersender.network.SenderSyncPacket;

import java.util.HashMap;
import java.util.Map;

public class EnderSenderClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(EnderSender.ENDER_SENDER_SCREEN_HANDLER, EnderSenderScreen::new);
		ClientPlayNetworking.registerGlobalReceiver(SenderSyncPacket.ID, (client, handler, buf, responseSender) -> {
			BlockPos pos = buf.readBlockPos();
			int mapSize = buf.readInt();
			Map<Item, Integer> stockMap = new HashMap<>();

			for (int i = 0; i < mapSize; i++) {
				Item item = buf.readById(BuiltInRegistries.ITEM);
				int count = buf.readInt();
				if (item != null) {
					stockMap.put(item, count);
				}

				client.execute(() -> ClientStockCache.update(pos, stockMap));
			}
		});
	}
}