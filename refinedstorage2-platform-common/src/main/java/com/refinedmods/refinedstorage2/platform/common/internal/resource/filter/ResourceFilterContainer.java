package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.TypedTemplate;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ResourceFilterContainer {
    private final FilteredResource<?>[] items;
    private final long maxAmount;
    @Nullable
    private Runnable listener;

    public ResourceFilterContainer(final int size) {
        this(size, -1);
    }

    public ResourceFilterContainer(final int size, final long maxAmount) {
        this.items = new FilteredResource[size];
        this.maxAmount = maxAmount;
    }

    public boolean supportsAmount() {
        return maxAmount >= 0;
    }

    public void setListener(@Nullable final Runnable listener) {
        this.listener = listener;
    }

    public void set(final int index, final FilteredResource<?> resource) {
        setSilently(index, resource);
        if (listener != null) {
            listener.run();
        }
    }

    private void setSilently(final int index, final FilteredResource<?> resource) {
        items[index] = resource;
    }

    public void setAmount(final int index, final long amount) {
        if (!supportsAmount()) {
            return;
        }
        final FilteredResource<?> filteredResource = get(index);
        if (filteredResource == null) {
            return;
        }
        final long newAmount = Mth.clamp(
            amount,
            1,
            Math.min(maxAmount, filteredResource.getMaxAmount())
        );
        set(index, filteredResource.withAmount(newAmount));
    }

    public void remove(final int index) {
        removeSilently(index);
        if (listener != null) {
            listener.run();
        }
    }

    private void removeSilently(final int index) {
        items[index] = null;
    }

    public int size() {
        return items.length;
    }

    @Nullable
    public FilteredResource<?> get(final int index) {
        return items[index];
    }

    public Set<TypedTemplate<?>> getUniqueTemplates() {
        return getTemplates(new HashSet<>());
    }

    public List<TypedTemplate<?>> getTemplates() {
        return getTemplates(new ArrayList<>());
    }

    private <C extends Collection<TypedTemplate<?>>> C getTemplates(final C result) {
        for (int i = 0; i < size(); ++i) {
            final FilteredResource<?> item = items[i];
            if (item == null) {
                continue;
            }
            result.add(createTemplate(item));
        }
        return result;
    }

    private <T> TypedTemplate<T> createTemplate(final FilteredResource<T> item) {
        return new TypedTemplate<>(
            item.getValue(),
            item.getStorageChannelType()
        );
    }

    public void writeToUpdatePacket(final FriendlyByteBuf buf) {
        for (int index = 0; index < items.length; ++index) {
            final FilteredResource<?> item = items[index];
            if (item == null) {
                buf.writeBoolean(false);
                continue;
            }
            writeToUpdatePacket(buf, item);
        }
    }

    private <T> void writeToUpdatePacket(final FriendlyByteBuf buf,
                                         final FilteredResource<T> item) {
        final PlatformStorageChannelType<T> storageChannelType = item.getStorageChannelType();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresentOrElse(id -> {
            buf.writeBoolean(true);
            buf.writeResourceLocation(id);
            storageChannelType.toBuffer(item.getValue(), buf);
            buf.writeLong(item.getAmount());
        }, () -> buf.writeBoolean(false));
    }

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
        storageChannelType.toFilteredResource(new ResourceAmount<>(resource, amount)).ifPresent(
            filteredResource -> setSilently(index, filteredResource)
        );
    }

    public CompoundTag toTag() {
        final CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            final FilteredResource<?> item = items[i];
            if (item == null) {
                continue;
            }
            addToTag(tag, i, item);
        }
        return tag;
    }

    private <T> void addToTag(final CompoundTag tag,
                              final int index,
                              final FilteredResource<T> item) {
        final PlatformStorageChannelType<T> storageChannelType = item.getStorageChannelType();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresent(
            storageChannelTypeId -> addToTag(tag, index, item, storageChannelType, storageChannelTypeId)
        );
    }

    private <T> void addToTag(final CompoundTag tag,
                              final int index,
                              final FilteredResource<T> filteredResource,
                              final PlatformStorageChannelType<T> storageChannelType,
                              final ResourceLocation storageChannelTypeId) {
        final CompoundTag serialized = new CompoundTag();
        serialized.putString("t", storageChannelTypeId.toString());
        serialized.put("v", storageChannelType.toTag(filteredResource.getValue()));
        serialized.putLong("a", filteredResource.getAmount());
        tag.put("s" + index, serialized);
    }

    public void fromTag(final CompoundTag tag) {
        for (int i = 0; i < size(); ++i) {
            final String key = "s" + i;
            if (!tag.contains(key)) {
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
        storageChannelType.fromTag(tag.getCompound("v"))
            .flatMap(resource -> storageChannelType.toFilteredResource(new ResourceAmount<>(resource, amount)))
            .ifPresent(filteredResource -> setSilently(index, filteredResource));
    }
}
