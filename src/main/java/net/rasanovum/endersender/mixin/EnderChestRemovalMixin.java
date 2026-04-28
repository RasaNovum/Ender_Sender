package net.rasanovum.endersender.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.rasanovum.endersender.EnderSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public abstract class EnderChestRemovalMixin {

    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true
    )
    private void removeOnPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving, CallbackInfo ci) {
        if (level == null || level.isClientSide) return;
        if (state.getBlock() instanceof EnderChestBlock) {

            boolean doEnderChestSpawning = level.getGameRules().getBoolean(EnderSender.SPAWN_ENDER_CHESTS);

            if (!doEnderChestSpawning) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                ci.cancel();
            }
        }
    }
}
