package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskManagerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.fluid.FabricRs2Fluid;
import com.refinedmods.refinedstorage2.platform.fabric.internal.item.FabricRs2Item;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.FabricConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk.FabricClientStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk.FabricStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk.ItemStorageDiskType;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class Rs2PlatformApiFacadeImpl implements Rs2PlatformApiFacade {
    private final PlatformStorageDiskManager clientStorageDiskManager = new FabricClientStorageDiskManager();
    private final Map<Item, Rs2Item> itemMap = new HashMap<>();
    private final Map<FluidVariant, Rs2Fluid> fluidMap = new HashMap<>();

    @Override
    public PlatformStorageDiskManager getStorageDiskManager(World world) {
        if (world.getServer() == null) {
            return clientStorageDiskManager;
        }

        return world
                .getServer()
                .getWorld(World.OVERWORLD)
                .getPersistentStateManager()
                .getOrCreate(this::createStorageDiskManager, this::createStorageDiskManager, FabricStorageDiskManager.NAME);
    }

    @Override
    public StorageDiskType<Rs2ItemStack> getItemStorageDiskType() {
        return ItemStorageDiskType.INSTANCE;
    }

    @Override
    public ConnectionProvider createConnectionProvider(World world) {
        return new FabricConnectionProvider(world);
    }

    @Override
    public Rs2Item toRs2Item(Item item) {
        return itemMap.computeIfAbsent(item, FabricRs2Item::new);
    }

    @Override
    public Rs2Fluid toRs2Fluid(FluidVariant fluidVariant) {
        return fluidMap.computeIfAbsent(fluidVariant, FabricRs2Fluid::new);
    }

    @Override
    public Item toMcItem(Rs2Item item) {
        return ((FabricRs2Item) item).getItem();
    }

    @Override
    public FluidVariant toMcFluid(Rs2Fluid fluid) {
        return ((FabricRs2Fluid) fluid).getFluidVariant();
    }

    @Override
    public TranslatableText createTranslation(String category, String value, Object... args) {
        return Rs2Mod.createTranslation(category, value, args);
    }

    private FabricStorageDiskManager createStorageDiskManager(NbtCompound tag) {
        var manager = createStorageDiskManager();
        manager.read(tag);
        return manager;
    }

    private FabricStorageDiskManager createStorageDiskManager() {
        return new FabricStorageDiskManager(new StorageDiskManagerImpl());
    }
}
