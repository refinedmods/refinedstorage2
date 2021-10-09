package com.refinedmods.refinedstorage2.platform.fabric.item;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.item.StorageDiskItemImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.FabricQuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk.FluidStorageDiskType;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class FluidStorageDiskItem extends StorageDiskItemImpl {
    private final FluidStorageType type;

    public FluidStorageDiskItem(Settings settings, FluidStorageType type) {
        super(settings);
        this.type = type;
    }

    @Override
    protected String formatQuantity(long qty) {
        return FabricQuantityFormatter.formatDropletsAsBucket(qty);
    }

    @Override
    protected Optional<ItemStack> createStoragePart(int count) {
        if (type == FluidStorageType.CREATIVE) {
            return Optional.empty();
        }
        return Optional.of(new ItemStack(Rs2Mod.ITEMS.getFluidStoragePart(type), count));
    }

    @Override
    protected StorageDisk<?> createStorageDisk(World world) {
        return new PlatformStorageDiskImpl<>(
                type.getCapacity(),
                FluidStorageDiskType.INSTANCE,
                Rs2PlatformApiFacade.INSTANCE.getStorageDiskManager(world)::markAsChanged
        );
    }

    @Override
    protected ItemStack createDisassemblyByproduct() {
        return new ItemStack(Rs2Mod.ITEMS.getStorageHousing());
    }

    @Override
    public Optional<StorageChannelType<?>> getType(ItemStack stack) {
        return Optional.of(StorageChannelTypes.FLUID);
    }

    public enum FluidStorageType {
        SIXTY_FOUR_B("64b", 64 * FluidConstants.BUCKET),
        TWO_HUNDRED_FIFTY_SIX_B("256b", 256 * FluidConstants.BUCKET),
        THOUSAND_TWENTY_FOUR_B("1024b", 1024 * FluidConstants.BUCKET),
        FOUR_THOUSAND_NINETY_SIX_B("4096b", 4096 * FluidConstants.BUCKET),
        CREATIVE("creative", -1);

        private final String name;
        private final long capacity;

        FluidStorageType(String name, long capacity) {
            this.name = name;
            this.capacity = capacity;
        }

        public String getName() {
            return name;
        }

        public long getCapacity() {
            return capacity;
        }
    }
}
