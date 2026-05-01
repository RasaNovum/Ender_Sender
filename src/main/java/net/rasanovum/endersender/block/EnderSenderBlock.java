package net.rasanovum.endersender.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.rasanovum.endersender.EnderSender;
import net.rasanovum.endersender.EnderSenderScreenHandler;
import net.rasanovum.endersender.block.entity.EnderSenderBlockEntity;
import net.rasanovum.endersender.network.SenderSyncPacket;

public class EnderSenderBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 13.0D / 16.0D, 1.0D);

    public EnderSenderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : createTickerHelper(type, EnderSender.ENDER_SENDER_BE, EnderSenderBlockEntity::tick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnderSenderBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) return InteractionResult.SUCCESS;
        if (player.isCrouching()) return InteractionResult.PASS;

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof EnderSenderBlockEntity senderBE) {
            player.openMenu(new SimpleMenuProvider((id, inv, p) ->
                    new EnderSenderScreenHandler(id, inv, senderBE), Component.literal("Ender Sender")));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof EnderSenderBlockEntity sender) {
                if (world instanceof ServerLevel serverWorld) {
                    SenderSyncPacket.remove(serverWorld, sender);
                }
                Containers.dropContents(world, pos, sender);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }
}
