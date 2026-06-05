package net.rasanovum.endersender.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientStockCache {
    private static final Map<SenderKey, SenderData> SENDER_DATA = new HashMap<>();
    private record SenderKey(ResourceLocation dimension, BlockPos pos) {}
    public record SenderData(Map<Item, Integer> inventory, int radius) {}
    public static void update(ResourceLocation dimension, BlockPos pos, Map<Item, Integer> inventory, int radius) {
        SENDER_DATA.put(new SenderKey(dimension, pos), new SenderData(inventory, radius));
    }

    public static void remove(ResourceLocation dimension, BlockPos pos) {
        SENDER_DATA.remove(new SenderKey(dimension, pos));
    }

    public static void clear() {
        SENDER_DATA.clear();
    }

    public static Optional<Integer> getRadius(ResourceLocation dimension, BlockPos pos) {
        SenderData data = SENDER_DATA.get(new SenderKey(dimension, pos));
        return data == null ? Optional.empty() : Optional.of(Math.max(1, Math.min(data.radius(), 64)));
    }

    public static int getStockForNearbySender(ResourceLocation dimension, BlockPos playerPos, Item item) {
        for (Map.Entry<SenderKey, SenderData> entry : SENDER_DATA.entrySet()) {
            if (!entry.getKey().dimension().equals(dimension)) continue;

            int rawRadius = entry.getValue().radius();
            int radius = Math.max(1, Math.min(rawRadius, 64));
            int radiusSqr = radius * radius;
            BlockPos senderPos = entry.getKey().pos();

            if (senderPos.distSqr(playerPos) <= radiusSqr) {
                return entry.getValue().inventory().getOrDefault(item, 0);
            }
        }
        return 0;
    }
}
