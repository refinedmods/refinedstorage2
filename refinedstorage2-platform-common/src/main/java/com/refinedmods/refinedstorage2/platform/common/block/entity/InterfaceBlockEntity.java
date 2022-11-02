package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.iface.InterfaceExportState;
import com.refinedmods.refinedstorage2.api.network.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannel;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.InterfaceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemFilteredResource;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

// TODO: Interfaces will probably be stealing from each other.
public class InterfaceBlockEntity
    extends AbstractInternalNetworkNodeContainerBlockEntity<InterfaceNetworkNode<ItemResource>>
    implements InterfaceExportState<ItemResource>, ExtendedMenuProvider, BlockEntityWithDrops {
    private static final String TAG_EXPORT_ITEMS = "ei";
    private static final int EXPORT_SLOTS = 9;

    private final FilterWithFuzzyMode filter;
    private final SimpleContainer exportedItems = new SimpleContainer(EXPORT_SLOTS);

    public InterfaceBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getInterface(),
            pos,
            state,
            new InterfaceNetworkNode<>(
                Platform.INSTANCE.getConfig().getInterface().getEnergyUsage(),
                StorageChannelTypes.ITEM
            )
        );
        getNode().setExportState(this);
        getNode().setTransferQuota(64);
        this.filter = new FilterWithFuzzyMode(
            ItemResourceType.INSTANCE,
            this::setChanged,
            value -> {
            },
            value -> {
            },
            EXPORT_SLOTS,
            64
        );
        this.exportedItems.addListener(c -> setChanged());
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_EXPORT_ITEMS, exportedItems.createTag());
        filter.save(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_EXPORT_ITEMS)) {
            exportedItems.fromTag(tag.getList(TAG_EXPORT_ITEMS, Tag.TAG_COMPOUND));
        }
        filter.load(tag);
        super.load(tag);
    }

    public boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    public void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
    }

    public SimpleContainer getExportedItems() {
        return exportedItems;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new InterfaceContainerMenu(syncId, player, this, filter.getFilterContainer(), exportedItems);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        filter.getFilterContainer().writeToUpdatePacket(buf);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "interface");
    }

    @Override
    public int getSlots() {
        return filter.getFilterContainer().size();
    }

    @Override
    public Collection<ItemResource> expandExportCandidates(final StorageChannel<ItemResource> storageChannel,
                                                           final ItemResource resource) {
        if (!filter.isFuzzyMode()) {
            return Collections.singletonList(resource);
        }
        if (!(storageChannel instanceof FuzzyStorageChannel<ItemResource> fuzzyStorageChannel)) {
            return Collections.singletonList(resource);
        }
        return fuzzyStorageChannel
            .getFuzzy(resource)
            .stream()
            .map(ResourceAmount::getResource)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isCurrentlyExportedResourceValid(final ItemResource want, final ItemResource got) {
        if (!filter.isFuzzyMode()) {
            return got.equals(want);
        }
        final ItemResource normalizedGot = got.normalize();
        final ItemResource normalizedWant = want.normalize();
        return normalizedGot.equals(normalizedWant);
    }

    @Nullable
    @Override
    public ItemResource getRequestedResource(final int index) {
        final FilteredResource filteredResource = filter.getFilterContainer().get(index);
        if (!(filteredResource instanceof ItemFilteredResource itemFilteredResource)) {
            return null;
        }
        return itemFilteredResource.value();
    }

    @Override
    public long getRequestedResourceAmount(final int index) {
        final FilteredResource filteredResource = filter.getFilterContainer().get(index);
        if (filteredResource == null) {
            return 0;
        }
        return filteredResource.getAmount();
    }

    @Nullable
    @Override
    public ItemResource getCurrentlyExportedResource(final int index) {
        final ItemStack current = exportedItems.getItem(index);
        if (current.isEmpty()) {
            return null;
        }
        return new ItemResource(current.getItem(), current.getTag());
    }

    @Override
    public long getCurrentlyExportedResourceAmount(final int index) {
        return exportedItems.getItem(index).getCount();
    }

    @Override
    public void setCurrentlyExported(final int index, final ItemResource resource, final long amount) {
        final ItemStack stack = resource.toItemStack();
        stack.setCount((int) amount);
        exportedItems.setItem(index, stack);
    }

    @Override
    public void decrementCurrentlyExportedAmount(final int index, final long amount) {
        exportedItems.getItem(index).shrink((int) amount);
        setChanged();
    }

    @Override
    public void incrementCurrentlyExportedAmount(final int index, final long amount) {
        exportedItems.getItem(index).grow((int) amount);
        setChanged();
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < exportedItems.getContainerSize(); ++i) {
            drops.add(exportedItems.getItem(i));
        }
        return drops;
    }
}
