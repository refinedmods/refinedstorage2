package com.refinedmods.refinedstorage.common.networking;

import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record NetworkMonitorDevice(UUID id, Component name, ItemStack icon) {
}
