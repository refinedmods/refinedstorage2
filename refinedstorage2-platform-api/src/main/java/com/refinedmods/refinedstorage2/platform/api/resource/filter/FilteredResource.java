package com.refinedmods.refinedstorage2.platform.api.resource.filter;

import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.0")
public interface FilteredResource<T> {
    void render(PoseStack poseStack, int x, int y);

    T getValue();

    FilteredResource<T> withAmount(long newAmount);

    long getAmount();

    long getMaxAmount();

    String getDisplayedAmount();

    List<Component> getTooltip();

    PlatformStorageChannelType<T> getStorageChannelType();
}
