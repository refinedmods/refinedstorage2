package com.refinedmods.refinedstorage2.platform.fabric.item;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.item.StorageDiskItemImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.FabricQuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk.FluidStorageDiskType;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

// TODO: Recipes
// TODO: Balancing: capacities should match droplets
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
        var disk = new PlatformStorageDiskImpl<>(
                type.getCapacity(),
                StackListImpl.createFluidStackList(),
                FluidStorageDiskType.INSTANCE,
                Rs2PlatformApiFacade.INSTANCE.getStorageDiskManager(world)::markAsChanged
        );

        // TODO: remove
        disk.insert(new Rs2FluidStack(Rs2PlatformApiFacade.INSTANCE.toRs2Fluid(FluidVariant.of(Fluids.WATER))), FluidConstants.BUCKET + (FluidConstants.BUCKET / 2), Action.EXECUTE);
        disk.insert(new Rs2FluidStack(Rs2PlatformApiFacade.INSTANCE.toRs2Fluid(FluidVariant.of(Fluids.LAVA))), FluidConstants.BUCKET + (FluidConstants.BUCKET / 2), Action.EXECUTE);

        return disk;
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
        SIXTY_FOUR_K("64k", 64_000),
        TWO_HUNDRED_FIFTY_SIX_K("256k", 256_000),
        THOUSAND_TWENTY_FOUR_K("1024k", 1024_000),
        FOUR_THOUSAND_NINETY_SIX_K("4096k", 4096_000),
        CREATIVE("creative", -1);

        private final String name;
        private final int capacity;

        FluidStorageType(String name, int capacity) {
            this.name = name;
            this.capacity = capacity;
        }

        public String getName() {
            return name;
        }

        public int getCapacity() {
            return capacity;
        }
    }
}
