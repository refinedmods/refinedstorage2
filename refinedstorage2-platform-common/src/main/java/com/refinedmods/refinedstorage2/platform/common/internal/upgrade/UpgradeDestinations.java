package com.refinedmods.refinedstorage2.platform.common.internal.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage2.platform.common.content.ContentIds;
import com.refinedmods.refinedstorage2.platform.common.content.Items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public enum UpgradeDestinations implements UpgradeDestination {
    IMPORTER(
        createTranslation("block", ContentIds.IMPORTER.getPath()),
        new ItemStack(Items.INSTANCE.getImporters().get(0).get())
    ),
    EXPORTER(
        createTranslation("block", ContentIds.EXPORTER.getPath()),
        new ItemStack(Items.INSTANCE.getExporters().get(0).get())
    ),
    DESTRUCTOR(
        createTranslation("block", ContentIds.DESTRUCTOR.getPath()),
        new ItemStack(Items.INSTANCE.getDestructors().get(0).get())
    ),
    CONSTRUCTOR(
        createTranslation("block", ContentIds.CONSTRUCTOR.getPath()),
        new ItemStack(Items.INSTANCE.getConstructors().get(0).get())
    );

    private final Component name;
    private final ItemStack stack;

    UpgradeDestinations(final Component name, final ItemStack stack) {
        this.name = name;
        this.stack = stack;
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public ItemStack getStackRepresentation() {
        return stack;
    }
}
