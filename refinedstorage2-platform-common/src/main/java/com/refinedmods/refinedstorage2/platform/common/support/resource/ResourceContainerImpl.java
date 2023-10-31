package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainerType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.util.MathHelper;

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
    private final ResourceAmountTemplate<?>[] slots;
    private final ResourceContainerType type;
    private final ToLongFunction<ResourceTemplate<?>> maxAmountProvider;
    private final ResourceFactory<?> primaryResourceFactory;
    private final Set<ResourceFactory<?>> alternativeResourceFactories;

    @Nullable
    private Runnable listener;

    public ResourceContainerImpl(final int size,
                                 final ResourceContainerType type,
                                 final ToLongFunction<ResourceTemplate<?>> maxAmountProvider,
                                 final ResourceFactory<?> primaryResourceFactory,
                                 final Set<ResourceFactory<?>> alternativeResourceFactories) {
        this.slots = new ResourceAmountTemplate<?>[size];
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
            for (final ResourceFactory<?> resourceFactory : alternativeResourceFactories) {
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
    public <T> void set(final int index, final ResourceAmountTemplate<T> resourceAmount) {
        setSilently(index, resourceAmount);
        changed();
    }

    private <T> void setSilently(final int index, final ResourceAmountTemplate<T> resourceAmount) {
        slots[index] = resourceAmount;
    }

    @Override
    public <T> boolean isValid(final T resource) {
        if (primaryResourceFactory.isValid(resource)) {
            return true;
        }
        for (final ResourceFactory<?> resourceFactory : alternativeResourceFactories) {
            if (resourceFactory.isValid(resource)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getAmount(final int index) {
        final ResourceAmountTemplate<?> resourceAmount = slots[index];
        if (resourceAmount == null) {
            return 0;
        }
        return resourceAmount.getAmount();
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
        final ResourceAmountTemplate<?> resourceAmount = slots[index];
        if (resourceAmount == null) {
            return;
        }
        final long newAmount = MathHelper.clamp(amount, 0, getMaxAmount(resourceAmount));
        if (newAmount == 0) {
            remove(index);
        } else {
            slots[index] = resourceAmount.withAmount(newAmount);
        }
        changed();
    }

    @Override
    public <T> long getMaxAmount(final ResourceAmountTemplate<T> resourceAmount) {
        return maxAmountProvider.applyAsLong(resourceAmount.getResourceTemplate());
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
    public ResourceAmountTemplate<?> get(final int index) {
        return slots[index];
    }

    @Override
    public Set<Object> getUniqueTemplates() {
        final Set<Object> result = new HashSet<>();
        for (int i = 0; i < size(); ++i) {
            final ResourceAmountTemplate<?> resourceAmount = slots[i];
            if (resourceAmount == null) {
                continue;
            }
            result.add(resourceAmount.getResourceTemplate().resource());
        }
        return result;
    }

    @Override
    public List<ResourceTemplate<?>> getTemplates() {
        final List<ResourceTemplate<?>> result = new ArrayList<>();
        for (int i = 0; i < size(); ++i) {
            final ResourceAmountTemplate<?> resourceAmount = slots[i];
            if (resourceAmount == null) {
                continue;
            }
            result.add(resourceAmount.getResourceTemplate());
        }
        return result;
    }

    @Override
    public void writeToUpdatePacket(final FriendlyByteBuf buf) {
        for (final ResourceAmountTemplate<?> slot : slots) {
            if (slot == null) {
                buf.writeBoolean(false);
                continue;
            }
            writeToUpdatePacket(buf, slot);
        }
    }

    private <T> void writeToUpdatePacket(final FriendlyByteBuf buf, final ResourceAmountTemplate<T> resourceAmount) {
        final PlatformStorageChannelType<T> storageChannelType = resourceAmount.getStorageChannelType();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresentOrElse(id -> {
            buf.writeBoolean(true);
            buf.writeResourceLocation(id);
            storageChannelType.toBuffer(resourceAmount.getResource(), buf);
            buf.writeLong(resourceAmount.getAmount());
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
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().get(id).ifPresent(
            storageChannelType -> readFromUpdatePacket(index, buf, storageChannelType)
        );
    }

    private <T> void readFromUpdatePacket(final int index,
                                          final FriendlyByteBuf buf,
                                          final PlatformStorageChannelType<T> storageChannelType) {
        final T resource = storageChannelType.fromBuffer(buf);
        final long amount = buf.readLong();
        setSilently(index, new ResourceAmountTemplate<>(resource, amount, storageChannelType));
    }

    @Override
    public CompoundTag toTag() {
        final CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            final ResourceAmountTemplate<?> resourceAmount = slots[i];
            if (resourceAmount == null) {
                continue;
            }
            addToTag(tag, i, resourceAmount);
        }
        return tag;
    }

    private <T> void addToTag(final CompoundTag tag,
                              final int index,
                              final ResourceAmountTemplate<T> resourceAmount) {
        final PlatformStorageChannelType<T> storageChannelType = resourceAmount.getStorageChannelType();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresent(
            storageChannelTypeId -> addToTag(tag, index, resourceAmount, storageChannelType, storageChannelTypeId)
        );
    }

    private <T> void addToTag(final CompoundTag tag,
                              final int index,
                              final ResourceAmountTemplate<T> resourceAmount,
                              final PlatformStorageChannelType<T> storageChannelType,
                              final ResourceLocation storageChannelTypeId) {
        final CompoundTag serialized = new CompoundTag();
        serialized.putString("t", storageChannelTypeId.toString());
        serialized.put("v", storageChannelType.toTag(resourceAmount.getResource()));
        serialized.putLong("a", resourceAmount.getAmount());
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
        final ResourceLocation storageChannelTypeId = new ResourceLocation(tag.getString("t"));
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().get(storageChannelTypeId).ifPresent(
            storageChannelType -> fromTag(index, tag, storageChannelType)
        );
    }

    private <T> void fromTag(final int index,
                             final CompoundTag tag,
                             final PlatformStorageChannelType<T> storageChannelType) {
        final long amount = tag.getLong("a");
        storageChannelType.fromTag(tag.getCompound("v")).ifPresent(resource -> setSilently(
            index,
            new ResourceAmountTemplate<>(resource, amount, storageChannelType)
        ));
    }

    @Override
    public ResourceFactory<?> getPrimaryResourceFactory() {
        return primaryResourceFactory;
    }

    @Override
    public Set<ResourceFactory<?>> getAlternativeResourceFactories() {
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
    public <T> long insert(final StorageChannelType<T> storageChannelType,
                           final T resource,
                           final long amount,
                           final Action action) {
        if (!(storageChannelType instanceof PlatformStorageChannelType<T> platformStorageChannelType)) {
            return 0;
        }
        long remainder = amount;
        for (int i = 0; i < size(); ++i) {
            final ResourceAmountTemplate<?> existing = get(i);
            if (existing == null) {
                remainder -= insertIntoEmptySlot(i, resource, action, platformStorageChannelType, remainder);
            } else if (existing.getResource().equals(resource)) {
                remainder -= insertIntoExistingSlot(
                    i,
                    platformStorageChannelType,
                    resource,
                    action,
                    remainder,
                    existing
                );
            }
            if (remainder == 0) {
                break;
            }
        }
        return amount - remainder;
    }

    private <T> long insertIntoEmptySlot(final int slotIndex,
                                         final T resource,
                                         final Action action,
                                         final PlatformStorageChannelType<T> platformStorageChannelType,
                                         final long amount) {
        final long inserted = Math.min(platformStorageChannelType.getInterfaceExportLimit(resource), amount);
        if (action == Action.EXECUTE) {
            set(slotIndex, new ResourceAmountTemplate<>(
                resource,
                inserted,
                platformStorageChannelType
            ));
        }
        return inserted;
    }

    private <T> long insertIntoExistingSlot(final int slotIndex,
                                            final PlatformStorageChannelType<T> storageChannelType,
                                            final T resource,
                                            final Action action,
                                            final long amount,
                                            final ResourceAmountTemplate<?> existing) {
        final long spaceRemaining = storageChannelType.getInterfaceExportLimit(resource) - existing.getAmount();
        final long inserted = Math.min(spaceRemaining, amount);
        if (action == Action.EXECUTE) {
            grow(slotIndex, inserted);
        }
        return inserted;
    }

    @Override
    public <T> long extract(final T resource, final long amount, final Action action) {
        long extracted = 0;
        for (int i = 0; i < size(); ++i) {
            final ResourceAmountTemplate<?> slot = get(i);
            if (slot == null || !resource.equals(slot.getResource())) {
                continue;
            }
            final long stillNeeded = amount - extracted;
            final long toExtract = Math.min(slot.getAmount(), stillNeeded);
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
            final ResourceAmountTemplate<?> resourceAmount = get(i);
            if (resourceAmount != null) {
                copy.set(i, resourceAmount);
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

    public static <T> ResourceContainer createForFilter(final ResourceFactory<T> resourceFactory) {
        return new ResourceContainerImpl(
            9,
            ResourceContainerType.FILTER,
            resource -> Long.MAX_VALUE,
            resourceFactory,
            Collections.emptySet()
        );
    }
}
