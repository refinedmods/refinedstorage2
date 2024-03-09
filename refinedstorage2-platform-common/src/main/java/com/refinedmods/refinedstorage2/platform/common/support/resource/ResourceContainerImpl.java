package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainerType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.util.MathUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToLongFunction;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ResourceContainerImpl implements ResourceContainer {
    private final ResourceContainerSlot[] slots;
    private final ResourceContainerType type;
    private final ToLongFunction<ResourceKey> maxAmountProvider;
    private final ResourceFactory primaryResourceFactory;
    private final Set<ResourceFactory> alternativeResourceFactories;

    @Nullable
    private Runnable listener;

    public ResourceContainerImpl(final int size,
                                 final ResourceContainerType type,
                                 final ToLongFunction<ResourceKey> maxAmountProvider,
                                 final ResourceFactory primaryResourceFactory,
                                 final Set<ResourceFactory> alternativeResourceFactories) {
        this.slots = new ResourceContainerSlot[size];
        this.type = type;
        this.maxAmountProvider = maxAmountProvider;
        this.primaryResourceFactory = primaryResourceFactory;
        this.alternativeResourceFactories = alternativeResourceFactories;
    }

    @Override
    public ResourceContainerType getType() {
        return type;
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
    public void writeToUpdatePacket(final FriendlyByteBuf buf) {
        for (final ResourceContainerSlot slot : slots) {
            if (slot == null) {
                buf.writeBoolean(false);
                continue;
            }
            writeToUpdatePacket(buf, slot);
        }
    }

    private void writeToUpdatePacket(final FriendlyByteBuf buf, final ResourceContainerSlot slot) {
        final ResourceType resourceType = slot.getResourceType();
        PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType).ifPresentOrElse(id -> {
            buf.writeBoolean(true);
            buf.writeResourceLocation(id);
            slot.getPlatformResource().toBuffer(buf);
            buf.writeLong(slot.getAmount());
        }, () -> buf.writeBoolean(false));
    }

    @Override
    public void readFromUpdatePacket(final int index, final FriendlyByteBuf buf) {
        final boolean present = buf.readBoolean();
        if (!present) {
            removeSilently(index);
            return;
        }
        final ResourceLocation id = buf.readResourceLocation();
        PlatformApi.INSTANCE.getResourceTypeRegistry().get(id).ifPresent(
            resourceType -> readFromUpdatePacket(index, buf, resourceType)
        );
    }

    private void readFromUpdatePacket(final int index,
                                      final FriendlyByteBuf buf,
                                      final ResourceType resourceType) {
        final ResourceKey resource = resourceType.fromBuffer(buf);
        final long amount = buf.readLong();
        setSilently(index, new ResourceAmount(resource, amount));
    }

    @Override
    public CompoundTag toTag() {
        final CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            final ResourceContainerSlot resourceAmount = slots[i];
            if (resourceAmount == null) {
                continue;
            }
            addToTag(tag, i, resourceAmount);
        }
        return tag;
    }

    private void addToTag(final CompoundTag tag,
                          final int index,
                          final ResourceContainerSlot resourceAmount) {
        final ResourceType resourceType = resourceAmount.getResourceType();
        PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType).ifPresent(
            resourceTypeId -> addToTag(tag, index, resourceAmount, resourceTypeId)
        );
    }

    private void addToTag(final CompoundTag tag,
                          final int index,
                          final ResourceContainerSlot slot,
                          final ResourceLocation resourceTypeId) {
        final CompoundTag serialized = new CompoundTag();
        serialized.putString("t", resourceTypeId.toString());
        serialized.put("v", slot.getPlatformResource().toTag());
        serialized.putLong("a", slot.getAmount());
        tag.put("s" + index, serialized);
    }

    @Override
    public void fromTag(final CompoundTag tag) {
        for (int i = 0; i < size(); ++i) {
            final String key = "s" + i;
            if (!tag.contains(key)) {
                removeSilently(i);
                continue;
            }
            final CompoundTag item = tag.getCompound(key);
            fromTag(i, item);
        }
    }

    private void fromTag(final int index, final CompoundTag tag) {
        final ResourceLocation resourceTypeId = new ResourceLocation(tag.getString("t"));
        PlatformApi.INSTANCE.getResourceTypeRegistry().get(resourceTypeId).ifPresent(
            resourceType -> fromTag(index, tag, resourceType)
        );
    }

    private void fromTag(final int index, final CompoundTag tag, final ResourceType resourceType) {
        final long amount = tag.getLong("a");
        resourceType.fromTag(tag.getCompound("v")).ifPresent(resource -> setSilently(
            index,
            new ResourceAmount(resource, amount)
        ));
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
        if (type != ResourceContainerType.CONTAINER) {
            throw new UnsupportedOperationException();
        }
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
            type,
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

    public static ResourceContainer createForFilter(final int size) {
        return createForFilter(size, ResourceContainerType.FILTER);
    }

    public static ResourceContainer createForFilter(final int size, final ResourceContainerType type) {
        return new ResourceContainerImpl(
            size,
            type,
            resource -> Long.MAX_VALUE,
            PlatformApi.INSTANCE.getItemResourceFactory(),
            PlatformApi.INSTANCE.getAlternativeResourceFactories()
        );
    }

    public static ResourceContainer createForFilter(final ResourceFactory resourceFactory) {
        return new ResourceContainerImpl(
            9,
            ResourceContainerType.FILTER,
            resource -> Long.MAX_VALUE,
            resourceFactory,
            Collections.emptySet()
        );
    }
}
