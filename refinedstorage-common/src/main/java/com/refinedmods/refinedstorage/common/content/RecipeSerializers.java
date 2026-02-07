package com.refinedmods.refinedstorage.common.content;

import com.refinedmods.refinedstorage.common.storage.StorageContainerUpgradeRecipe;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeWithEnchantedBookRecipe;

import java.util.function.Supplier;

import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class RecipeSerializers {
    public static final RecipeSerializers INSTANCE = new RecipeSerializers();

    @Nullable
    private Supplier<RecipeSerializer<UpgradeWithEnchantedBookRecipe>> upgradeWithEnchantedBook;
    @Nullable
    private Supplier<RecipeSerializer<StorageContainerUpgradeRecipe>> storageContainerUpgrade;

    private RecipeSerializers() {
    }

    public RecipeSerializer<UpgradeWithEnchantedBookRecipe> getUpgradeWithEnchantedBook() {
        return requireNonNull(upgradeWithEnchantedBook).get();
    }

    public void setUpgradeWithEnchantedBook(
        final Supplier<RecipeSerializer<UpgradeWithEnchantedBookRecipe>> upgradeWithEnchantedBook
    ) {
        this.upgradeWithEnchantedBook = upgradeWithEnchantedBook;
    }

    public RecipeSerializer<StorageContainerUpgradeRecipe> getStorageContainerUpgrade() {
        return requireNonNull(storageContainerUpgrade).get();
    }

    public void setStorageContainerUpgrade(
        final Supplier<RecipeSerializer<StorageContainerUpgradeRecipe>> storageContainerUpgrade
    ) {
        this.storageContainerUpgrade = storageContainerUpgrade;
    }
}
