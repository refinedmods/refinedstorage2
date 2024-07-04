package com.refinedmods.refinedstorage.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.storage.AbstractStorageContainerBlockItem;
import com.refinedmods.refinedstorage.platform.api.support.AmountFormatting;
import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.content.Blocks;
import com.refinedmods.refinedstorage.platform.common.content.Items;
import com.refinedmods.refinedstorage.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResourceRendering;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class FluidStorageBlockBlockItem extends AbstractStorageContainerBlockItem {
    private static final Component CREATIVE_HELP = createTranslation("item", "creative_fluid_storage_block.help");

    private final FluidStorageType.Variant variant;
    private final Component helpText;

    public FluidStorageBlockBlockItem(final Block block, final FluidStorageType.Variant variant) {
        super(
            block,
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
            "fluid_storage_block.help",
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
    protected ItemStack createPrimaryDisassemblyByproduct(final int count) {
        return new ItemStack(Blocks.INSTANCE.getMachineCasing(), count);
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
    protected boolean placeBlock(final BlockPlaceContext ctx, final BlockState state) {
        if (ctx.getPlayer() instanceof ServerPlayer serverPlayer
            && !(PlatformApi.INSTANCE.canPlaceNetworkNode(serverPlayer, ctx.getLevel(), ctx.getClickedPos(), state))) {
            return false;
        }
        return super.placeBlock(ctx, state);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(helpText));
    }
}
