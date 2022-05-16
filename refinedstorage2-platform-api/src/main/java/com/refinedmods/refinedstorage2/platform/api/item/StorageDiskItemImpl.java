package com.refinedmods.refinedstorage2.platform.api.item;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.api.storage.item.StorageDiskItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

// TODO immunity for despawning
// TODO tags in recipes
public abstract class StorageDiskItemImpl extends Item implements StorageDiskItem {
    protected StorageDiskItemImpl(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<UUID> getDiskId(ItemStack stack) {
        return StorageItemHelper.getStorageId(stack);
    }

    @Override
    public Optional<StorageInfo> getInfo(Level level, ItemStack stack) {
        return StorageItemHelper.getInfo(level, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        StorageItemHelper.appendHoverText(stack, level, tooltip, context, this::formatQuantity);
    }

    protected String formatQuantity(long qty) {
        return QuantityFormatter.formatWithUnits(qty);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return StorageItemHelper.tryDisassembly(level, player, stack, createPrimaryDisassemblyByproduct(), createSecondaryDisassemblyByproduct(stack.getCount()));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (!level.isClientSide() && !stack.hasTag() && entity instanceof Player) {
            UUID id = UUID.randomUUID();
            Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level).set(id, createStorage(level));
            StorageItemHelper.setStorageId(stack, id);
        }
    }

    protected abstract Storage<?> createStorage(Level level);

    protected abstract ItemStack createPrimaryDisassemblyByproduct();

    protected abstract ItemStack createSecondaryDisassemblyByproduct(int count);
}
