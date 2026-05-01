package net.rasanovum.endersender;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.rasanovum.endersender.block.entity.EnderSenderBlockEntity;

public class BlockPlaceHandler {
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand != net.minecraft.world.InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (world.isClientSide || player.getAbilities().instabuild) return InteractionResult.PASS;

            if (!player.isCrouching() && world.getBlockState(hitResult.getBlockPos()).is(EnderSender.ENDER_SENDER_BLOCK)) {
                return InteractionResult.PASS;
            }

            int rawRadius = world.getGameRules().getInt(EnderSender.ENDER_SENDER_RADIUS);
            int radius = Math.max(1, Math.min(rawRadius, 64));
            BlockPos playerPos = player.blockPosition();

            ItemStack stackInHand = player.getItemInHand(hand);
            if (stackInHand.isEmpty()) return InteractionResult.PASS;

            for (BlockPos checkPos : BlockPos.betweenClosed(
                    playerPos.offset(-radius, -radius, -radius),
                    playerPos.offset(radius, radius, radius))) {

                if (world.getBlockEntity(checkPos) instanceof EnderSenderBlockEntity sender) {

                    if (checkPos.closerThan(player.blockPosition(), radius)) {
                        for (int i = 0; i < sender.getContainerSize(); i++) {
                            ItemStack senderStack = sender.getItem(i);
                            Item itemToTrack = stackInHand.getItem(); // to avoid the "1 item in hand" issue of tracking "Air" instead of the block.

                            if (!senderStack.isEmpty() && senderStack.is(itemToTrack)) {
                                int countBefore = stackInHand.getCount();
                                InteractionResult result = stackInHand.useOn(new UseOnContext(player, hand, hitResult));

                                if (result.consumesAction()) {
                                    senderStack.shrink(1);
                                    sender.markDirtyAndSync();
                                    stackInHand.setCount(countBefore);

                                    if (player instanceof ServerPlayer serverPlayer) {
                                        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                                                -2,
                                                0,
                                                serverPlayer.getInventory().selected,
                                                stackInHand
                                        ));
                                        serverPlayer.containerMenu.broadcastChanges();
                                    }
                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }
}