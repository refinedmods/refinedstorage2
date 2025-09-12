package com.refinedmods.refinedstorage.common.support.resource;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.util.MathUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToLongFunction;
import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceContainerImpl implements ResourceContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceContainerImpl.class);

    private final ResourceAmount[] slots;
    private final ItemStack[] stackRepresentations;
    private final ToLongFunction<ResourceKey> maxAmountProvider;
    private final ResourceFactory primaryResourceFactory;
    private final Set<ResourceFactory> alternativeResourceFactories;

    @Nullable
    private Runnable listener;

    public ResourceContainerImpl(final int size,
                                 final ToLongFunction<ResourceKey> maxAmountProvider,
                                 final ResourceFactory primaryResourceFactory,
                                 final Set<ResourceFactory> alternativeResourceFactories) {
        this.slots = new ResourceAmount[size];
        this.stackRepresentations = new ItemStack[size];
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
                final var result = resourceFactory.create(stack).map(this::respectMaxAmount);
                if (result.isPresent()) {
                    set(index, result.get());
                    return;
                }
            }
        }
        primaryResourceFactory.create(stack).map(this::respectMaxAmount).ifPresentOrElse(
            resource -> set(index, resource),
            () -> remove(index)
        );
    }

    private ResourceAmount respectMaxAmount(final ResourceAmount resourceAmount) {
        final long maxAmount = getMaxAmount(resourceAmount.resource());
        if (resourceAmount.amount() > maxAmount) {
            return new ResourceAmount(resourceAmount.resource(), maxAmount);
        }
        return resourceAmount;
    }

    @Override
    public void set(final int index, final ResourceAmount resourceAmount) {
        setSilently(index, resourceAmount);
        changed();
    }

    protected void setSilently(final int index, final ResourceAmount resourceAmount) {
        slots[index] = resourceAmount;
        stackRepresentations[index] = resourceAmount.resource() instanceof ItemResource itemResource
            ? itemResource.toItemStack(resourceAmount.amount())
            : null;
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
        final ResourceAmount slot = slots[index];
        if (slot == null) {
            return 0;
        }
        return slot.amount();
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
        final ResourceAmount slot = slots[index];
        if (slot == null) {
            return;
        }
        final long newAmount = MathUtil.clamp(amount, 0, getMaxAmount(slot.resource()));
        if (newAmount == 0) {
            removeSilently(index);
        } else {
            setSilently(index, new ResourceAmount(slot.resource(), newAmount));
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

    @Override
    public void clear() {
        for (int i = 0; i < size(); ++i) {
            removeSilently(i);
        }
        changed();
    }

    protected void removeSilently(final int index) {
        slots[index] = null;
        stackRepresentations[index] = null;
    }

    @Override
    public int size() {
        return slots.length;
    }

    @Override
    @Nullable
    public ResourceAmount get(final int index) {
        return slots[index];
    }

    @Nullable
    @Override
    public PlatformResourceKey getResource(final int index) {
        final ResourceAmount slot = slots[index];
        if (slot == null) {
            return null;
        }
        return (PlatformResourceKey) slot.resource();
    }

    @Override
    public ItemStack getStackRepresentation(final int index) {
        final ItemStack stack = stackRepresentations[index];
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @Override
    public Set<ResourceKey> getUniqueResources() {
        final Set<ResourceKey> result = new HashSet<>();
        for (int i = 0; i < size(); ++i) {
            final ResourceAmount slot = slots[i];
            if (slot == null) {
                continue;
            }
            result.add(slot.resource());
        }
        return result;
    }

    @Override
    public List<ResourceKey> getResources() {
        final List<ResourceKey> result = new ArrayList<>();
        for (int i = 0; i < size(); ++i) {
            final ResourceAmount slot = slots[i];
            if (slot == null) {
                continue;
            }
            result.add(slot.resource());
        }
        return result;
    }

    @Override
    public CompoundTag toTag(final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            final ResourceAmount slot = slots[i];
            if (slot == null) {
                continue;
            }
            addToTag(tag, i, slot, provider);
        }
        return tag;
    }

    private void addToTag(final CompoundTag tag,
                          final int index,
                          final ResourceAmount slot,
                          final HolderLookup.Provider provider) {
        final Tag serialized = ResourceCodecs.AMOUNT_CODEC.encode(
            slot,
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
        ResourceCodecs.AMOUNT_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
            .resultOrPartial(error ->
                LOGGER.error("Failed to load resource container slot {} {}: {}", index, tag, error))
            .ifPresent(resourceAmount -> setSilently(index, resourceAmount));
    }

    @Override
    public ResourceFactory getPrimaryResourceFactory() {
        return primaryResourceFactory;
    }

    @Override
    public Set<ResourceFactory> getAlternativeResourceFactories() {
        return alternativeResourceFactories;
    }

    protected final void changed() {
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
        CoreValidations.validateNotNull(resource, "Resource to insert must not be null.");
        CoreValidations.validateLargerThanZero(amount, "Amount to insert must be larger than zero.");
        if (!(resource instanceof PlatformResourceKey platformResource)) {
            return 0L;
        }
        long remainder = amount;
        for (int i = 0; i < size(); ++i) {
            final ResourceAmount slot = get(i);
            if (slot == null) {
                remainder -= insertIntoEmptySlot(i, platformResource, action, remainder);
            } else if (slot.resource().equals(resource)) {
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
        final long spaceRemaining = resource.getInterfaceExportLimit() - existing.amount();
        final long inserted = Math.min(spaceRemaining, amount);
        if (action == Action.EXECUTE) {
            grow(slotIndex, inserted);
        }
        return inserted;
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action) {
        CoreValidations.validateNotNull(resource, "Resource to extract must not be null.");
        CoreValidations.validateLargerThanZero(amount, "Amount to extract must be larger than zero.");
        long extracted = 0;
        for (int i = 0; i < size(); ++i) {
            final ResourceAmount slotContents = get(i);
            if (slotContents == null || !resource.equals(slotContents.resource())) {
                continue;
            }
            final long stillNeeded = amount - extracted;
            final long toExtract = Math.min(slotContents.amount(), stillNeeded);
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
            final ResourceAmount slot = get(i);
            if (slot != null) {
                copy.set(i, slot);
            }
        }
        return copy;
    }

    public static ResourceContainer createForFilter() {
        return createForFilter(9);
    }

    public static ResourceContainer createForFilter(final ResourceContainerData data) {
        final ResourceContainer resourceContainer = createForFilter(data.resources().size());
        setResourceContainerData(data.resources(), resourceContainer);
        return resourceContainer;
    }

    public static ResourceContainer createForFilter(final int size) {
        return new ResourceContainerImpl(
            size,
            resource -> Long.MAX_VALUE,
            RefinedStorageApi.INSTANCE.getItemResourceFactory(),
            RefinedStorageApi.INSTANCE.getAlternativeResourceFactories()
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
        setResourceContainerData(data.resources(), resourceContainer);
        return resourceContainer;
    }

    public static ResourceContainer createForFilter(final ResourceFactory resourceFactory,
                                                    final List<Optional<ResourceAmount>> resources) {
        final ResourceContainer resourceContainer = createForFilter(resourceFactory, resources.size());
        setResourceContainerData(resources, resourceContainer);
        return resourceContainer;
    }

    public static void setResourceContainerData(final List<Optional<ResourceAmount>> resources,
                                                final ResourceContainer resourceContainer) {
        for (int i = 0; i < resources.size(); ++i) {
            final int ii = i;
            resources.get(i).ifPresent(resource -> resourceContainer.set(ii, resource));
        }
    }
}
