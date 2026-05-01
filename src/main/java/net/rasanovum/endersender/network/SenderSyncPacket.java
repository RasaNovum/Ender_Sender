package net.rasanovum.endersender.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.rasanovum.endersender.block.entity.EnderSenderBlockEntity;

import java.util.HashMap;
import java.util.Map;

public class SenderSyncPacket {
    public static final ResourceLocation ID = new ResourceLocation("ender_sender", "sync_stock");

    public static void send(EnderSenderBlockEntity sender) {
        if (sender.getLevel() != null && !sender.getLevel().isClientSide) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(sender.getBlockPos());

            Map<Item, Integer> totals = new HashMap<>();
            for (int i = 0; i < sender.getContainerSize(); i++) {
                ItemStack stack = sender.getItem(i);
                if (!stack.isEmpty()) {
                    totals.put(stack.getItem(), totals.getOrDefault(stack.getItem(), 0) + stack.getCount());
                }
            }

            buf.writeInt(totals.size());
            totals.forEach((item, count) -> {
                buf.writeId(BuiltInRegistries.ITEM, item);
                buf.writeInt(count);
            });

            for (ServerPlayer player : PlayerLookup.around((ServerLevel) sender.getLevel(), sender.getBlockPos(), 64)) {
                ServerPlayNetworking.send(player, ID, buf);
            }
        }
    }
}
