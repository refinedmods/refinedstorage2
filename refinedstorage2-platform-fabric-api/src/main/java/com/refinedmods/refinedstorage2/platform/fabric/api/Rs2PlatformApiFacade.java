package com.refinedmods.refinedstorage2.platform.fabric.api;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskManager;

import net.minecraft.item.Item;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public interface Rs2PlatformApiFacade {
    Rs2PlatformApiFacade INSTANCE = new Rs2PlatformApiFacadeProxy();

    PlatformStorageDiskManager getStorageDiskManager(World world);

    ConnectionProvider createConnectionProvider(World world);

    Rs2Item toRs2Item(Item item);

    Item toMcItem(Rs2Item item);

    TranslatableText createTranslation(String category, String value, Object... args);
}
