package net.rasanovum.endersender;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.rasanovum.endersender.block.entity.EnderSenderBlockEntity;

public class BlockPlaceHandler {
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
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

                            if (!senderStack.isEmpty() && senderStack.is(stackInHand.getItem())) {
                                int countBefore = stackInHand.getCount();
                                InteractionResult result = stackInHand.useOn(new UseOnContext(player, hand, hitResult));

                                if (result.consumesAction()) {
                                    senderStack.shrink(1);
                                    sender.setChanged();

                                    stackInHand.setCount(countBefore);

                                    if (player instanceof ServerPlayer serverPlayer) {
                                        serverPlayer.containerMenu.broadcastChanges();
                                        // -2 = inventory constant, 0 = container ID
                                        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                                                0,
                                                serverPlayer.containerMenu.getStateId(),
                                                serverPlayer.getInventory().selected + 36,
                                                stackInHand
                                        ));
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