package com.refinedmods.refinedstorage.platform.common.storage.storagedisk;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.storage.AbstractStorageContainerItem;
import com.refinedmods.refinedstorage.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.platform.api.support.AmountFormatting;
import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.content.Items;
import com.refinedmods.refinedstorage.platform.common.storage.ItemStorageType;
import com.refinedmods.refinedstorage.platform.common.storage.StorageTypes;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class ItemStorageDiskItem extends AbstractStorageContainerItem {
    private static final Component CREATIVE_HELP = createTranslation("item", "creative_storage_disk.help");

    private final ItemStorageType.Variant variant;
    private final Component helpText;

    public ItemStorageDiskItem(final ItemStorageType.Variant variant) {
        super(
            new Item.Properties().stacksTo(1).fireResistant(),
            PlatformApi.INSTANCE.getStorageContainerItemHelper()
        );
        this.variant = variant;
        this.helpText = variant.getCapacity() == null
            ? CREATIVE_HELP
            : createTranslation("item", "storage_disk.help", AmountFormatting.format(variant.getCapacity()));
    }

    @Override
    protected boolean hasCapacity() {
        return variant.hasCapacity();
    }

    @Override
    protected String formatAmount(final long amount) {
        return AmountFormatting.format(amount);
    }

    @Override
    protected SerializableStorage createStorage(final StorageRepository storageRepository) {
        return StorageTypes.ITEM.create(variant.getCapacity(), storageRepository::markAsChanged);
    }

    @Override
    protected ItemStack createPrimaryDisassemblyByproduct(final int count) {
        return new ItemStack(Items.INSTANCE.getStorageHousing(), count);
    }

    @Override
    @Nullable
    protected ItemStack createSecondaryDisassemblyByproduct(final int count) {
        if (variant == ItemStorageType.Variant.CREATIVE) {
            return null;
        }
        return new ItemStack(Items.INSTANCE.getItemStoragePart(variant), count);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(helpText));
    }
}
