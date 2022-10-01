package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceFilterContainer {
    private static final Logger LOGGER = LogManager.getLogger();

    private final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry;
    private final FilteredResource[] items;
    private final Runnable listener;


    public ResourceFilterContainer(final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry,
                                   final int size) {
        this(resourceTypeRegistry, size, () -> {
        });
    }

    public ResourceFilterContainer(final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry,
                                   final int size,
                                   final Runnable listener) {
        this.resourceTypeRegistry = resourceTypeRegistry;
        this.items = new FilteredResource[size];
        this.listener = listener;
    }

    public void set(final int index, final FilteredResource resource) {
        setSilently(index, resource);
        listener.run();
    }

    private void setSilently(final int index, final FilteredResource resource) {
        items[index] = resource;
    }

    public void remove(final int index) {
        removeSilently(index);
        listener.run();
    }

    private void removeSilently(final int index) {
        items[index] = null;
    }

    public int size() {
        return items.length;
    }

    @Nullable
    public FilteredResource get(final int index) {
        return items[index];
    }

    public Set<Object> getUniqueTemplates() {
        return getTemplates(new HashSet<>());
    }

    public List<Object> getTemplates() {
        return getTemplates(new ArrayList<>());
    }

    private <C extends Collection<Object>> C getTemplates(final C result) {
        for (int i = 0; i < size(); ++i) {
            final FilteredResource item = items[i];
            if (item == null) {
                continue;
            }
            result.add(item.getValue());
        }
        return result;
    }

    public ResourceType determineDefaultType() {
        final List<ResourceType> distinctTypes = Arrays.stream(items)
            .filter(Objects::nonNull)
            .map(FilteredResource::getType)
            .distinct()
            .toList();
        if (distinctTypes.size() == 1) {
            return distinctTypes.get(0);
        }
        return resourceTypeRegistry.getDefault();
    }

    public void writeToUpdatePacket(final FriendlyByteBuf buf) {
        buf.writeResourceLocation(resourceTypeRegistry.getId(determineDefaultType())
            .orElseThrow(() -> new IllegalStateException("Default resource type not registered")));
        for (int index = 0; index < items.length; ++index) {
            writeToUpdatePacket(index, buf);
        }
    }

    public void writeToUpdatePacket(final int index, final FriendlyByteBuf buf) {
        final FilteredResource item = items[index];
        if (item == null) {
            buf.writeBoolean(false);
            return;
        }
        resourceTypeRegistry.getId(item.getType()).ifPresentOrElse(
            id -> {
                buf.writeBoolean(true);
                buf.writeResourceLocation(id);
                item.writeToPacket(buf);
            },
            () -> buf.writeBoolean(false)
        );
    }

    public void readFromUpdatePacket(final int index, final FriendlyByteBuf buf) {
        final boolean present = buf.readBoolean();
        if (!present) {
            removeSilently(index);
            return;
        }
        final ResourceLocation id = buf.readResourceLocation();
        resourceTypeRegistry.get(id).ifPresentOrElse(
            type -> setSilently(index, type.fromPacket(buf)),
            () -> LOGGER.warn("Resource type {} is not registered on the client, cannot read from packet", id)
        );
    }

    public CompoundTag toTag() {
        final CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            final FilteredResource item = items[i];
            if (item == null) {
                continue;
            }
            final int index = i;
            resourceTypeRegistry.getId(item.getType()).ifPresentOrElse(
                id -> addToTag(tag, index, item, id),
                () -> LOGGER.warn("Resource type {} is not registered, cannot serialize", item.getType())
            );
        }
        return tag;
    }

    private void addToTag(final CompoundTag tag,
                          final int index,
                          final FilteredResource item,
                          final ResourceLocation typeId) {
        final CompoundTag serialized = new CompoundTag();
        serialized.putString("t", typeId.toString());
        serialized.put("v", item.toTag());
        tag.put("s" + index, serialized);
    }

    public void load(final CompoundTag tag) {
        for (int i = 0; i < size(); ++i) {
            final String key = "s" + i;
            if (!tag.contains(key)) {
                continue;
            }
            final CompoundTag item = tag.getCompound(key);
            load(i, item);
        }
    }

    private void load(final int index, final CompoundTag item) {
        final ResourceLocation typeId = new ResourceLocation(item.getString("t"));
        resourceTypeRegistry.get(typeId).ifPresentOrElse(
            type -> load(index, item, type),
            () -> LOGGER.warn("Resource type {} is not registered, cannot deserialize", typeId)
        );
    }

    private void load(final int index, final CompoundTag item, final ResourceType type) {
        type.fromTag(item.getCompound("v")).ifPresent(resource -> setSilently(index, resource));
    }
}
