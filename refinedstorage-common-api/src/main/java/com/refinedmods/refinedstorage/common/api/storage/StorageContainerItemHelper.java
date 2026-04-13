package com.refinedmods.refinedstorage.common.api.storage;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongFunction;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

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

    InteractionResult tryDisassembly(Level level,
                                     Player player,
                                     ItemStack stack,
                                     ItemStack primaryByproduct,
                                     @Nullable ItemStack secondaryByproduct);

    void appendToTooltip(ItemStack stack,
                         StorageRepository storageRepository,
                         Consumer<Component> builder,
                         TooltipFlag context,
                         LongFunction<String> amountFormatter,
                         @Nullable Long capacity);

    void transferToBlockEntity(ItemStack stack, StorageBlockEntity blockEntity);

    void transferFromBlockEntity(ItemStack stack, StorageBlockEntity blockEntity);

    void markAsToTransfer(ItemStack from, ItemStack to);

    boolean clear(ItemStack stack);
}
