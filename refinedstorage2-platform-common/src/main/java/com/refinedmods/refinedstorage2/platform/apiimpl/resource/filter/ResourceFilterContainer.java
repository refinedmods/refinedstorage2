package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    public ResourceFilterContainer(OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry, int size, Runnable listener) {
        this.resourceTypeRegistry = resourceTypeRegistry;
        this.items = new FilteredResource[size];
        this.listener = listener;
    }

    public void set(int index, FilteredResource resource) {
        setSilently(index, resource);
        listener.run();
    }

    private void setSilently(int index, FilteredResource resource) {
        items[index] = resource;
    }

    public void remove(int index) {
        removeSilently(index);
        listener.run();
    }

    private void removeSilently(int index) {
        items[index] = null;
    }

    public int size() {
        return items.length;
    }

    public FilteredResource get(int index) {
        return items[index];
    }

    public Set<Object> getTemplates() {
        Set<Object> result = new HashSet<>();
        for (int i = 0; i < size(); ++i) {
            FilteredResource item = items[i];
            if (item == null) {
                continue;
            }
            Object value = item.getValue();
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    public ResourceType determineDefaultType() {
        List<ResourceType> distinctTypes = Arrays.stream(items)
                .filter(Objects::nonNull)
                .map(FilteredResource::getType)
                .distinct()
                .toList();
        if (distinctTypes.size() == 1) {
            return distinctTypes.get(0);
        }
        return resourceTypeRegistry.getDefault();
    }

    public void writeToUpdatePacket(FriendlyByteBuf buf) {
        buf.writeResourceLocation(resourceTypeRegistry.getId(determineDefaultType()).orElseThrow(() -> new IllegalStateException("Default resource type not registered")));
        for (int index = 0; index < items.length; ++index) {
            writeToUpdatePacket(index, buf);
        }
    }

    public void writeToUpdatePacket(int index, FriendlyByteBuf buf) {
        FilteredResource item = items[index];
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

    public void readFromUpdatePacket(int index, FriendlyByteBuf buf) {
        boolean present = buf.readBoolean();
        if (!present) {
            removeSilently(index);
            return;
        }
        ResourceLocation id = buf.readResourceLocation();
        resourceTypeRegistry.get(id).ifPresentOrElse(
                type -> setSilently(index, type.fromPacket(buf)),
                () -> LOGGER.warn("Resource type {} is not registered on the client, cannot read from packet", id)
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            FilteredResource item = items[i];
            if (item == null) {
                continue;
            }
            int index = i;
            resourceTypeRegistry.getId(item.getType()).ifPresentOrElse(
                    id -> addToTag(tag, index, item, id),
                    () -> LOGGER.warn("Resource type {} is not registered, cannot serialize", item.getType())
            );
        }
        return tag;
    }

    private void addToTag(CompoundTag tag, int index, FilteredResource item, ResourceLocation typeId) {
        CompoundTag serialized = new CompoundTag();
        serialized.putString("t", typeId.toString());
        serialized.put("v", item.toTag());
        tag.put("s" + index, serialized);
    }

    public void load(CompoundTag tag) {
        for (int i = 0; i < size(); ++i) {
            String key = "s" + i;
            if (!tag.contains(key)) {
                continue;
            }
            CompoundTag item = tag.getCompound(key);
            load(i, item);
        }
    }

    private void load(int index, CompoundTag item) {
        ResourceLocation typeId = new ResourceLocation(item.getString("t"));
        resourceTypeRegistry.get(typeId).ifPresentOrElse(
                type -> load(index, item, type),
                () -> LOGGER.warn("Resource type {} is not registered, cannot deserialize", typeId)
        );
    }

    private void load(int index, CompoundTag item, ResourceType type) {
        type.fromTag(item.getCompound("v")).ifPresent(resource -> setSilently(index, resource));
    }
}
