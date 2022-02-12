package com.refinedmods.refinedstorage2.platform.api.resource.filter;

import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ResourceFilterContainer {
    private final Object[] filters;
    private final ResourceType<?>[] types;
    private final Runnable listener;

    public ResourceFilterContainer(int size, Runnable listener) {
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

    public void writeToUpdatePacket(FriendlyByteBuf buf) {
        buf.writeResourceLocation(determineDefaultType().getId());
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
        buf.writeBoolean(true);
        buf.writeResourceLocation(type.getId());
        type.writeToPacket(buf, getFilter(slot));
    }

    @SuppressWarnings("unchecked")
    public void readFromUpdatePacket(int slot, FriendlyByteBuf buf) {
        boolean present = buf.readBoolean();
        if (!present) {
            remove(slot);
            return;
        }
        ResourceLocation id = buf.readResourceLocation();
        ResourceType<Object> type = (ResourceType<Object>) Rs2PlatformApiFacade.INSTANCE.getResourceTypeRegistry().get(id);
        Object value = type.readFromPacket(buf);
        set(slot, type, value);
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
            CompoundTag item = new CompoundTag();
            item.putString("t", type.getId().toString());
            item.put("v", type.toTag(value));
            tag.put("s" + i, item);
        }
        return tag;
    }

    @SuppressWarnings("unchecked")
    public void load(CompoundTag tag) {
        for (int i = 0; i < size(); ++i) {
            String key = "s" + i;
            if (!tag.contains(key)) {
                continue;
            }
            CompoundTag item = tag.getCompound(key);
            ResourceLocation typeId = new ResourceLocation(item.getString("t"));
            ResourceType<Object> type = (ResourceType<Object>) Rs2PlatformApiFacade.INSTANCE.getResourceTypeRegistry().get(typeId);
            final int index = i;
            type.fromTag(item.getCompound("v")).ifPresent(value -> setSilently(index, type, value));
        }
    }

    public ResourceType<?> determineDefaultType() {
        List<ResourceType<?>> distinctTypes = Arrays.stream(types).filter(Objects::nonNull).distinct().toList();
        if (distinctTypes.size() == 1) {
            return distinctTypes.get(0);
        }
        return Rs2PlatformApiFacade.INSTANCE.getResourceTypeRegistry().getDefault();
    }
}
