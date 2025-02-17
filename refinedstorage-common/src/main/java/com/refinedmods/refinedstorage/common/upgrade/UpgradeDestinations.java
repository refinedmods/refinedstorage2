package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.Items;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public enum UpgradeDestinations implements UpgradeDestination {
    IMPORTER(ContentNames.IMPORTER, () -> new ItemStack(Items.INSTANCE.getImporters().getFirst().get())),
    EXPORTER(ContentNames.EXPORTER, () -> new ItemStack(Items.INSTANCE.getExporters().getFirst().get())),
    DESTRUCTOR(ContentNames.DESTRUCTOR, () -> new ItemStack(Items.INSTANCE.getDestructors().getFirst().get())),
    CONSTRUCTOR(ContentNames.CONSTRUCTOR, () -> new ItemStack(Items.INSTANCE.getConstructors().getFirst().get())),
    WIRELESS_TRANSMITTER(ContentNames.WIRELESS_TRANSMITTER, () -> new ItemStack(
        Items.INSTANCE.getWirelessTransmitters().getFirst().get()
    )),
    DISK_INTERFACE(ContentNames.DISK_INTERFACE, () -> new ItemStack(
        Items.INSTANCE.getDiskInterfaces().getFirst().get()
    )),
    AUTOCRAFTER(ContentNames.AUTOCRAFTER, () -> new ItemStack(Items.INSTANCE.getAutocrafters().getFirst().get()));

    private final Component name;
    private final Supplier<ItemStack> stackFactory;
    @Nullable
    private ItemStack cachedStack;

    UpgradeDestinations(final Component name, final Supplier<ItemStack> stackFactory) {
        this.name = name;
        this.stackFactory = stackFactory;
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public ItemStack getStackRepresentation() {
        if (cachedStack == null) {
            cachedStack = stackFactory.get();
        }
        return cachedStack;
    }
}
