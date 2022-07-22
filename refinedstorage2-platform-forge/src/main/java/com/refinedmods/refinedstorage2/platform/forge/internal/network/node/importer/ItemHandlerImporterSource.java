package com.refinedmods.refinedstorage2.platform.forge.internal.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nullable;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.ofItemStack;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toItemStack;

public class ItemHandlerImporterSource implements ImporterSource<ItemResource> {
    private final Level level;
    private final BlockPos pos;
    private final Direction direction;

    public ItemHandlerImporterSource(final Level level, final BlockPos pos, final Direction direction) {
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

    @Override
    public Iterator<ItemResource> getResources() {
        return getItemHandler().map(itemHandler -> (Iterator<ItemResource>) new AbstractIterator<ItemResource>() {
            private int index;

            @Nullable
            @Override
            protected ItemResource computeNext() {
                if (index > itemHandler.getSlots()) {
                    return endOfData();
                }
                for (; index < itemHandler.getSlots(); ++index) {
                    final ItemStack slot = itemHandler.getStackInSlot(index);
                    if (!slot.isEmpty()) {
                        index++;
                        return ofItemStack(slot);
                    }
                }
                return endOfData();
            }
        }).orElse(Collections.emptyListIterator());
    }

    @Override
    public long extract(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return getItemHandler().map(itemHandler -> {
            final ItemStack stack = toItemStack(resource, amount);
            long extracted = 0;
            for (int i = 0; i < itemHandler.getSlots() && extracted < amount; ++i) {
                final ItemStack slot = itemHandler.getStackInSlot(i);
                if (ItemStack.isSameItemSameTags(slot, stack)) {
                    extracted += itemHandler.extractItem(
                        i,
                        (int) amount - (int) extracted,
                        action == Action.SIMULATE
                    ).getCount();
                }
            }
            return extracted;
        }).orElse(0L);
    }

    @Override
    public long insert(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        final ItemStack stack = toItemStack(resource, amount);
        return getItemHandler().map(itemHandler -> {
            final ItemStack remainder = ItemHandlerHelper.insertItem(
                itemHandler,
                stack,
                action == Action.SIMULATE
            );
            return amount - remainder.getCount();
        }).orElse(0L);
    }
}
