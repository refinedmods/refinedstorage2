package com.refinedmods.refinedstorage.common.api.storage;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.5")
public interface StorageContainerItemHelper {
    Optional<SerializableStorage> resolveStorage(StorageRepository storageRepository, ItemStack stack);

    void loadStorageIfNecessary(ItemStack stack,
                                Level level,
                                Entity entity,
                                Function<StorageRepository, SerializableStorage> factory);

    void transferStorageIfNecessary(ItemStack stack,
                                    Level level,
                                    Entity entity,
                                    Function<StorageRepository, SerializableStorage> factory);

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
                         @Nullable Long capacity);

    void transferToBlockEntity(ItemStack stack, StorageBlockEntity blockEntity);

    void transferFromBlockEntity(ItemStack stack, StorageBlockEntity blockEntity);

    void markAsToTransfer(ItemStack from, ItemStack to);

    boolean clear(ItemStack stack);
}
