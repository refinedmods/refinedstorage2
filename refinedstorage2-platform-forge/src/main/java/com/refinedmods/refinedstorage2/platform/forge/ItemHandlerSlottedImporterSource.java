package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.importer.SlottedImporterSource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.ofItemStack;

public class ItemHandlerSlottedImporterSource implements SlottedImporterSource<ItemResource> {
    private final Level level;
    private final BlockPos pos;
    private final Direction direction;

    public ItemHandlerSlottedImporterSource(final Level level, final BlockPos pos, final Direction direction) {
        this.level = level;
        this.pos = pos;
        this.direction = direction;
    }

    private LazyOptional<IItemHandler> getItemHandler() {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return LazyOptional.empty();
        }
        return blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
    }

    @Nullable
    @Override
    public ItemResource getResource(final int slot) {
        return getItemHandler().map(itemHandler -> {
            final ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                return null;
            }
            return ofItemStack(stack);
        }).orElse(null);
    }

    @Override
    public int getSlots() {
        return getItemHandler().map(IItemHandler::getSlots).orElse(0);
    }

    @Override
    public long extract(final int slot, final long amount, final Action action) {
        return getItemHandler().map(
            itemHandler ->
                itemHandler.extractItem(slot, (int) amount, action == Action.SIMULATE).getCount()).orElse(0);
    }
}
