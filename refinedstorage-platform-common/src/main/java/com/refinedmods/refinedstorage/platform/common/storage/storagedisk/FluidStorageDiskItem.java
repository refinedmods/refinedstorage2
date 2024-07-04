package com.refinedmods.refinedstorage.platform.common.storage.storagedisk;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.storage.AbstractStorageContainerItem;
import com.refinedmods.refinedstorage.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.platform.api.support.AmountFormatting;
import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.content.Items;
import com.refinedmods.refinedstorage.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage.platform.common.storage.StorageTypes;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResourceRendering;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class FluidStorageDiskItem extends AbstractStorageContainerItem {
    private static final Component CREATIVE_HELP = createTranslation("item", "creative_fluid_storage_disk.help");

    private final FluidStorageType.Variant variant;
    private final Component helpText;

    public FluidStorageDiskItem(final FluidStorageType.Variant variant) {
        super(
            new Item.Properties().stacksTo(1).fireResistant(),
            PlatformApi.INSTANCE.getStorageContainerItemHelper()
        );
        this.variant = variant;
        this.helpText = getHelpText(variant);
    }

    private static Component getHelpText(final FluidStorageType.Variant variant) {
        if (variant.getCapacityInBuckets() == null) {
            return CREATIVE_HELP;
        }
        return createTranslation(
            "item",
            "fluid_storage_disk.help",
            AmountFormatting.format(variant.getCapacityInBuckets())
        );
    }

    @Override
    protected boolean hasCapacity() {
        return variant.hasCapacity();
    }

    @Override
    protected String formatAmount(final long amount) {
        return FluidResourceRendering.format(amount);
    }

    @Override
    protected SerializableStorage createStorage(final StorageRepository storageRepository) {
        return StorageTypes.FLUID.create(variant.getCapacity(), storageRepository::markAsChanged);
    }

    @Override
    protected ItemStack createPrimaryDisassemblyByproduct(final int count) {
        return new ItemStack(Items.INSTANCE.getStorageHousing(), count);
    }

    @Override
    @Nullable
    protected ItemStack createSecondaryDisassemblyByproduct(final int count) {
        if (variant == FluidStorageType.Variant.CREATIVE) {
            return null;
        }
        return new ItemStack(Items.INSTANCE.getFluidStoragePart(variant), count);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(helpText));
    }
}
