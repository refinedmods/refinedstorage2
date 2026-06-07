package com.refinedmods.refinedstorage.common.api.networking;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.3.0")
public record NetworkMonitorDeviceType(Component name, Item icon) {
}
