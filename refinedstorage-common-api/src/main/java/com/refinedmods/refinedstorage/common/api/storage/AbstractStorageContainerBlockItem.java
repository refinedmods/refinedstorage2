package com.refinedmods.refinedstorage.common.api.storage;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public abstract class AbstractStorageContainerBlockItem extends BlockItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageContainerBlockItem.class);

    protected final StorageContainerItemHelper helper;

    protected AbstractStorageContainerBlockItem(
        final Block block,
        final Properties properties,
        final StorageContainerItemHelper helper
    ) {
        super(block, properties);
        this.helper = helper;
    }

    @Override
    public void inventoryTick(final ItemStack stack, final ServerLevel level, final Entity entity,
                              @Nullable final EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        helper.transferStorageIfNecessary(stack, level, entity, this::createStorage);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        return helper.tryDisassembly(
            level,
            player,
            stack,
            createPrimaryDisassemblyByproduct(stack.getCount()),
            createSecondaryDisassemblyByproduct(stack.getCount())
        );
    }

    @Override
    protected boolean updateCustomBlockEntityTag(final BlockPos pos,
                                                 final Level level,
                                                 @Nullable final Player player,
                                                 final ItemStack stack,
                                                 final BlockState state) {
        if (!level.isClientSide()) {
            updateBlockEntityTag(pos, level, stack);
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    private void updateBlockEntityTag(final BlockPos pos,
                                      final Level level,
                                      final ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof StorageBlockEntity blockEntity) {
            helper.transferToBlockEntity(stack, blockEntity);
        } else {
            LOGGER.warn("Storage could not be set, block entity does not exist yet at {}", pos);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final TooltipDisplay display,
                                final Consumer<Component> builder,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        final StorageRepository storageRepository = RefinedStorageApi.INSTANCE.getClientStorageRepository();
        helper.appendToTooltip(stack, storageRepository, builder, flag, this::formatAmount, getCapacity());
    }

    @Nullable
    protected abstract Long getCapacity();

    protected abstract String formatAmount(long amount);

    protected abstract SerializableStorage createStorage(StorageRepository storageRepository);

    protected abstract ItemStack createPrimaryDisassemblyByproduct(int count);

    @Nullable
    protected abstract ItemStack createSecondaryDisassemblyByproduct(int count);
}
