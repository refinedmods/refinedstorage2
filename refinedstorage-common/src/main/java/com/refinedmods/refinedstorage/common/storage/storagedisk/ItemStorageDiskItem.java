package com.refinedmods.refinedstorage.common.storage.storagedisk;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.AbstractStorageContainerItem;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.storage.StorageTypes;
import com.refinedmods.refinedstorage.common.storage.StorageVariant;
import com.refinedmods.refinedstorage.common.storage.UpgradeableStorageContainer;

import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;

public class ItemStorageDiskItem extends AbstractStorageContainerItem implements UpgradeableStorageContainer {
    private static final Component CREATIVE_HELP = createTranslation("item", "creative_storage_disk.help");

    private final ItemStorageVariant variant;
    private final Component helpText;

    public ItemStorageDiskItem(final Identifier id, final ItemStorageVariant variant) {
        super(
            new Item.Properties().stacksTo(1).fireResistant().setId(ResourceKey.create(Registries.ITEM, id)),
            RefinedStorageApi.INSTANCE.getStorageContainerItemHelper()
        );
        this.variant = variant;
        this.helpText = getHelpText(variant);
    }

    private static Component getHelpText(final ItemStorageVariant variant) {
        final Long capacity = variant.getCapacity();
        return capacity == null
            ? CREATIVE_HELP
            : createTranslation("item", "storage_disk.help", format(capacity));
    }

    @Nullable
    @Override
    protected Long getCapacity() {
        return variant.getCapacity();
    }

    @Override
    protected String formatAmount(final long amount) {
        return format(amount);
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
        if (variant == ItemStorageVariant.CREATIVE) {
            return null;
        }
        return new ItemStack(Items.INSTANCE.getItemStoragePart(variant), count);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(helpText));
    }

    @Override
    public StorageVariant getVariant() {
        return variant;
    }

    @Override
    public void transferTo(final ItemStack from, final ItemStack to) {
        helper.markAsToTransfer(from, to);
    }
}
