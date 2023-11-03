package com.refinedmods.refinedstorage2.platform.common.storagemonitor;

import com.refinedmods.refinedstorage2.platform.common.content.BlockConstants;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage2.platform.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage2.platform.common.support.network.NetworkNodeBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class StorageMonitorBlock extends AbstractDirectionalBlock<BiDirection> implements EntityBlock {
    private static final AbstractBlockEntityTicker<StorageMonitorBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getStorageMonitor);

    public StorageMonitorBlock() {
        super(BlockConstants.PROPERTIES.strength(2.5F, 6.0F));
    }

    @Override
    protected DirectionType<BiDirection> getDirectionType() {
        return BiDirectionType.INSTANCE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new StorageMonitorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }

    @Override
    public InteractionResult use(final BlockState state,
                                 final Level level,
                                 final BlockPos pos,
                                 final Player player,
                                 final InteractionHand hand,
                                 final BlockHitResult hit) {
        if (player.isCrouching()) {
            return super.use(state, level, pos, player, hand, hit);
        }
        final BiDirection direction = getDirection(state);
        if (direction == null || hit.getDirection() != direction.asDirection()) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof StorageMonitorBlockEntity storageMonitor) {
                storageMonitor.insert(player, hand);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void attack(final BlockState state, final Level level, final BlockPos pos, final Player player) {
        super.attack(state, level, pos, player);
        if (level.isClientSide()) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof StorageMonitorBlockEntity storageMonitor)) {
            return;
        }
        final BiDirection direction = getDirection(state);
        if (direction == null) {
            return;
        }
        final Direction hitDirection = getHitDirection(level, player);
        if (hitDirection != direction.asDirection()) {
            return;
        }
        storageMonitor.extract(player);
    }

    private Direction getHitDirection(final Level level, final Player player) {
        final Vec3 base = player.getEyePosition(1.0F);
        final Vec3 look = player.getLookAngle();
        final Vec3 target = base.add(look.x * 20, look.y * 20, look.z * 20);
        return level.clip(new ClipContext(
            base,
            target,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            player
        )).getDirection();
    }
}
