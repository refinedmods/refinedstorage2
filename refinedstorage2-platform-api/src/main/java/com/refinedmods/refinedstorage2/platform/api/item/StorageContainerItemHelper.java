package com.refinedmods.refinedstorage2.platform.api.item;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.LongFunction;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.5")
public interface StorageContainerItemHelper {
    Optional<Storage<?>> resolve(StorageRepository storageRepository, ItemStack stack);

    void set(StorageRepository storageRepository, ItemStack stack, Storage<?> storage);

    Optional<StorageInfo> getInfo(StorageRepository storageRepository, ItemStack stack);

    InteractionResultHolder<ItemStack> tryDisassembly(Level level,
                                                      Player player,
                                                      ItemStack stack,
                                                      ItemStack primaryByproduct,
                                                      @Nullable ItemStack secondaryByproduct);

    void appendToTooltip(ItemStack stack,
                         StorageRepository storageRepository,
                         List<Component> tooltip,
                         TooltipFlag context,
                         LongFunction<String> amountFormatter,
                         boolean hasCapacity);

    // TODO: remove - leaky abstraction
    Optional<UUID> getId(ItemStack stack);

    // TODO: remove - leaky abstraction
    void setId(ItemStack stack, UUID id);
}
