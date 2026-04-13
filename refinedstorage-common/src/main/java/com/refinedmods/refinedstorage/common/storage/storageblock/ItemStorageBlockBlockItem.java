package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.AbstractStorageContainerBlockItem;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.content.Blocks;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;

public class ItemStorageBlockBlockItem extends AbstractStorageContainerBlockItem
    implements UpgradeableStorageContainer {
    private static final Component CREATIVE_HELP = createTranslation("item", "creative_storage_block.help");

    private final ItemStorageVariant variant;
    private final Component helpText;

    public ItemStorageBlockBlockItem(final Identifier id, final Block block, final ItemStorageVariant variant) {
        super(
            block,
            new Item.Properties().stacksTo(1).useBlockDescriptionPrefix().fireResistant()
                .setId(ResourceKey.create(Registries.ITEM, id)),
            RefinedStorageApi.INSTANCE.getStorageContainerItemHelper()
        );
        this.variant = variant;
        this.helpText = getHelpText(variant);
    }

    private static Component getHelpText(final ItemStorageVariant variant) {
        final Long capacity = variant.getCapacity();
        return capacity == null
            ? CREATIVE_HELP
            : createTranslation("item", "storage_block.help", format(capacity));
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
        return createStorage(variant, storageRepository::markAsChanged);
    }

    static SerializableStorage createStorage(final ItemStorageVariant variant, final Runnable listener) {
        return StorageTypes.ITEM.create(variant.getCapacity(), listener);
    }

    @Override
    protected ItemStack createPrimaryDisassemblyByproduct(final int count) {
        return new ItemStack(Blocks.INSTANCE.getMachineCasing(), count);
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
    protected boolean placeBlock(final BlockPlaceContext ctx, final BlockState state) {
        if (ctx.getPlayer() instanceof ServerPlayer serverPlayer && !(RefinedStorageApi.INSTANCE.canPlaceNetworkNode(
            serverPlayer,
            ctx.getLevel(),
            ctx.getClickedPos(),
            state
        ))) {
            return false;
        }
        return super.placeBlock(ctx, state);
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
