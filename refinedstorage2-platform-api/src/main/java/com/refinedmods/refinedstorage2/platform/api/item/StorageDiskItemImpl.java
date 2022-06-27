package com.refinedmods.refinedstorage2.platform.api.item;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.item.StorageDiskItem;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// TODO: Immunity for despawning
// TODO: Tags/ore dict in recipes
public abstract class StorageDiskItemImpl extends Item implements StorageDiskItem {
    protected StorageDiskItemImpl(final Properties properties) {
        super(properties);
    }

    @Override
    public Optional<UUID> getDiskId(final ItemStack stack) {
        return StorageItemHelper.getStorageId(stack);
    }

    @Override
    public Optional<StorageInfo> getInfo(final Level level, final ItemStack stack) {
        return StorageItemHelper.getInfo(level, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        return StorageItemHelper.tryDisassembly(level, player, stack, createPrimaryDisassemblyByproduct(stack.getCount()), createSecondaryDisassemblyByproduct(stack.getCount()));
    }

    @Override
    public void inventoryTick(final ItemStack stack, final Level level, final Entity entity, final int slot, final boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (!level.isClientSide() && !stack.hasTag() && entity instanceof Player) {
            final UUID id = UUID.randomUUID();
            PlatformApi.INSTANCE.getStorageRepository(level).set(id, createStorage(level));
            StorageItemHelper.setStorageId(stack, id);
        }
    }

    protected abstract Storage<?> createStorage(Level level);

    protected abstract ItemStack createPrimaryDisassemblyByproduct(int count);

    @Nullable
    protected abstract ItemStack createSecondaryDisassemblyByproduct(int count);
}
