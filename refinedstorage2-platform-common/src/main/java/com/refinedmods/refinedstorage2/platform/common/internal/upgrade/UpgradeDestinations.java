package com.refinedmods.refinedstorage2.platform.common.internal.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage2.platform.common.content.ContentIds;
import com.refinedmods.refinedstorage2.platform.common.content.Items;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public enum UpgradeDestinations implements UpgradeDestination {
    IMPORTER(createTranslation("block", ContentIds.IMPORTER.getPath()),
        () -> new ItemStack(Items.INSTANCE.getImporters().get(0).get())),
    EXPORTER(createTranslation("block", ContentIds.EXPORTER.getPath()),
        () -> new ItemStack(Items.INSTANCE.getExporters().get(0).get())),
    DESTRUCTOR(createTranslation("block", ContentIds.DESTRUCTOR.getPath()),
        () -> new ItemStack(Items.INSTANCE.getDestructors().get(0).get())),
    CONSTRUCTOR(createTranslation("block", ContentIds.CONSTRUCTOR.getPath()),
        () -> new ItemStack(Items.INSTANCE.getConstructors().get(0).get()));

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
