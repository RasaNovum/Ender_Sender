package net.rasanovum.endersender.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class ClientStockCache {
    private static final Map<BlockPos, SenderData> SENDER_DATA = new HashMap<>();
    public record SenderData(Map<Item, Integer> inventory, int radius) {}
    public static void update(BlockPos pos, Map<Item, Integer> inventory, int radius) {
        SENDER_DATA.remove(pos);
        SENDER_DATA.put(pos, new SenderData(inventory, radius));
    }

    public static int getStockForNearbySender(BlockPos playerPos, Item item) {
        for (Map.Entry<BlockPos, SenderData> entry : SENDER_DATA.entrySet()) {
            int rawRadius = entry.getValue().radius();
            int radius = Math.max(1, Math.min(rawRadius, 64));
            int radiusSqr = radius * radius;
            BlockPos senderPos = entry.getKey();

            if (senderPos.distSqr(playerPos) <= radiusSqr) {
                return entry.getValue().inventory().getOrDefault(item, 0);
            }
        }
        return 0;
    }
}
