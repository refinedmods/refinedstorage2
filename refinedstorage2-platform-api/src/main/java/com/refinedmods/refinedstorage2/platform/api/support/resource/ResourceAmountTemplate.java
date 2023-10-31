package com.refinedmods.refinedstorage2.platform.api.support.resource;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.Objects;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

/**
 * A ResourceAmountTemplate is the combination of a {@link com.refinedmods.refinedstorage2.api.resource.ResourceAmount}
 * and a {@link ResourceTemplate}. It identifies a resource, its storage channel type and an amount.
 * Additionally, for performance reasons, it provides an {@link ItemStack} representation.
 *
 * @param <T> the resource type
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.13")
public class ResourceAmountTemplate<T> {
    private final ResourceTemplate<T> resourceTemplate;
    private final long amount;
    private final ItemStack stackRepresentation;

    public ResourceAmountTemplate(final T resource,
                                  final long amount,
                                  final PlatformStorageChannelType<T> storageChannelType) {
        this.resourceTemplate = new ResourceTemplate<>(resource, storageChannelType);
        this.amount = amount;
        this.stackRepresentation = resource instanceof ItemResource itemResource
            ? itemResource.toItemStack(amount)
            : ItemStack.EMPTY;
    }

    public T getResource() {
        return resourceTemplate.resource();
    }

    public PlatformStorageChannelType<T> getStorageChannelType() {
        return (PlatformStorageChannelType<T>) resourceTemplate.storageChannelType();
    }

    public long getAmount() {
        return amount;
    }

    public ResourceTemplate<T> getResourceTemplate() {
        return resourceTemplate;
    }

    public ItemStack getStackRepresentation() {
        return stackRepresentation;
    }

    public ResourceAmountTemplate<T> withAmount(final long newAmount) {
        return new ResourceAmountTemplate<>(
            resourceTemplate.resource(),
            newAmount,
            (PlatformStorageChannelType<T>) resourceTemplate.storageChannelType()
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
        final ResourceAmountTemplate<?> that = (ResourceAmountTemplate<?>) o;
        return Objects.equals(amount, that.amount)
            && Objects.equals(resourceTemplate.resource(), that.resourceTemplate.resource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, resourceTemplate.resource());
    }
}
