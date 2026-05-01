package net.rasanovum.endersender.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.rasanovum.endersender.EnderSender;
import net.rasanovum.endersender.network.SenderSyncPacket;
import net.rasanovum.endersender.util.ImplementedInventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EnderSenderBlockEntity extends BlockEntity implements ImplementedInventory {
    private final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    public EnderSenderBlockEntity(BlockPos pos, BlockState state) {
        super(EnderSender.ENDER_SENDER_BE, pos, state);
    }

    private final Set<UUID> playersInRange = new HashSet<>();

    public static void tick(Level world, BlockPos pos, BlockState state, EnderSenderBlockEntity be) {
        if (world.isClientSide || world.getGameTime() % 5 != 0) return;

        int rawRadius = world.getGameRules().getInt(EnderSender.ENDER_SENDER_RADIUS);
        int radius = Math.max(1, Math.min(rawRadius, 64)); // set max to 64

        boolean playEffects = world.getGameRules().getBoolean(EnderSender.DO_ENDER_SENDER_EFFECTS);
        AABB area = new AABB(pos).inflate(radius);
        List<Player> currentPlayers = world.getEntitiesOfClass(Player.class, area);
        List<UUID> currentUuids = currentPlayers.stream().map(Entity::getUUID).toList();

        if (world instanceof ServerLevel serverWorld) {
            // player enters
            for (Player player : currentPlayers) {
                if (!be.playersInRange.contains(player.getUUID())) {
                    if (playEffects) {
                        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.6f, 1.2f);

                        // particle line connecting sender and player
                        spawnConnectionLine(serverWorld, pos, player);
                    }
                    SenderSyncPacket.send(be);
                    be.playersInRange.add(player.getUUID());
                }
            }

            // player exits
            be.playersInRange.removeIf(uuid -> {
                if (!currentUuids.contains(uuid)) {
                    Player p = world.getPlayerByUUID(uuid);
                    if (p != null && playEffects) {
                        world.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.6f, 0.8f);

                        // burst at player on disconnect
                        serverWorld.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 15, 0.2, 0.2, 0.2, 0.1);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    public void markDirtyAndSync() {
        this.setChanged();
        if (!this.level.isClientSide) {
            SenderSyncPacket.send(this);
        }
    }

    public int countTotalItems(Item item) {
        int count = 0;
        for (ItemStack stack : this.items) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void spawnConnectionLine(ServerLevel world, BlockPos blockPos, Player player) {
        // centre of ender sender
        double startX = blockPos.getX() + 0.5;
        double startY = blockPos.getY() + 0.5;
        double startZ = blockPos.getZ() + 0.5;

        // centre of player
        double endX = player.getX();
        double endY = player.getY() + 1.0;
        double endZ = player.getZ();

        double diffX = endX - startX;
        double diffY = endY - startY;
        double diffZ = endZ - startZ;

        double distance = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
        int particles = (int) (distance * 2);

        for (int i = 0; i < particles; i++) {
            double ratio = (double) i / particles;
            double x = startX + (diffX * ratio);
            double y = startY + (diffY * ratio);
            double z = startZ + (diffZ * ratio);

            world.sendParticles(ParticleTypes.PORTAL, x, y, z, 1, 0, 0, 0, 0);
        }

        world.sendParticles(ParticleTypes.PORTAL, startX, startY, startZ, 20, 0.2, 0.2, 0.2, 0.1);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            SenderSyncPacket.send(this);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        ContainerHelper.saveAllItems(nbt, this.items);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.items.clear();
        ContainerHelper.loadAllItems(nbt, this.items);
    }

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            player.level().playSound(null, this.worldPosition, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.5f, 1.0f);
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (this.level != null && !this.level.isClientSide) {
            this.level.playSound(null, this.worldPosition, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.5f, 1.0f);
        }
    }

}
