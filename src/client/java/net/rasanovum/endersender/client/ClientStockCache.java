package net.rasanovum.endersender.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import java.util.HashMap;
import java.util.Map;

public class ClientStockCache {
    private static final Map<BlockPos, Map<Item, Integer>> SENDER_DATA = new HashMap<>();

    public static void update(BlockPos pos, Map<Item, Integer> data) {
        SENDER_DATA.remove(pos);
        SENDER_DATA.put(pos, data);
    }

    public static int getStockForNearbySender(BlockPos playerPos, Item item) {
        for (Map.Entry<BlockPos, Map<Item, Integer>> entry : SENDER_DATA.entrySet()) {
            if (entry.getKey().closerThan(playerPos, 64)) {
                int count = entry.getValue().getOrDefault(item, 0);
                if (count > 0) {
                    return count;
                }
            }
        }
        return 0;
    }
}
