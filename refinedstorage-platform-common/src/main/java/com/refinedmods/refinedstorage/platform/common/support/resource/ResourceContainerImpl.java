package com.refinedmods.refinedstorage.platform.common.support.resource;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.platform.common.util.MathUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToLongFunction;
import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ResourceContainerImpl implements ResourceContainer {
    private final ResourceContainerSlot[] slots;
    private final ToLongFunction<ResourceKey> maxAmountProvider;
    private final ResourceFactory primaryResourceFactory;
    private final Set<ResourceFactory> alternativeResourceFactories;

    @Nullable
    private Runnable listener;

    public ResourceContainerImpl(final int size,
                                 final ToLongFunction<ResourceKey> maxAmountProvider,
                                 final ResourceFactory primaryResourceFactory,
                                 final Set<ResourceFactory> alternativeResourceFactories) {
        this.slots = new ResourceContainerSlot[size];
        this.maxAmountProvider = maxAmountProvider;
        this.primaryResourceFactory = primaryResourceFactory;
        this.alternativeResourceFactories = alternativeResourceFactories;
    }

    @Override
    public void setListener(@Nullable final Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void change(final int index, final ItemStack stack, final boolean tryAlternatives) {
        if (tryAlternatives) {
            for (final ResourceFactory resourceFactory : alternativeResourceFactories) {
                final var result = resourceFactory.create(stack);
                if (result.isPresent()) {
                    set(index, result.get());
                    return;
                }
            }
        }
        primaryResourceFactory.create(stack).ifPresentOrElse(
            resource -> set(index, resource),
            () -> remove(index)
        );
    }

    @Override
    public void set(final int index, final ResourceAmount resourceAmount) {
        setSilently(index, resourceAmount);
        changed();
    }

    private void setSilently(final int index, final ResourceAmount resourceAmount) {
        slots[index] = new ResourceContainerSlot(resourceAmount);
    }

    @Override
    public boolean isValid(final ResourceKey resource) {
        if (primaryResourceFactory.isValid(resource)) {
            return true;
        }
        for (final ResourceFactory resourceFactory : alternativeResourceFactories) {
            if (resourceFactory.isValid(resource)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getAmount(final int index) {
        final ResourceContainerSlot slot = slots[index];
        if (slot == null) {
            return 0;
        }
        return slot.getResourceAmount().getAmount();
    }

    @Override
    public void grow(final int index, final long amount) {
        CoreValidations.validateNotNegative(amount, "Amount to grow cannot be negative.");
        setAmount(index, getAmount(index) + amount);
    }

    @Override
    public void shrink(final int index, final long amount) {
        CoreValidations.validateNotNegative(amount, "Amount to shrink cannot be negative.");
        setAmount(index, getAmount(index) - amount);
    }

    @Override
    public void setAmount(final int index, final long amount) {
        final ResourceContainerSlot slot = slots[index];
        if (slot == null) {
            return;
        }
        final long newAmount = MathUtil.clamp(amount, 0, getMaxAmount(slot.getResourceAmount().getResource()));
        if (newAmount == 0) {
            remove(index);
        } else {
            slots[index] = slot.withAmount(newAmount);
        }
        changed();
    }

    @Override
    public long getMaxAmount(final ResourceKey resource) {
        return maxAmountProvider.applyAsLong(resource);
    }

    @Override
    public void remove(final int index) {
        removeSilently(index);
        changed();
    }

    private void removeSilently(final int index) {
        slots[index] = null;
    }

    @Override
    public int size() {
        return slots.length;
    }

    @Override
    @Nullable
    public ResourceAmount get(final int index) {
        final ResourceContainerSlot slot = slots[index];
        if (slot == null) {
            return null;
        }
        return slot.getResourceAmount();
    }

    @Nullable
    @Override
    public PlatformResourceKey getResource(final int index) {
        final ResourceContainerSlot slot = slots[index];
        if (slot == null) {
            return null;
        }
        return slot.getPlatformResource();
    }

    @Override
    public ItemStack getStackRepresentation(final int index) {
        final ResourceContainerSlot slot = slots[index];
        if (slot == null) {
            return ItemStack.EMPTY;
        }
        return slot.getStackRepresentation();
    }

    @Override
    public Set<ResourceKey> getUniqueResources() {
        final Set<ResourceKey> result = new HashSet<>();
        for (int i = 0; i < size(); ++i) {
            final ResourceContainerSlot slot = slots[i];
            if (slot == null) {
                continue;
            }
            result.add(slot.getResourceAmount().getResource());
        }
        return result;
    }

    @Override
    public List<ResourceKey> getResources() {
        final List<ResourceKey> result = new ArrayList<>();
        for (int i = 0; i < size(); ++i) {
            final ResourceContainerSlot slot = slots[i];
            if (slot == null) {
                continue;
            }
            result.add(slot.getResourceAmount().getResource());
        }
        return result;
    }

    @Override
    public CompoundTag toTag(final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            final ResourceContainerSlot slot = slots[i];
            if (slot == null) {
                continue;
            }
            addToTag(tag, i, slot, provider);
        }
        return tag;
    }

    private void addToTag(final CompoundTag tag,
                          final int index,
                          final ResourceContainerSlot slot,
                          final HolderLookup.Provider provider) {
        final Tag serialized = ResourceCodecs.AMOUNT_CODEC.encode(
            slot.getResourceAmount(),
            provider.createSerializationContext(NbtOps.INSTANCE),
            new CompoundTag()
        ).getOrThrow();
        tag.put("s" + index, serialized);
    }

    @Override
    public void fromTag(final CompoundTag tag, final HolderLookup.Provider provider) {
        for (int i = 0; i < size(); ++i) {
            final String key = "s" + i;
            if (!tag.contains(key)) {
                removeSilently(i);
                continue;
            }
            final CompoundTag item = tag.getCompound(key);
            fromTag(i, item, provider);
        }
    }

    private void fromTag(final int index, final CompoundTag tag, final HolderLookup.Provider provider) {
        final ResourceAmount resourceAmount = ResourceCodecs.AMOUNT_CODEC.decode(
            provider.createSerializationContext(NbtOps.INSTANCE),
            tag
        ).getOrThrow().getFirst();
        setSilently(index, resourceAmount);
    }

    @Override
    public ResourceFactory getPrimaryResourceFactory() {
        return primaryResourceFactory;
    }

    @Override
    public Set<ResourceFactory> getAlternativeResourceFactories() {
        return alternativeResourceFactories;
    }

    private void changed() {
        if (listener != null) {
            listener.run();
        }
    }

    @Override
    public Container toItemContainer() {
        return new AbstractResourceContainerContainerAdapter(this) {
            @Override
            public void setChanged() {
                changed();
            }
        };
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action) {
        if (!(resource instanceof PlatformResourceKey platformResource)) {
            return 0L;
        }
        long remainder = amount;
        for (int i = 0; i < size(); ++i) {
            final ResourceAmount slot = get(i);
            if (slot == null) {
                remainder -= insertIntoEmptySlot(i, platformResource, action, remainder);
            } else if (slot.getResource().equals(resource)) {
                remainder -= insertIntoExistingSlot(
                    i,
                    platformResource,
                    action,
                    remainder,
                    slot
                );
            }
            if (remainder == 0) {
                break;
            }
        }
        return amount - remainder;
    }

    private long insertIntoEmptySlot(final int slotIndex,
                                     final PlatformResourceKey resource,
                                     final Action action,
                                     final long amount) {
        final long inserted = Math.min(resource.getInterfaceExportLimit(), amount);
        if (action == Action.EXECUTE) {
            set(slotIndex, new ResourceAmount(resource, inserted));
        }
        return inserted;
    }

    private long insertIntoExistingSlot(final int slotIndex,
                                        final PlatformResourceKey resource,
                                        final Action action,
                                        final long amount,
                                        final ResourceAmount existing) {
        final long spaceRemaining = resource.getInterfaceExportLimit() - existing.getAmount();
        final long inserted = Math.min(spaceRemaining, amount);
        if (action == Action.EXECUTE) {
            grow(slotIndex, inserted);
        }
        return inserted;
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action) {
        long extracted = 0;
        for (int i = 0; i < size(); ++i) {
            final ResourceAmount slotContents = get(i);
            if (slotContents == null || !resource.equals(slotContents.getResource())) {
                continue;
            }
            final long stillNeeded = amount - extracted;
            final long toExtract = Math.min(slotContents.getAmount(), stillNeeded);
            if (action == Action.EXECUTE) {
                shrink(i, toExtract);
            }
            extracted += toExtract;
        }
        return extracted;
    }

    @Override
    public ResourceContainer copy() {
        final ResourceContainer copy = new ResourceContainerImpl(
            slots.length,
            maxAmountProvider,
            primaryResourceFactory,
            alternativeResourceFactories
        );
        for (int i = 0; i < size(); ++i) {
            final ResourceAmount slotContents = get(i);
            if (slotContents != null) {
                copy.set(i, slotContents);
            }
        }
        return copy;
    }

    public static ResourceContainer createForFilter() {
        return createForFilter(9);
    }

    public static ResourceContainer createForFilter(final ResourceContainerData data) {
        final ResourceContainer resourceContainer = createForFilter(data.resources().size());
        for (int i = 0; i < data.resources().size(); ++i) {
            final int ii = i;
            data.resources().get(i).ifPresent(resource -> resourceContainer.set(ii, resource));
        }
        return resourceContainer;
    }

    public static ResourceContainer createForFilter(final int size) {
        return new ResourceContainerImpl(
            size,
            resource -> Long.MAX_VALUE,
            PlatformApi.INSTANCE.getItemResourceFactory(),
            PlatformApi.INSTANCE.getAlternativeResourceFactories()
        );
    }

    public static ResourceContainer createForFilter(final ResourceFactory resourceFactory) {
        return createForFilter(resourceFactory, 9);
    }

    public static ResourceContainer createForFilter(final ResourceFactory resourceFactory, final int size) {
        return new ResourceContainerImpl(
            size,
            resource -> Long.MAX_VALUE,
            resourceFactory,
            Collections.emptySet()
        );
    }

    public static ResourceContainer createForFilter(final ResourceFactory resourceFactory,
                                                    final ResourceContainerData data) {
        final ResourceContainer resourceContainer = createForFilter(resourceFactory, data.resources().size());
        for (int i = 0; i < data.resources().size(); ++i) {
            final int ii = i;
            data.resources().get(i).ifPresent(resource -> resourceContainer.set(ii, resource));
        }
        return resourceContainer;
    }
}
