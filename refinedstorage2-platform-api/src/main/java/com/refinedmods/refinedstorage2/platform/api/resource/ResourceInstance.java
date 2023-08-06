package com.refinedmods.refinedstorage2.platform.api.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.Objects;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.13")
public class ResourceInstance<T> {
    private final ResourceAmount<T> resourceAmount;
    private final PlatformStorageChannelType<T> storageChannelType;
    private final ItemStack stackRepresentation;

    public ResourceInstance(final ResourceAmount<T> resourceAmount,
                            final PlatformStorageChannelType<T> storageChannelType) {
        this.resourceAmount = resourceAmount;
        this.storageChannelType = storageChannelType;
        this.stackRepresentation = resourceAmount.getResource() instanceof ItemResource itemResource
            ? itemResource.toItemStack(resourceAmount.getAmount())
            : ItemStack.EMPTY;
    }

    public T getResource() {
        return resourceAmount.getResource();
    }

    public long getAmount() {
        return resourceAmount.getAmount();
    }

    public PlatformStorageChannelType<T> getStorageChannelType() {
        return storageChannelType;
    }

    public ItemStack getStackRepresentation() {
        return stackRepresentation;
    }

    public long getInterfaceExportLimit() {
        return storageChannelType.getInterfaceExportLimit(resourceAmount.getResource());
    }

    public ResourceInstance<T> withAmount(final long amount) {
        return new ResourceInstance<>(
            new ResourceAmount<>(resourceAmount.getResource(), amount),
            storageChannelType
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResourceInstance<?> that = (ResourceInstance<?>) o;
        return Objects.equals(resourceAmount.getAmount(), that.resourceAmount.getAmount())
            && Objects.equals(resourceAmount.getResource(), that.resourceAmount.getResource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceAmount.getAmount(), resourceAmount.getResource());
    }
}
