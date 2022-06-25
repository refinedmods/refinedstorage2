package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
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

    private final OrderedRegistry<ResourceLocation, ResourceType<?>> resourceTypeRegistry;
    private final Object[] filters;
    private final ResourceType<?>[] types;
    private final Runnable listener;

    public ResourceFilterContainer(OrderedRegistry<ResourceLocation, ResourceType<?>> resourceTypeRegistry, int size, Runnable listener) {
        this.resourceTypeRegistry = resourceTypeRegistry;
        this.filters = new Object[size];
        this.types = new ResourceType[size];
        this.listener = listener;
    }

    public <T> void set(int slot, ResourceType<T> type, T value) {
        setSilently(slot, type, value);
        listener.run();
    }

    private <T> void setSilently(int slot, ResourceType<T> type, T value) {
        filters[slot] = value;
        types[slot] = type;
    }

    public void remove(int slot) {
        removeSilently(slot);
        listener.run();
    }

    private void removeSilently(int slot) {
        filters[slot] = null;
        types[slot] = null;
    }

    public int size() {
        return filters.length;
    }

    public Object getFilter(int slot) {
        return filters[slot];
    }

    public ResourceType<?> getType(int slot) {
        return types[slot];
    }

    public Set<Object> getTemplates() {
        Set<Object> set = new HashSet<>();
        for (int i = 0; i < size(); ++i) {
            Object value = getFilter(i);
            if (value != null) {
                set.add(value);
            }
        }
        return set;
    }

    public ResourceType<?> determineDefaultType() {
        List<ResourceType<?>> distinctTypes = Arrays.stream(types).filter(Objects::nonNull).distinct().toList();
        if (distinctTypes.size() == 1) {
            return distinctTypes.get(0);
        }
        return resourceTypeRegistry.getDefault();
    }

    public void writeToUpdatePacket(FriendlyByteBuf buf) {
        buf.writeResourceLocation(resourceTypeRegistry.getId(determineDefaultType()).orElseThrow(() -> new IllegalStateException("Default resource type not registered")));
        for (int i = 0; i < filters.length; ++i) {
            writeToUpdatePacket(i, buf);
        }
    }

    @SuppressWarnings("unchecked")
    public void writeToUpdatePacket(int slot, FriendlyByteBuf buf) {
        ResourceType<Object> type = (ResourceType<Object>) getType(slot);
        if (type == null) {
            buf.writeBoolean(false);
            return;
        }
        resourceTypeRegistry.getId(type).ifPresentOrElse(
                id -> {
                    buf.writeBoolean(true);
                    buf.writeResourceLocation(id);
                    type.writeToPacket(buf, getFilter(slot));
                },
                () -> buf.writeBoolean(false)
        );
    }

    @SuppressWarnings("unchecked")
    public void readFromUpdatePacket(int slot, FriendlyByteBuf buf) {
        boolean present = buf.readBoolean();
        if (!present) {
            removeSilently(slot);
            return;
        }
        ResourceLocation id = buf.readResourceLocation();
        resourceTypeRegistry.get(id).ifPresentOrElse(
                type -> {
                    Object value = type.readFromPacket(buf);
                    setSilently(slot, (ResourceType<? super Object>) type, value);
                },
                () -> LOGGER.warn("Resource type {} is not registered on the client, cannot read from packet", id)
        );
    }

    @SuppressWarnings("unchecked")
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < size(); ++i) {
            Object value = getFilter(i);
            if (value == null) {
                continue;
            }
            ResourceType<Object> type = (ResourceType<Object>) getType(i);
            int index = i;
            resourceTypeRegistry.getId(type).ifPresentOrElse(
                    id -> addResourceTypeToTag(tag, index, value, type, id),
                    () -> LOGGER.warn("Resource type {} is not registered, cannot serialize", type)
            );
        }
        return tag;
    }

    private void addResourceTypeToTag(CompoundTag tag, int index, Object value, ResourceType<Object> type, ResourceLocation typeId) {
        CompoundTag item = new CompoundTag();
        item.putString("t", typeId.toString());
        item.put("v", type.toTag(value));
        tag.put("s" + index, item);
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

    @SuppressWarnings("unchecked")
    private void load(int index, CompoundTag item, ResourceType<?> type) {
        type.fromTag(item.getCompound("v")).ifPresent(value -> setSilently(index, (ResourceType<? super Object>) type, value));
    }
}
